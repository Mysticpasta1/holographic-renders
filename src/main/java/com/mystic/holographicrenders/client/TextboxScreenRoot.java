package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;


public class TextboxScreenRoot extends LightweightGuiDescription {

    public TextboxScreenRoot(InteractionHand hand) {
        WGridPanel root = new WGridPanel();
        WButton button = new WButton();
        WTextField textFieldWidget = new WTextField(Component.nullToEmpty("Please enter a valid URL!"));
        WLabel label = new WLabel(Component.literal("Save URL"));
        setRootPanel(root);
        root.setSize(256, 240);
        textFieldWidget.setMaxLength(2000);
        root.add(textFieldWidget, 0, 5, 15, 10);
        root.add(button, 4, 8, 6, 20);
        root.add(label, 6, 7);
        root.validate(this);
        button.setOnClick(() -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(textFieldWidget.getText(), 2000);
            buf.writeEnum(hand);
            ClientPlayNetworking.send(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "url_packet"), buf);
        });
    }
}