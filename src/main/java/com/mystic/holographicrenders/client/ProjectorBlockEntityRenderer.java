package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.net.MalformedURLException;

public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {

    private static VertexConsumerProvider.Immediate immediate;


    public ProjectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) { }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (immediate == null) {
            immediate = HologramRenderLayer.initBuffers(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
        }

        matrices.push();

        Direction facing = ProjectorBlock.getFacing(entity.getCachedState());

        matrices.translate(facing.getOffsetX() * 0.55, facing.getOffsetY() * 0.55, facing.getOffsetZ() * 0.55);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(facing.getOffsetY() == -1 ? 180 : 0));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(facing.getOffsetZ() * 90));
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(facing.getOffsetX() * 90));
        matrices.translate(-0.5, -0.5, -0.5);

        AreaProvider.setEntity(entity);

        if (entity.lightsEnabled()) {
            matrices.push();
            RenderSystem.enableDepthTest();
            final VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLightning());
            final Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            final float r = 0.5f;
            final float g = 0.5f;
            final float b = 1;

            final float bottomY = 0.3f;
            final float topY = 0.7f;
            final float startAlpha = 0.65f;

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
            matrices.pop();
        }

        if (entity.getAlpha() != 0) {
            HologramRenderLayer.setAlpha(entity.getAlpha());
            matrices.push();

            try {
                ItemProjectionHandler.getDataProvider(entity, entity.getItem()).render(matrices, immediate, tickDelta, light, overlay, entity);
            } catch (MalformedURLException ignored) {}

            immediate.draw();
            matrices.pop();
        }
        matrices.pop();
    }


    private void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
    }

}
