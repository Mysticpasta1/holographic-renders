package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.HolographicRenders;
import net.minecraft.item.Item;

public class PlayerScannerItem extends Item {
    public PlayerScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }
}