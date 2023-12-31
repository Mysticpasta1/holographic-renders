package com.mystic.holographicrenders.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityScannerItem extends Item {

    public EntityScannerItem() {
        super(new Settings().maxCount(1));
    }

    @Nullable
    public EntityType<?> getEntityType(ItemStack stack){
        return Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(stack.getOrCreateNbt().getCompound("Entity").getString("id"))).orElse(null);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        NbtCompound stackTag = user.getStackInHand(hand).getOrCreateNbt();
        if (stackTag.contains("Entity")) return ActionResult.PASS;

        NbtCompound entityTag = new NbtCompound();
        entity.saveSelfNbt(entityTag);

        stackTag.put("Entity", entityTag);

        return ActionResult.success(user.getWorld().isClient);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            if (stack.getOrCreateNbt().contains("Entity")) {
                stack.getOrCreateNbt().remove("Entity");
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.success(stack);
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound stackTag = stack.getOrCreateNbt();

        if (stackTag.contains("Entity")) {
            Registries.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(stackTag.getCompound("Entity").getString("id"))).ifPresent(entityType -> {
                tooltip.add(Text.literal("§7Entity: ").append(Text.of(entityType.getTranslationKey())).formatted(Formatting.AQUA));
            });
        } else {
            tooltip.add(Text.literal("§7Blank"));
        }
    }
}