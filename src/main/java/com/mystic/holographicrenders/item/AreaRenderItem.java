package com.mystic.holographicrenders.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AreaRenderItem extends Item {
    public AreaRenderItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack itemstack = context.getStack();
        CompoundTag tag = itemstack.getOrCreateTag();

        if(tag.contains("Pos2"))
        {
            return ActionResult.PASS;
        }

        if(tag.contains("Pos1"))
        {
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

        if(!player.isSneaking()){
            return TypedActionResult.pass(itemstack);
        }

        tag.remove("Pos1");
        tag.remove("Pos2");

        return TypedActionResult.success(itemstack);
    }
}
