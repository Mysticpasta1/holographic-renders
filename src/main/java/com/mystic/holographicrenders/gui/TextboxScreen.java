package com.mystic.holographicrenders.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import org.lwjgl.glfw.GLFW;

public class TextboxScreen extends CottonClientScreen {
    public TextboxScreen(GuiDescription description) {
        super(description);
    }



    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_E) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}