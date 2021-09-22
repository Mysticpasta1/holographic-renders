package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.client.TextboxScreenRoot;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Objects;

public class TextboxScreen extends CottonClientScreen {
    private static final Identifier TEXTURE = new Identifier("holographic_renders", "textures/gui_textbox_base.png");
    private static String url;
    public TextboxScreen(GuiDescription description) {
        super(description);
    }

    @Override
    public void init() {
        super.init();
    }

    private void reload() {
        this.init(MinecraftClient.getInstance(), this.width, this.height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_E) {
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER){
            new TextboxScreenRoot().fireThisThing();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);

    }
}
