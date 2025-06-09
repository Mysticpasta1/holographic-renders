package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;

public interface Sprite {

    int getFrameCount();

    public int getHeight();

    public float getUSize();

    public float getVSize();

    public int getWidth();

    public float minU(int animFrames);

    public float minV(int animFrames);

    public float maxU(int animFrames);

    public float maxV(int animFrames);

    default void render(VertexConsumer immediate, int texture, Matrix4f matrix, int x, int y, int width, int height, int frame, Color white, ProjectorBlockEntity be) {
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        var color = white.getRGB();

        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = be.getAlpha();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        Matrix4f model = matrix;
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        float u1 = minU(frame), v1 = minV(frame), u2 = maxU(frame), v2 = maxV(frame);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(model, x,         y + height, 0).uv(u1, v2).endVertex();
        buffer.vertex(model, x + width, y + height, 0).uv(u2, v2).endVertex();
        buffer.vertex(model, x + width, y,          0).uv(u2, v1).endVertex();
        buffer.vertex(model, x,         y,          0).uv(u1, v1).endVertex();
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}
