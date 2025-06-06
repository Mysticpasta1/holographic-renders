package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import org.joml.Matrix4f;

import java.net.MalformedURLException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {

    private static MultiBufferSource.BufferSource immediate;


    public ProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) { }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (immediate == null) {
            immediate = HologramRenderLayer.initBuffers(Minecraft.getInstance().renderBuffers().bufferSource());
        }

        Direction facing = ProjectorBlock.getFacing(entity.getBlockState());

        matrices.translate(facing.getStepX() * 0.55, facing.getStepY() * 0.55, facing.getStepZ() * 0.55);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Axis.XN.rotationDegrees(facing.getStepY() == -1 ? 180 : 0));
        matrices.mulPose(Axis.XP.rotationDegrees(facing.getStepZ() * 90));
        matrices.mulPose(Axis.ZN.rotationDegrees(facing.getStepX() * 90));
        matrices.translate(-0.5, -0.5, -0.5);

        // Set provider entities
        AreaProvider.setEntity(entity);
        BlockProvider.setEntity(entity);
        EntityProvider.setEntity(entity);
        ItemProvider.setEntity(entity);
        MapProvider.setEntity(entity);
        TextProvider.setEntity(entity);
        TextureProvider.setEntity(entity);

        if (entity.lightsEnabled()) {
            matrices.pushPose(); // Lights pose
            try {
                RenderSystem.enableDepthTest();
                final VertexConsumer buffer = vertexConsumers.getBuffer(RenderType.lightning());
                final Matrix4f matrix4f = matrices.last().pose();

                final float r = 0.5f, g = 0.5f, b = 1;
                final float bottomY = 0.3f, topY = 0.7f, startAlpha = 0.65f;

                // Lightning geometry
                vertex(matrix4f, buffer, 0.1f, bottomY, 0.125f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.9f, bottomY, 0.125f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 1, topY, -0.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 0, topY, -0.25f, r, g, b, 0);

                vertex(matrix4f, buffer, 0, topY, -0.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 1, topY, -0.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 0.9f, bottomY, 0.125f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.1f, bottomY, 0.125f, r, g, b, startAlpha);

                vertex(matrix4f, buffer, 0.1f, bottomY, 0.875f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.9f, bottomY, 0.875f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 1, topY, 1.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 0, topY, 1.25f, r, g, b, 0);

                vertex(matrix4f, buffer, 0, topY, 1.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 1, topY, 1.25f, r, g, b, 0);
                vertex(matrix4f, buffer, 0.9f, bottomY, 0.875f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.1f, bottomY, 0.875f, r, g, b, startAlpha);

                vertex(matrix4f, buffer, 0.875f, bottomY, 0.1f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.875f, bottomY, 0.9f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 1.25f, topY, 1, r, g, b, 0);
                vertex(matrix4f, buffer, 1.25f, topY, 0, r, g, b, 0);

                vertex(matrix4f, buffer, 1.25f, topY, 0, r, g, b, 0);
                vertex(matrix4f, buffer, 1.25f, topY, 1, r, g, b, 0);
                vertex(matrix4f, buffer, 0.875f, bottomY, 0.9f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.875f, bottomY, 0.1f, r, g, b, startAlpha);

                vertex(matrix4f, buffer, 0.125f, bottomY, 0.1f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.125f, bottomY, 0.9f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, -0.25f, topY, 1, r, g, b, 0);
                vertex(matrix4f, buffer, -0.25f, topY, 0, r, g, b, 0);

                vertex(matrix4f, buffer, -0.25f, topY, 0, r, g, b, 0);
                vertex(matrix4f, buffer, -0.25f, topY, 1, r, g, b, 0);
                vertex(matrix4f, buffer, 0.125f, bottomY, 0.9f, r, g, b, startAlpha);
                vertex(matrix4f, buffer, 0.125f, bottomY, 0.1f, r, g, b, startAlpha);

                RenderSystem.disableDepthTest();
            } finally {
                matrices.popPose(); // Always pop lights pose
            }
        }

        if (entity.getAlpha() != 0) {
            matrices.pushPose(); // Projection pose
            try {
                HologramRenderLayer.setAlpha(entity.getAlpha());
                ItemProjectionHandler.getDataProvider(entity, entity.getItem()).render(matrices, immediate, tickDelta, light, overlay, entity);
                immediate.endBatch();
            } catch (MalformedURLException ignored) {
                // Consider logging or handling for debugging purposes
            } finally {
                matrices.popPose(); // Always pop projection pose
            }
        }
    }



    private void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

}
