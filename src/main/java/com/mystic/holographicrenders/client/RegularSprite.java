package com.mystic.holographicrenders.client;

public class RegularSprite implements Sprite {
    private final int width;
    private final int height;

    public RegularSprite(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getFrameCount() {
        return 1;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getUSize() {
        return 1.0f;
    }

    @Override
    public float getVSize() {
        return 1.0f;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public float maxU(int animFrames) {
        return 1.0f;
    }

    @Override
    public float maxV(int animFrames) {
        return 1.0f;
    }

    @Override
    public float minU(int animFrames) {
        return 0;
    }

    @Override
    public float minV(int animFrames) {
        return 0;
    }
}
