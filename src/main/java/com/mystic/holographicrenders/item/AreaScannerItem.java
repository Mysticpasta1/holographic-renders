package com.mystic.holographicrenders.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AreaScannerItem extends Item {
    public AreaScannerItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack itemstack = context.getStack();
        CompoundTag tag = itemstack.getOrCreateTag();

        if (tag.contains("Pos2")) {
            return ActionResult.PASS;
        }

        if (tag.contains("Pos1")) {
            tag.putLong("Pos2", context.getBlockPos().asLong());
        } else {
            tag.putLong("Pos1", context.getBlockPos().asLong());
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack itemstack = player.getStackInHand(hand);

        CompoundTag tag = itemstack.getOrCreateTag();

        if (!player.isSneaking()) {
            return TypedActionResult.pass(itemstack);
        }

        tag.remove("Pos1");
        tag.remove("Pos2");

        return TypedActionResult.success(itemstack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        List<Text> newLines = new ArrayList<>();

        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("Pos1")) {
            BlockPos pos = BlockPos.fromLong(tag.getLong("Pos1"));
            newLines.add(new LiteralText("§7Start Position: §8[§b" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "§8]"));
        }

        if (tag.contains("Pos2")) {
            BlockPos pos = BlockPos.fromLong(tag.getLong("Pos2"));
            newLines.add(new LiteralText("§7End Position: §8[§b" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "§8]"));
        }

        if (newLines.isEmpty()) {
            newLines.add(new LiteralText("§7Blank"));
        } else if (newLines.size() == 2) {
            newLines.add(new LiteralText(""));
            newLines.add(new LiteralText("§aReady to project!"));
        }

        tooltip.addAll(newLines);
    }
}
