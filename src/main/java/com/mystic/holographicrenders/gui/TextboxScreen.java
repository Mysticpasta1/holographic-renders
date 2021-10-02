package com.mystic.holographicrenders.gui;

import static com.teamwizardry.librarianlib.core.util.Shorthand.vec;

import java.awt.Color;

import com.mystic.holographicrenders.HolographicRenders;
import com.teamwizardry.librarianlib.facade.FacadeScreen;
import com.teamwizardry.librarianlib.facade.layers.InputLayout;
import com.teamwizardry.librarianlib.facade.layers.RectLayer;
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer;
import com.teamwizardry.librarianlib.facade.layers.StackLayout;
import com.teamwizardry.librarianlib.facade.layers.StackLayoutBuilder;
import com.teamwizardry.librarianlib.facade.layers.TextInputLayer;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryButton;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryLabel;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class TextboxScreen extends FacadeScreen {
    public TextboxScreen(Hand hand) {
        super(LiteralText.EMPTY);

        PastryLabel label = new PastryLabel(4, 0, "Save (PNG, JPEG, GIF) URL");

        TextInputLayer textField = new TextInputLayer(0, 0, label.getWidthi(), 9);
        SpriteLayer textFieldBackground = new SpriteLayer(Textures.textfield);
        textFieldBackground.setFrame(textField.getFrame().grow(2));
        textField.setPos(vec(2,2));
        textField.getContainerLayers().get(0).setColor(Color.WHITE);
        textFieldBackground.add(textField);

        PastryButton button = new PastryButton("Confirm", 4, 8, 12, 11);
        button.fitLabel();

        button.hook(PastryButton.ClickEvent.class, clickEvent -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(textField.getText().getPlaintext(), 2000);
            buf.writeEnumConstant(hand);
            ClientPlayNetworking.send(new Identifier(HolographicRenders.MOD_ID, "url_packet"), buf);
        });

        StackLayout layout = new StackLayoutBuilder(0,0)
                .add(label, textFieldBackground, button)
        .alignCenterX().alignCenterY().vertical().spacing(3).fit().build();

        getMain().setFrame(layout.getBounds().grow(5));

        layout.setPos(vec(5, 5));

        SpriteLayer background = new SpriteLayer(Textures.projector, 0,0, getMain().getWidthi(), getMain().getHeighti());

        getMain().add(background);
        getMain().add(layout);
    }
}
