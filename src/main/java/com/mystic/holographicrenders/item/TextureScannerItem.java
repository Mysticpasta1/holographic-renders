package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.Common;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TextureScannerItem extends Item {

    public TextureScannerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (world.isClientSide && Common.textScreenRunnable != null) {
            Common.textScreenRunnable.accept(hand);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("URL")) {
            tooltip.add(Component.literal("URL: ").withStyle(ChatFormatting.GREEN).append(Component.literal(tag.getString("URL")).withStyle(ChatFormatting.YELLOW)));
        } else {
            tooltip.add(Component.literal("Empty"));
        }
    }
}