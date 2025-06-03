package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapProvider extends RenderDataProvider<Integer> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "map");

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
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException {
        matrices.pushPose();
        RenderSystem.enableDepthTest();
        matrices.scale(0.1f, -0.1f, 0.1f);
        matrices.translate(5, -20, 5);

        Player player = Minecraft.getInstance().player;
        double x = player.getX() - be.getBlockPos().getX() - 0.5;
        double z = player.getZ() - be.getBlockPos().getZ() - 0.5;
        float rot = (float) Mth.atan2(z, x);

        matrices.mulPose(Axis.YP.rotation(-rot));
        matrices.mulPose(Axis.YP.rotationDegrees(90));

        matrices.translate(-7.5, 0, 0);

        MapItemSavedData state = MapItem.getSavedData(data, be.getLevel());
        if (state != null) {
            matrices.scale(0.125f, 0.125f, 0.125f);
            Minecraft.getInstance().gameRenderer.getMapRenderer().render(matrices, immediate, data, state, false, light);
        }

        RenderSystem.disableDepthTest();
        matrices.popPose();
    }

    @Override
    protected CompoundTag write(ProjectorBlockEntity be) {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("Id", data);
        return tag;
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {
        this.data = tag.getInt("Id");
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
