package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;

public interface Sprite {

    int getFrameCount();

    public int getHeight();

    @NotNull
    public Identifier getTexture();

    public float getUSize();

    public float getVSize();

    public int getWidth();

    public float minU(int animFrames);

    public float minV(int animFrames);

    public float maxU(int animFrames);

    public float maxV(int animFrames);

    default void render(VertexConsumer immediate, Matrix4f matrix, int x, int y, int width, int height, int frame, Color white) {
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        var color = white.getRGB();

        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f model = matrix;
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, getTexture());
        RenderSystem.setShaderColor(r, g, b, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        float u1 = minU(frame), v1 = minV(frame), u2 = maxU(frame), v2 = maxV(frame);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(model, x,         y + height, 0).texture(u1, v2).next();
        buffer.vertex(model, x + width, y + height, 0).texture(u2, v2).next();
        buffer.vertex(model, x + width, y,          0).texture(u2, v1).next();
        buffer.vertex(model, x,         y,          0).texture(u1, v1).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
}
