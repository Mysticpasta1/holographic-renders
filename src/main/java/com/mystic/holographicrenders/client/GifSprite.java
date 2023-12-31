package com.mystic.holographicrenders.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class GifSprite implements Sprite {
    private final Identifier identifier;

    private final GifDefinition definition;

    public GifSprite(Identifier identifier, GifDefinition definition) {
        this.identifier = identifier;
        this.definition = definition;
    }

    public int getFrameCount() {
        return definition.frames.length;
    }

    public int getHeight() {
        return definition.height;
    }

    @NotNull
    public Identifier getTexture() {
        return identifier;
    }

    public float getUSize() {
        return 1.0f;
    }

    public float getVSize() {
        return definition.uvHeight;
    }

    public int getWidth() {
        return definition.width;
    }

    public float minU(int animFrames) {
        return 0;
    }

    public float minV(int animFrames) {
        return getVSize() * definition.frames[MathHelper.clamp(animFrames, 0, getFrameCount()-1)];
    }

    @Override
    public float maxU(int animFrames) {
        return 1;
    }

    @Override
    public float maxV(int animFrames) {
        return minV(animFrames) + getVSize();
    }

    public static class GifDefinition {
        int[] frames;
        public float uvHeight;
        public int width;
        public int height;
    }
}
