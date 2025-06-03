package com.mystic.holographicrenders.item;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AreaScannerItem extends Item {
    public AreaScannerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemstack = context.getItemInHand();
        CompoundTag tag = itemstack.getOrCreateTag();

        if (tag.contains("Pos2")) {
            return InteractionResult.PASS;
        }

        if (tag.contains("Pos1")) {
            tag.putLong("Pos2", context.getClickedPos().asLong());
        } else {
            tag.putLong("Pos1", context.getClickedPos().asLong());
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        ItemStack itemstack = player.getItemInHand(hand);
        CompoundTag tag = itemstack.getOrCreateTag();

        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(itemstack);

        tag.remove("Pos1");
        tag.remove("Pos2");

        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {

        List<Component> newLines = new ArrayList<>();

        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("Pos1")) {
            BlockPos pos = BlockPos.of(tag.getLong("Pos1"));
            newLines.add(Component.nullToEmpty("§7Start Position: §8[§b" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "§8]"));
        }

        if (tag.contains("Pos2")) {
            BlockPos pos = BlockPos.of(tag.getLong("Pos2"));
            newLines.add(Component.nullToEmpty("§7End Position: §8[§b" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "§8]"));
        }

        if (newLines.isEmpty()) {
            newLines.add(Component.nullToEmpty("§7Blank"));
        } else if (newLines.size() == 2) {
            newLines.add(Component.nullToEmpty(""));
            newLines.add(Component.nullToEmpty("§aReady to project!"));
        }

        tooltip.addAll(newLines);
    }
}
