package com.mystic.holographicrenders.gui;

import static com.teamwizardry.librarianlib.core.util.Shorthand.vec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.item.WidgetType;
import com.teamwizardry.librarianlib.facade.FacadeScreen;
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer;
import com.teamwizardry.librarianlib.facade.layers.StackLayout;
import com.teamwizardry.librarianlib.facade.layers.StackLayoutBuilder;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryButton;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryLabel;
import com.teamwizardry.librarianlib.facade.pastry.layers.dropdown.DropdownTextItem;
import com.teamwizardry.librarianlib.facade.pastry.layers.dropdown.PastryDropdown;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class WidgetScreen extends FacadeScreen {
    private WidgetType widget = WidgetType.Blank;

    public WidgetScreen(Hand hand) {
        super(LiteralText.EMPTY);

        PastryLabel label = new PastryLabel(4, 0, "Select Widget");

        PastryDropdown<WidgetType> dropdown;
        int width = Stream.of(WidgetType.values()).map(WidgetType::toString).mapToInt(MinecraftClient.getInstance().textRenderer::getWidth).max().orElse(300);

        dropdown = new PastryDropdown<>(0,0, width + 15, widgetType -> widget = widgetType);

        dropdown.getItems().addAll(Arrays.stream(WidgetType.values()).map(a -> new DropdownTextItem<>(a, a.toString())).collect(Collectors.toList()));

        dropdown.select(WidgetType.Blank);

        PastryButton button = new PastryButton("Confirm", 4, 8, 12, 11);
        button.fitLabel();

        button.hook(PastryButton.ClickEvent.class, clickEvent -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeEnumConstant(hand);
            buf.writeEnumConstant(widget);
            ClientPlayNetworking.send(new Identifier(HolographicRenders.MOD_ID, "widget_packet"), buf);
        });

        StackLayout layout = new StackLayoutBuilder(0,0)
                .add(label, dropdown, button)
                .alignCenterX().alignCenterY().vertical().spacing(3).fit().build();

        getMain().setFrame(layout.getBounds().grow(5));

        layout.setPos(vec(5, 5));

        SpriteLayer background = new SpriteLayer(Textures.projector, 0,0, getMain().getWidthi(), getMain().getHeighti());

        getMain().add(background);
        getMain().add(layout);
    }
}
