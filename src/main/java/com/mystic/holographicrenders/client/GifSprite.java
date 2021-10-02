package com.mystic.holographicrenders.client;

import com.teamwizardry.librarianlib.mosaic.Sprite;
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

    @Override
    public int getFrameCount() {
        return definition.frames.length;
    }

    @Override
    public int getHeight() {
        return definition.height;
    }

    @NotNull
    @Override
    public Identifier getTexture() {
        return identifier;
    }

    @Override
    public float getUSize() {
        return 1.0f;
    }

    @Override
    public float getVSize() {
        return definition.uvHeight;
    }

    @Override
    public int getWidth() {
        return definition.width;
    }

    @Override
    public float minU(int animFrames) {
        return 0;
    }

    @Override
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
