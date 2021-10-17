package com.mystic.holographicrenders.item;

import java.util.List;

import com.mystic.holographicrenders.Common;
import com.mystic.holographicrenders.HolographicRenders;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WidgetScannerItem extends Item {

    public WidgetScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack itemStack = player.getStackInHand(hand);

        if (world.isClient &&  Common.widgetScreenRunnable != null) {
            Common.widgetScreenRunnable.accept(hand);
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound tag = stack.getOrCreateNbt();
        if(tag.contains("Widget")) {
            tooltip.add(new LiteralText("Widget: ").formatted(Formatting.GREEN).append(new LiteralText(WidgetType.fromId(tag.getInt("Widget")).toString()).formatted(Formatting.YELLOW)));
        } else {
            tooltip.add(new LiteralText("Empty"));
        }
    }
}