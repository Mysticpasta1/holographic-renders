package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.Common;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextureScannerItem extends Item {

    public TextureScannerItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (world.isClient && Common.textScreenRunnable != null) {
            Common.textScreenRunnable.accept(hand);
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound tag = stack.getOrCreateNbt();
        if(tag.contains("URL")) {
            tooltip.add(Text.literal("URL: ").formatted(Formatting.GREEN).append(Text.literal(tag.getString("URL")).formatted(Formatting.YELLOW)));
        } else {
            tooltip.add(Text.literal("Empty"));
        }
    }
}