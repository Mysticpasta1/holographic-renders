package com.mystic.holographicrenders.gui;

import static com.teamwizardry.librarianlib.core.util.Shorthand.vec;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import com.teamwizardry.librarianlib.facade.container.FacadeView;
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer;
import com.teamwizardry.librarianlib.facade.layers.TextLayer;
import com.teamwizardry.librarianlib.facade.layers.text.TextFit;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryCheckbox;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryToggle;
import com.teamwizardry.librarianlib.mosaic.Mosaic;
import com.teamwizardry.librarianlib.mosaic.MosaicSprite;
import ll.dev.thecodewarrior.bitfont.typesetting.TextLayoutManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.function.Consumer;

public class ProjectorScreen extends FacadeView<ProjectorScreenHandler> {
    private static final MosaicSprite TEXTURE = new Mosaic(new Identifier("holographic_renders", "textures/gui_hologram_projector.png"), 256,256).getSprite("main");

    private boolean lightsEnabled = false;

    public ProjectorScreen(ProjectorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        getMain().setSize(vec(Textures.main.getWidth(), Textures.main.getHeight()));

        SpriteLayer sprite = new SpriteLayer(Textures.main);
        TextLayer text = new TextLayer(128, 7, Language.getInstance().get("block.holographic_renders.projector"));
        text.fitToText(TextFit.BOTH);
        text.setTextAlignment(TextLayoutManager.Alignment.LEFT);
        sprite.add(text);

        PastryCheckbox checkbox = new PastryCheckbox(110, 39, false);
        checkbox.BUS.hook(PastryToggle.StateWillChangeEvent.class, event -> client.getNetworkHandler().sendPacket(ProjectorScreenPacket.createLightAction(event.getNewState())));
        TextLayer light = new TextLayer(8, -1,"Light");
        light.fitToText(TextFit.BOTH);
        light.setTextAlignment(TextLayoutManager.Alignment.LEFT);
        checkbox.add(light);
        checkbox.setState(handler.getLight());

        getMain().add(sprite);
        getMain().add(checkbox);

        getMain().onLayout(() -> {
            text.setX(getMain().getWidth() * 0.5 - text.getWidth() * 0.5);
        });
    }

    public void setLights(boolean lightsEnabled) {
        this.lightsEnabled = lightsEnabled;
    }
}
