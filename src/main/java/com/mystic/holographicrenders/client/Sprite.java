package com.mystic.holographicrenders.client;

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

    default void render(Matrix4f matrix, int i, int i1, int i2, int i3, int i4, Color white) {

    }

}
