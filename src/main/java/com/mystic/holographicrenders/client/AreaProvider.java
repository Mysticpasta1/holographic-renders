package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import io.wispforest.worldmesher.WorldMesh;
import io.wispforest.worldmesher.renderers.WorldMesherFluidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {
    private static final LoadingCache<Pair<BlockPos, BlockPos>, com.mystic.holographicrenders.client.AreaProvider> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .build(new CacheLoader<Pair<BlockPos, BlockPos>, com.mystic.holographicrenders.client.AreaProvider>() {
                @Override
                public com.mystic.holographicrenders.client.AreaProvider load(Pair<BlockPos, BlockPos> key) {
                    return new com.mystic.holographicrenders.client.AreaProvider(key);
                }
            });

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "area");

    private final Minecraft client;
    private final WorldMesherFluidRenderer worldMesherFluidRenderer;
    private long lastUpdateTick;
    private WorldMesh mesh;
    private static ProjectorBlockEntity entity;

    protected AreaProvider(Pair<BlockPos, BlockPos> data) {
        super(data);
        this.client = Minecraft.getInstance();
        //TODO fix this argh
        this.lastUpdateTick = this.client.level.getGameTime();
        this.worldMesherFluidRenderer = new WorldMesherFluidRenderer();
        invalidateCache();
    }

    public static com.mystic.holographicrenders.client.AreaProvider from(BlockPos start, BlockPos end) throws ExecutionException {
        return cache.get(Pair.of(start, end));
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        AreaProvider.entity = entity;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        if (client.level.getGameTime() - lastUpdateTick > 160) {
            lastUpdateTick = client.level.getGameTime();
            mesh.scheduleRebuild();
        }

        if (!mesh.canRender()) {
            matrices.translate(0.5, 0, 0.5);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(-0.5, 0, -0.5);
            matrices.translate(0, 0.65, 0);
            TextProvider.drawText(matrices, be, 0, Component.nullToEmpty("§b[§aScanning§b]"), immediate);
        } else {
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0.5);

            if(entity.spinEnabled()) {
                matrices.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
            } else {
                matrices.mulPose(Axis.YP.rotationDegrees(entity.getRotation()));
            }

            matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders

            int xSize = (int) (mesh.dimensions().getXsize() + 1);
            int zSize = (int) (mesh.dimensions().getZsize() + 1);

            matrices.translate(-xSize / 2f, 0, -zSize / 2f); //TODO make this usable with translation sliders

            mesh.render(matrices);
            matrices.popPose();
        }
    }

    @Environment(EnvType.CLIENT)
    public void invalidateCache() {
        assert Minecraft.getInstance().level != null;
        mesh = new WorldMesh.Builder(Minecraft.getInstance().level, data.getLeft(), data.getRight())
                .renderActions(HologramRenderLayer.beginAction, HologramRenderLayer.endAction)
                .build();
        if (mesh.state() == WorldMesh.MeshState.CORRUPT) return;
        rebuild();
    }

    @Environment(EnvType.CLIENT)
    public void rebuild() {
        mesh.scheduleRebuild();
    }

    @Override
    public CompoundTag write(ProjectorBlockEntity be) {
        final CompoundTag tag = new CompoundTag();

        tag.putLong("Start", data.getLeft().asLong());
        tag.putLong("End", data.getRight().asLong());

        return tag;
    }

    @Override
    public void read(CompoundTag tag, ProjectorBlockEntity be) {
        BlockPos start = BlockPos.of(tag.getLong("Start"));
        BlockPos end = BlockPos.of(tag.getLong("End"));

        if (!(start.equals(data.getLeft()) && end.equals(data.getRight()))) {
            this.data = Pair.of(start, end);
            invalidateCache();
        }
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
