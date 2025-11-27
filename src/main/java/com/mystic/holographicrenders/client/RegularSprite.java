package com.mystic.holographicrenders.client;

public record RegularSprite(int width, int height) implements Sprite {

    @Override
    public int getFrameCount() {
        return 1;
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
