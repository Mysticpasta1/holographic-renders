package com.mystic.holographicrenders.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AreaScannerItem extends Item {
    public AreaScannerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (!data.isEmpty() && data.copyTag().contains("Pos2")) {
            return InteractionResult.PASS;
        }

        BlockPos clickedPos = context.getClickedPos();

        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            if (nbt.contains("Pos2")) {
                return;
            }
            if (nbt.contains("Pos1")) {
                nbt.putLong("Pos2", clickedPos.asLong());
            } else {
                nbt.putLong("Pos1", clickedPos.asLong());
            }
        });

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            nbt.remove("Pos1");
            nbt.remove("Pos2");
        });

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag context) {
        List<Component> newLines = new ArrayList<>();

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.isEmpty() ? new CompoundTag() : data.copyTag();

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
