package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;


public class TextboxScreenRoot extends LightweightGuiDescription {

    public TextboxScreenRoot(Hand hand) {
        WGridPanel root = new WGridPanel();
        WButton button = new WButton();
        WTextField textFieldWidget = new WTextField(Text.of("Please enter a valid URL!"));
        WLabel label = new WLabel("Save PNG or JPEG URL");
        setRootPanel(root);
        root.setSize(256, 240);
        textFieldWidget.setMaxLength(90);
        root.add(textFieldWidget, 0, 5, 15, 10);
        root.add(button, 4, 8, 6, 12);
        root.add(label, 4, 0);
        root.validate(this);
        button.setOnClick(() -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(textFieldWidget.getText());
            buf.writeEnumConstant(hand);
            ClientPlayNetworking.send(new Identifier(HolographicRenders.MOD_ID, "url_packet"), buf);
        });
    }
}