package com.mystic.holographicrenders.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ProjectorScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("holographic_renders", "textures/gui_hologram_projector.png");
    private boolean LightsOnOff = true;

    public ProjectorScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
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

        CheckboxWidget Lights_On_Or_Off = new CallbackCheckboxWidget(20, 20, Text.of("Lights On or Off"), LightsOnOff, aBoolean -> {
            LightsOnOff = aBoolean;
        });
        addButton(Lights_On_Or_Off);
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
            System.out.println(isChecked());
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(isChecked());
            ClientPlayNetworking.send(new Identifier(HolographicRenders.MOD_ID, "send_side_light_packet"), buf);
        }
    }
}
