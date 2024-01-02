package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import io.wispforest.worldmesher.WorldMesh;
import io.wispforest.worldmesher.mixin.FluidRendererMixin;
import io.wispforest.worldmesher.renderers.WorldMesherFluidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
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

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "area");

    private final MinecraftClient client;
    private final WorldMesherFluidRenderer worldMesherFluidRenderer;
    private long lastUpdateTick;
    private WorldMesh mesh;
    private static ProjectorBlockEntity entity;

    protected AreaProvider(Pair<BlockPos, BlockPos> data) {
        super(data);
        this.client = MinecraftClient.getInstance();
        //TODO fix this argh
        this.lastUpdateTick = this.client.world.getTime();
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
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        if (client.world.getTime() - lastUpdateTick > 160) {
            lastUpdateTick = client.world.getTime();
            mesh.scheduleRebuild();
        }

        if (!mesh.canRender()) {
            matrices.translate(0.5, 0, 0.5);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(-0.5, 0, -0.5);
            matrices.translate(0, 0.65, 0);
            TextProvider.drawText(matrices, be, 0, Text.of("§b[§aScanning§b]"), immediate);
        } else {
            matrices.push();
            matrices.translate(0.5, 0.5, 0.5);

            if(entity.spinEnabled()) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation()));
            }

            matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders

            int xSize = (int) (mesh.dimensions().getXLength() + 1);
            int zSize = (int) (mesh.dimensions().getZLength() + 1);

            matrices.translate(-xSize / 2f, 0, -zSize / 2f); //TODO make this usable with translation sliders

            mesh.render(matrices);
            matrices.pop();
        }
    }

    @Environment(EnvType.CLIENT)
    public void invalidateCache() {
        assert MinecraftClient.getInstance().world != null;
        mesh = new WorldMesh.Builder(MinecraftClient.getInstance().world, data.getLeft(), data.getRight())
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
    public NbtCompound write(ProjectorBlockEntity be) {
        final NbtCompound tag = new NbtCompound();

        tag.putLong("Start", data.getLeft().asLong());
        tag.putLong("End", data.getRight().asLong());

        return tag;
    }

    @Override
    public void read(NbtCompound tag, ProjectorBlockEntity be) {
        BlockPos start = BlockPos.fromLong(tag.getLong("Start"));
        BlockPos end = BlockPos.fromLong(tag.getLong("End"));

        if (!(start.equals(data.getLeft()) && end.equals(data.getRight()))) {
            this.data = Pair.of(start, end);
            invalidateCache();
        }
    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
