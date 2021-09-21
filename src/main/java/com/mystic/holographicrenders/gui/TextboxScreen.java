package com.mystic.holographicrenders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class TextboxScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("holographic_renders", "textures/gui_textbox_base.png");

    public TextboxScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 4210752);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        TextFieldWidget textFieldWidget = new TextboxScreen.CallbackTextboxWidget(textRenderer, x + (backgroundWidth / 2 - 67) , y + (backgroundHeight / 2), (int) (backgroundWidth / 1.3), 12, Text.of("something"));
        addButton(textFieldWidget);

    }

    private void reload() {
        this.init(MinecraftClient.getInstance(), this.width, this.height);
    }


    public static class CallbackTextboxWidget extends TextFieldWidget {
        public CallbackTextboxWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
            super(textRenderer, x, y, width, height, text);
            if(keyPressed(GLFW.GLFW_KEY_ENTER, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_ENTER), 0)){
                String url = CallbackTextboxWidget.this.getText();
                CallbackTextboxWidget.drawCenteredString(new MatrixStack(), textRenderer, "Saved url", x, y - 20, 0x00a8f3);
                setText(this.getText());
            }
        }
    }
}
