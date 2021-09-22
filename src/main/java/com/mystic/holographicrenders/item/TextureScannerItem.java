package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.TextboxScreenRoot;
import com.mystic.holographicrenders.gui.TextboxScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TextureScannerItem extends Item{
    public TextureScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack itemStack = player.getStackInHand(hand);

        if (!world.isClient) return TypedActionResult.success(itemStack);

        MinecraftClient.getInstance().openScreen(new TextboxScreen(new TextboxScreenRoot()));

        return TypedActionResult.success(itemStack);
    }
}