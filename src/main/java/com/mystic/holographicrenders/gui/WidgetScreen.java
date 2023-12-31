package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.item.WidgetType;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class WidgetScreen extends CottonClientScreen {
    private WidgetType widget = WidgetType.Blank;

    public WidgetScreen(GuiDescription guiDescription, Hand hand) {
        super(guiDescription);

        WGridPanel root = new WGridPanel();
        WLabel label = new WLabel(Text.literal("Select Widget"));
        root.add(label, 0,0, 4, 1);

        WButton button = new WButton(Text.literal("Confirm"));
        root.add(button, 4, 8, 12, 11);

        button.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(hand);
            buf.writeEnumConstant(widget);
            ClientPlayNetworking.send(new Identifier(HolographicRenders.MOD_ID, "widget_packet"), buf);
        });
    }
}
