package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.blocks.ProjectorBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class ProjectorBlockEntityRenderer extends BlockEntityRenderer<ProjectorBlockEntity> {

    private static VertexConsumerProvider.Immediate immediate;

    public ProjectorBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (immediate == null) {
            immediate = HologramRenderLayer.initBuffers(MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers());
        }

        entity.getRenderer().render(matrices, immediate, tickDelta, light, overlay);

        immediate.draw();
    }
}
