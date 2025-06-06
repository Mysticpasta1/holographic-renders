package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import io.wispforest.worldmesher.WorldMesh;
import io.wispforest.worldmesher.renderers.WorldMesherFluidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {
    private static final LoadingCache<Pair<BlockPos, BlockPos>, AreaProvider> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public AreaProvider load(Pair<BlockPos, BlockPos> key) {
                    return new AreaProvider(key);
                }
            });

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "area");

    private final Minecraft client;
    private final WorldMesherFluidRenderer worldMesherFluidRenderer;
    private long lastUpdateTick;
    private WorldMesh mesh;
    private static ProjectorBlockEntity entity;
    private final Set<Integer> renderedEntityIds = new HashSet<>();
    private final Set<BlockPos> renderedBlockEntityPositions = new HashSet<>();

    protected AreaProvider(Pair<BlockPos, BlockPos> data) {
        super(data);
        this.client = Minecraft.getInstance();
        //TODO fix this argh
        this.lastUpdateTick = this.client.level.getGameTime();
        this.worldMesherFluidRenderer = new WorldMesherFluidRenderer();
        invalidateCache();
    }

    public static AreaProvider from(BlockPos start, BlockPos end) throws ExecutionException {
        return cache.get(Pair.of(start, end));
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        AreaProvider.entity = entity;
    }

    private <T extends BlockEntity> void renderBlockEntity(
            T blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int overlay, int light) {

        if (blockEntity == null
                || blockEntity instanceof ProjectorBlockEntity
                || renderedBlockEntityPositions.contains(blockEntity.getBlockPos())) return;

        BlockEntityRenderer<T> renderer = client.getBlockEntityRenderDispatcher().getRenderer(blockEntity);

        if (renderer != null) {
            renderedBlockEntityPositions.add(blockEntity.getBlockPos());
            renderer.render(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        }
    }

    private <T extends Entity> void renderEntity(
            T entity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light) {

        if (entity == null || renderedEntityIds.contains(entity.getId())) return;

        // Only render if the entity is alive
        if (!entity.isAlive()) return;

        // Extra safeguard for ItemEntity (e.g. dropped items)
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            // Prevent crash on null or empty ItemStack
            if (stack == null || stack.isEmpty()) return;
        }

        EntityRenderer<? super T> renderer = client.getEntityRenderDispatcher().getRenderer(entity);

        if (renderer != null) {
            renderedEntityIds.add(entity.getId());

            float yaw = entity.getViewYRot(partialTicks);

            try {
                renderer.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
            } catch (Exception e) {
                System.err.println("Error rendering entity: " + entity.getClass().getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        if (client.level.getGameTime() - lastUpdateTick > 160) {
            lastUpdateTick = client.level.getGameTime();
            mesh.scheduleRebuild();
        }

        if (!mesh.canRender()) {
            RenderSystem.enableDepthTest();
            matrices.translate(0.5, 0, 0.5);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(-0.5, 0, -0.5);
            matrices.translate(0, 0.65, 0);
            TextProvider.drawText(matrices, be, 0, Component.nullToEmpty("§b[§aScanning§b]"), immediate);
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
            renderedEntityIds.clear();
            renderedBlockEntityPositions.clear();

            matrices.translate(0.5, 0.5, 0.5);

            if (entity.spinEnabled()) {
                matrices.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
            } else {
                matrices.mulPose(Axis.YP.rotationDegrees(entity.getRotation()));
            }

            matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders

            int xSize = (int) (mesh.dimensions().getXsize() + 1);
            int zSize = (int) (mesh.dimensions().getZsize() + 1);

            matrices.translate(-xSize / 2f, 0, -zSize / 2f); //TODO make this usable with translation sliders

            final var blockEntities = mesh.renderInfo().blockEntities();
            blockEntities.forEach((blockPos, entity) -> {
                matrices.pushPose();
                blockPos = entity.getBlockPos().subtract(new Vec3i(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ()));
                matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                renderBlockEntity(entity, 0, matrices, immediate, overlay, light);
                matrices.popPose();
            });

            final var effectiveDelta = client.getFrameTime();
            final var entities = mesh.renderInfo().entities();
            entities.forEach((vec3d, entry) -> {
                Vec3 worldPos = entry.entity().getPosition(effectiveDelta);
                Vec3 relative = worldPos.subtract(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ());
                if (!isInsideMeshBounds(worldPos, mesh.dimensions())) return;
                matrices.pushPose();
                matrices.translate(relative.x, relative.y, relative.z);
                renderEntity(entry.entity(), effectiveDelta, matrices, immediate, entry.light());
                matrices.popPose();
            });


            Vec3 playerWorld = client.player.getPosition(effectiveDelta);
            Vec3 playerRelative = playerWorld.subtract(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ());

            if (isInsideMeshBounds(playerWorld, mesh.dimensions())) {
                matrices.pushPose();
                matrices.translate(playerRelative.x, playerRelative.y, playerRelative.z);
                PlayerRenderer playerRender = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
                playerRender.render(client.player, client.player.getViewYRot(effectiveDelta), effectiveDelta, matrices, immediate, light);
                matrices.popPose();
            }

            playerRelative = client.player.getPosition(effectiveDelta).subtract(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ());
            if (isInsideMeshBounds(playerWorld, mesh.dimensions())) {
                matrices.pushPose();
                Vec3 diff = Vec3.atLowerCornerOf(mesh.startPos()).subtract(client.player.position());
                matrices.translate(-diff.x, -diff.y + 1.65, -diff.z);

                client.particleEngine.render(matrices, immediate,
                        client.gameRenderer.lightTexture(),
                        client.getEntityRenderDispatcher().camera,
                        tickDelta
                );
                matrices.popPose();
            }

            mesh.render(matrices);
            RenderSystem.disableDepthTest();
        }
    }

    private boolean isInsideMeshBounds(Vec3 relativePos, AABB original) {
        AABB expanded = new AABB(
                original.minX,
                original.minY,
                original.minZ,
                original.maxX + 1,
                original.maxY + 1,
                original.maxZ + 1
        );

        return expanded.contains(relativePos);
    }

    @Environment(EnvType.CLIENT)
    public void invalidateCache() {
        assert Minecraft.getInstance().level != null;
        mesh = new WorldMesh.Builder(Minecraft.getInstance().level, data.getLeft(), data.getRight())
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
