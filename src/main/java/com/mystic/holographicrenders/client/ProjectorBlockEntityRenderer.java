package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class ProjectorBlockEntityRenderer extends BlockEntityRenderer<ProjectorBlockEntity> {

    private static VertexConsumerProvider.Immediate immediate;

    public static HologramBlockModelRenderer blockModelRenderer;

    public ProjectorBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (immediate == null) {
            immediate = HologramRenderLayer.initBuffers(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
            blockModelRenderer = new HologramBlockModelRenderer(MinecraftClient.getInstance().getBlockColors());
        }

        matrices.push();
        entity.getRenderer().render(matrices, immediate, tickDelta, light, overlay, entity);
        matrices.pop();

        immediate.draw();

        try {

            final BufferBuilder buffer = (BufferBuilder) vertexConsumers.getBuffer(RenderLayer.getLightning());
            final Matrix4f matrix4f = matrices.peek().getModel();

            final float r = 0.5f;
            final float g = 0.5f;
            final float b = 1;

            final float topY = 1.25f;
            final float startAlpha = 0.65f;

            vertex(matrix4f, buffer, 0.1f, 0.85f, 0.125f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.9f, 0.85f, 0.125f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 1, topY, -0.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 0, topY, -0.25f, r, g, b, 0);

            vertex(matrix4f, buffer, 0, topY, -0.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 1, topY, -0.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 0.9f, 0.85f, 0.125f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.1f, 0.85f, 0.125f, r, g, b, startAlpha);


            vertex(matrix4f, buffer, 0.1f, 0.85f, 0.875f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.9f, 0.85f, 0.875f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 1, topY, 1.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 0, topY, 1.25f, r, g, b, 0);

            vertex(matrix4f, buffer, 0, topY, 1.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 1, topY, 1.25f, r, g, b, 0);
            vertex(matrix4f, buffer, 0.9f, 0.85f, 0.875f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.1f, 0.85f, 0.875f, r, g, b, startAlpha);


            vertex(matrix4f, buffer, 0.875f, 0.85f, 0.1f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.875f, 0.85f, 0.9f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 1.25f, topY, 1, r, g, b, 0);
            vertex(matrix4f, buffer, 1.25f, topY, 0, r, g, b, 0);

            vertex(matrix4f, buffer, 1.25f, topY, 0, r, g, b, 0);
            vertex(matrix4f, buffer, 1.25f, topY, 1, r, g, b, 0);
            vertex(matrix4f, buffer, 0.875f, 0.85f, 0.9f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.875f, 0.85f, 0.1f, r, g, b, startAlpha);


            vertex(matrix4f, buffer, 0.125f, 0.85f, 0.1f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.125f, 0.85f, 0.9f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, -0.25f, topY, 1, r, g, b, 0);
            vertex(matrix4f, buffer, -0.25f, topY, 0, r, g, b, 0);

            vertex(matrix4f, buffer, -0.25f, topY, 0, r, g, b, 0);
            vertex(matrix4f, buffer, -0.25f, topY, 1, r, g, b, 0);
            vertex(matrix4f, buffer, 0.125f, 0.85f, 0.9f, r, g, b, startAlpha);
            vertex(matrix4f, buffer, 0.125f, 0.85f, 0.1f, r, g, b, startAlpha);
        } catch (Exception e) {

        }
    }

    private void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
    }

    public static VertexConsumerProvider.Immediate getImmediate() {
        return immediate;
    }

}
