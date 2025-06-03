package com.mystic.holographicrenders.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityScannerItem extends Item {

    public EntityScannerItem() {
        super(new Properties().stacksTo(1));
    }

    @Nullable
    public EntityType<?> getEntityType(ItemStack stack){
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(stack.getOrCreateTag().getCompound("Entity").getString("id"))).orElse(null);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {

        CompoundTag stackTag = user.getItemInHand(hand).getOrCreateTag();
        if (stackTag.contains("Entity")) return InteractionResult.PASS;

        CompoundTag entityTag = new CompoundTag();
        entity.saveAsPassenger(entityTag);

        stackTag.put("Entity", entityTag);

        return InteractionResult.sidedSuccess(user.level().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        final ItemStack stack = user.getItemInHand(hand);
        if (user.isShiftKeyDown()) {
            if (stack.getOrCreateTag().contains("Entity")) {
                stack.getOrCreateTag().remove("Entity");
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.success(stack);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag stackTag = stack.getOrCreateTag();

        if (stackTag.contains("Entity")) {
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(stackTag.getCompound("Entity").getString("id"))).ifPresent(entityType -> {
                tooltip.add(Component.literal("§7Entity: ").append(Component.nullToEmpty(entityType.getDescriptionId())).withStyle(ChatFormatting.AQUA));
            });
        } else {
            tooltip.add(Component.literal("§7Blank"));
        }
    }
}