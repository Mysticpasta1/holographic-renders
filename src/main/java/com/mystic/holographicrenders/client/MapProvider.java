package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MapProvider extends RenderDataProvider<MapId> {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "map");
    private static ProjectorBlockEntity entity;

    private static final LoadingCache<MapId, MapProvider> CACHE = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public MapProvider load(MapId key) {
                    return new MapProvider(key);
                }
            });

    protected MapProvider(MapId id) {
        super(id);
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        MapProvider.entity = entity;
    }

    public static RenderDataProvider<?> of(MapId id) {
        try {
            return CACHE.get(id);
        } catch (ExecutionException e) {
            return new MapProvider(new MapId(-1));
        }
    }

    @Override
    public void render(PoseStack matrices,
                       MultiBufferSource.BufferSource immediate,
                       float tickDelta,
                       int light,
                       int overlay,
                       BlockEntity be) {

        if (!(be instanceof ProjectorBlockEntity projector)) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null || be.getLevel() == null) {
            return;
        }

        matrices.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        matrices.scale(0.1f, -0.1f, 0.1f);
        matrices.translate(5, -20, 5);

        double dx = player.getX() - be.getBlockPos().getX() - 0.5;
        double dz = player.getZ() - be.getBlockPos().getZ() - 0.5;
        float rot = (float) Mth.atan2(dz, dx);

        matrices.mulPose(Axis.YP.rotation(-rot));
        matrices.mulPose(Axis.YP.rotationDegrees(90));
        matrices.translate(-7.5, 0, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, projector.getAlpha());

        MapItemSavedData state = MapItem.getSavedData(data, be.getLevel());
        if (state != null) {
            matrices.scale(0.125f, 0.125f, 0.125f);
            Minecraft.getInstance().gameRenderer
                    .getMapRenderer()
                    .render(matrices, immediate, data, state, false, light);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrices.popPose();
    }

    @Override
    protected CompoundTag write(ProjectorBlockEntity be) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Id", data.id());
        return tag;
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {
        int id = tag.getInt("Id");
        this.data = new MapId(id);
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
