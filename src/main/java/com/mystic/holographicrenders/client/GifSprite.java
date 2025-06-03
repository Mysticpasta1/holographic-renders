package com.mystic.holographicrenders.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GifSprite implements Sprite {
    private final ResourceLocation identifier;

    private final GifDefinition definition;

    public GifSprite(ResourceLocation identifier, GifDefinition definition) {
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
    public ResourceLocation getTexture() {
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
        return getVSize() * definition.frames[Mth.clamp(animFrames, 0, getFrameCount()-1)];
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
