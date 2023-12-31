package com.mystic.holographicrenders.client;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.util.Hand;

public class WidgetScreenRoot extends LightweightGuiDescription {

    public WidgetScreenRoot(Hand hand) {
        WGridPanel root = new WGridPanel();
        root.validate(this);
    }
}