package com.mystic.holographicrenders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.network.LightPacket;
import com.mystic.holographicrenders.network.RotatePacket;
import com.mystic.holographicrenders.network.SpinPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;
import java.util.function.Consumer;

public class ProjectorScreen extends HandledScreen<ProjectorScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("holographic_renders", "textures/gui_hologram_projector.png");

    private boolean lightsEnabled = false;
    private boolean spinEnabled;
    public int rotationInt;

    public ProjectorScreen(ProjectorScreenHandler handler, PlayerInventory inventory, Text text) {
        super(handler, inventory, Text.literal(""));
    }

    @Override
    protected void drawBackground(DrawContext matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderFogColor(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        matrices.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        CheckboxWidget lightCheckbox = new CallbackCheckboxWidget(x + 110, y + 33, Text.of("Light"), lightsEnabled, light -> {
            assert client != null;
            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(LightPacket.createLightAction(light));
        });
        CheckboxWidget spinCheckbox = new CallbackCheckboxWidget(x + 110, y + 60, Text.of("Spin"), spinEnabled, spin -> {
            assert client != null;
            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(SpinPacket.createSpinAction(spin));
        });
        SliderWidget rotation = new CallbackSliderWidget(x + 70, y + 6, 100, 20, Text.of("Rotation: " + rotationInt), rotationInt, rotate -> {
            assert client != null;
            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(RotatePacket.createRotateAction(rotate));
        });
        addDrawableChild(spinCheckbox);
        addDrawableChild(lightCheckbox);
        addDrawableChild(rotation);
    }

    public void setLights(boolean lightsEnabled) {
        this.lightsEnabled = lightsEnabled;
        reload();
    }

    public void setSpin(boolean setSpin) {
        this.spinEnabled = setSpin;
        reload();
    }

    public void setRotationInt(int setRotationInt) {
        this.rotationInt = setRotationInt;
        reload();
    }

    private void reload() {
        this.init(MinecraftClient.getInstance(), this.width, this.height);
    }

    protected static class CallbackCheckboxWidget extends CheckboxWidget {

        private final Consumer<Boolean> changeCallback;

        public CallbackCheckboxWidget(int x, int y, Text message, boolean checked, Consumer<Boolean> changeCallback) {
            super(x, y, 20, 20, message, checked);
            this.changeCallback = changeCallback;
        }

        @Override
        public void onPress() {
            super.onPress();
            changeCallback.accept(isChecked());
        }
    }

    protected static class CallbackSliderWidget extends SliderWidget {

        private final Consumer<Integer> changeCallback;

        public CallbackSliderWidget(int x, int y, int width, int height, Text text, double value, Consumer<Integer> changeCallback) {
            super(x, y, width, height, text, value);
            this.changeCallback = changeCallback;
        }

        @Override
        protected void updateMessage() {}

        @Override
        protected void applyValue() {
            changeCallback.accept(MathHelper.floor(MathHelper.clampedLerp(0.0, 360.0, this.value)));
        }
    }
}