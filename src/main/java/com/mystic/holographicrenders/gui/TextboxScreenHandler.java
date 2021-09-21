package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.item.TextureScannerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

public class TextboxScreenHandler extends ScreenHandler {

    public TextboxScreenHandler(int syncId, TextureScannerItem textureScannerItem) {
        super(HolographicRenders.TEXTBOX_SCREEN_HANDLER, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
