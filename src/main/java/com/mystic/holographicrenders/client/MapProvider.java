package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapProvider extends RenderDataProvider<Integer> {
    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "map");

    private static final LoadingCache<Integer, com.mystic.holographicrenders.client.MapProvider> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .build(new CacheLoader<Integer, com.mystic.holographicrenders.client.MapProvider>() {
                @Override
                public com.mystic.holographicrenders.client.MapProvider load(Integer key) {
                    return new com.mystic.holographicrenders.client.MapProvider(key);
                }
            });

    protected MapProvider(Integer id) {
        super(id);
    }

    public static RenderDataProvider<?> of(Integer id) {
        try {
            return cache.get(id);
        } catch (ExecutionException e) {
            return new com.mystic.holographicrenders.client.MapProvider(-1);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException {
        matrices.push();
        RenderSystem.enableDepthTest();
        matrices.scale(0.1f, -0.1f, 0.1f);
        matrices.translate(5, -20, 5);

        PlayerEntity player = MinecraftClient.getInstance().player;
        double x = player.getX() - be.getPos().getX() - 0.5;
        double z = player.getZ() - be.getPos().getZ() - 0.5;
        float rot = (float) MathHelper.atan2(z, x);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rot));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));

        matrices.translate(-7.5, 0, 0);

        MapState state = FilledMapItem.getMapState(data, be.getWorld());
        if (state != null) {
            matrices.scale(0.125f, 0.125f, 0.125f);
            MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(matrices, immediate, data, state, false, light);
        }

        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    @Override
    protected NbtCompound write(ProjectorBlockEntity be) {
        final NbtCompound tag = new NbtCompound();
        tag.putInt("Id", data);
        return tag;
    }

    @Override
    protected void read(NbtCompound tag, ProjectorBlockEntity be) {
        this.data = tag.getInt("Id");
    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
