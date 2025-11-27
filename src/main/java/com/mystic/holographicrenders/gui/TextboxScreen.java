package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.network.ProjectorPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class TextboxScreen extends Screen {
    private static final Component TITLE = Component.literal("Save URL");

    private final InteractionHand hand;

    private EditBox urlField;
    private Button saveButton;

    public TextboxScreen(InteractionHand hand) {
        super(TITLE);
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 200;
        int fieldHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int fieldX = centerX - fieldWidth / 2;
        int fieldY = centerY - 10;

        // URL text box
        this.urlField = new EditBox(
                this.font,
                fieldX,
                fieldY,
                fieldWidth,
                fieldHeight,
                Component.literal("URL")
        );
        this.urlField.setMaxLength(2000);
        this.urlField.setValue("");
        this.addRenderableWidget(this.urlField);

        // Save button
        this.saveButton = Button.builder(Component.literal("Save"), button -> this.saveAndClose())
                .pos(centerX - 40, fieldY + 30)
                .size(80, 20)
                .build();
        this.addRenderableWidget(this.saveButton);

        this.setInitialFocus(this.urlField);
    }

    private void saveAndClose() {
        String url = this.urlField.getValue().trim();

        // send your existing packet
        PacketDistributor.sendToServer(new ProjectorPackets.UrlToItem(url, this.hand));

        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close on E or Escape (like your old screen)
        if (keyCode == GLFW.GLFW_KEY_E || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        // Let the text box handle typing/keyboard navigation
        if (this.urlField.keyPressed(keyCode, scanCode, modifiers) || this.urlField.canConsumeInput()) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.urlField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title above the field
        graphics.drawString(
                this.font,
                TITLE,
                this.width / 2 - this.font.width(TITLE) / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );
    }
}
