package com.mystic.holographicrenders.gui;

import com.teamwizardry.librarianlib.mosaic.Mosaic;
import com.teamwizardry.librarianlib.mosaic.MosaicSprite;
import com.teamwizardry.librarianlib.mosaic.Sprite;

import net.minecraft.util.Identifier;

public class Textures {
    public static final MosaicSprite main;

    public static final MosaicSprite projector;
    public static final MosaicSprite textfield;

    static {
        final Mosaic TEXTURE = new Mosaic(new Identifier("holographic_renders", "textures/gui_hologram_projector.png"), 256,256);

        main = TEXTURE.getSprite("main");
        projector = TEXTURE.getSprite("projector");
        textfield = TEXTURE.getSprite("textfield");
    }
}
