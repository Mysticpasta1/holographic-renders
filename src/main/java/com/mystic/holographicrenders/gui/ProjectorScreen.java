package com.mystic.holographicrenders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.network.ProjectorPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;
import java.util.function.Consumer;

public class ProjectorScreen extends AbstractContainerScreen<ProjectorScreenHandler> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("holographic_renders", "textures/gui_hologram_projector.png");

    private boolean lightsEnabled = false;
    private boolean spinEnabled;
    public int rotationInt;

    public ProjectorScreen(ProjectorScreenHandler handler, Inventory inventory, Component text) {
        super(handler, inventory, Component.literal(""));
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderFogColor(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindForSetup(TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        matrices.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        Checkbox lightCheckbox = Checkbox.builder(Component.nullToEmpty("Light"), this.font)
                .pos(x + 110, y + 33)
                .selected(lightsEnabled)
                .onValueChange((checkbox, checked) -> {
                    this.lightsEnabled = checked;
                    ProjectorPackets.sendSetLightToServer(checked);
                })
                .build();

        Checkbox spinCheckbox = Checkbox.builder(Component.nullToEmpty("Spin"), this.font)
                .pos(x + 110, y + 60)
                .selected(spinEnabled)
                .onValueChange((checkbox, checked) -> {
                    this.spinEnabled = checked;
                    ProjectorPackets.sendSetSpinToServer(checked);
                })
                .build();

        AbstractSliderButton rotation = new CallbackSliderWidget(
                x + 70,
                y + 6,
                100,
                20,
                Component.nullToEmpty("Rotation: " + rotationInt),
                rotationInt,
                rotate -> {
                    this.rotationInt = rotate;
                    ProjectorPackets.sendSetRotateToServer(rotate);
                });

        addRenderableWidget(spinCheckbox);
        addRenderableWidget(lightCheckbox);
        addRenderableWidget(rotation);
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
        this.init(Minecraft.getInstance(), this.width, this.height);
    }

    protected static class CallbackSliderWidget extends AbstractSliderButton {

        private final Consumer<Integer> changeCallback;

        public CallbackSliderWidget(int x, int y, int width, int height, Component text,
                                    double value, Consumer<Integer> changeCallback) {
            super(x, y, width, height, text, value);
            this.changeCallback = changeCallback;
        }

        @Override
        protected void updateMessage() {}

        @Override
        protected void applyValue() {
            changeCallback.accept(Mth.floor(Mth.clampedLerp(0.0, 360.0, this.value)));
        }
    }
}
