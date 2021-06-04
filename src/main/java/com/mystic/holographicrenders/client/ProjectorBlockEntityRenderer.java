package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

public class ProjectorBlockEntityRenderer extends BlockEntityRenderer<ProjectorBlockEntity> {

    private static VertexConsumerProvider.Immediate immediate;

    public ProjectorBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }



    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (immediate == null) {
            immediate = HologramRenderLayer.initBuffers(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers());
        }

        matrices.push();

        Direction facing = ProjectorBlock.getFacing(entity.getCachedState());

        matrices.translate(facing.getOffsetX() * 0.55, facing.getOffsetY() * 0.55, facing.getOffsetZ() * 0.55);

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(facing.getOffsetY() == -1 ? 180 : 0));
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(facing.getOffsetZ() * 90));
        matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(facing.getOffsetX() * 90));
        matrices.translate(-0.5, -0.5, -0.5);

            if(entity.shouldDrawLights()){
                matrices.push();

                final BufferBuilder buffer = (BufferBuilder) vertexConsumers.getBuffer(RenderLayer.getLightning());
                final Matrix4f matrix4f = matrices.peek().getModel();

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

                matrices.pop();
            }
        matrices.push();
        entity.getRenderer().render(matrices, immediate, tickDelta, light, overlay, entity);
        matrices.pop();

        matrices.pop();

        immediate.draw();
    }

    private void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
    }

    public static VertexConsumerProvider.Immediate getImmediate() {
        return immediate;
    }

}
