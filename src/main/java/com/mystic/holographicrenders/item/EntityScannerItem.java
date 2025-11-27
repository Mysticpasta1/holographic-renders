package com.mystic.holographicrenders.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityScannerItem extends Item {

    public EntityScannerItem() {
        super(new Properties().stacksTo(1));
    }

    @Nullable
    public EntityType<?> getEntityType(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return null;

        CompoundTag tag = data.copyTag();
        String id = tag.getString("EntityId"); // we will store this below
        if (id.isEmpty()) return null;

        return BuiltInRegistries.ENTITY_TYPE
                .getOptional(ResourceLocation.tryParse(id))
                .orElse(null);
    }

    private static boolean hasEntityData(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return false;
        return data.copyTag().contains("Entity");
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack,
                                                  Player user,
                                                  LivingEntity entity,
                                                  InteractionHand hand) {

        if (hasEntityData(stack)) {
            return InteractionResult.PASS;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            CompoundTag entityTag = new CompoundTag();
            // save full entity data
            entity.save(entityTag);
            nbt.put("Entity", entityTag);

            // also store type id explicitly
            ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (typeId != null) {
                nbt.putString("EntityId", typeId.toString());
            }
        });

        // make sure the modified stack is back in the player’s hand
        user.setItemInHand(hand, stack);

        return InteractionResult.sidedSuccess(user.level().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (user.isShiftKeyDown()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
                nbt.remove("Entity");
                nbt.remove("EntityId");
            });
            user.setItemInHand(hand, stack);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                TooltipContext tooltipContext,
                                List<Component> tooltip,
                                TooltipFlag tooltipFlag) {

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (!data.isEmpty() && data.copyTag().contains("EntityId")) {
            CompoundTag tag = data.copyTag();
            String id = tag.getString("EntityId");

            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(id)).ifPresent(entityType -> {
                Component name = entityType.getDescription();
                tooltip.add(
                        Component.literal("Entity: ")
                                .append(name)
                                .withStyle(ChatFormatting.AQUA)
                );
            });
        } else {
            tooltip.add(Component.literal("Blank").withStyle(ChatFormatting.GRAY));
        }
    }
}
