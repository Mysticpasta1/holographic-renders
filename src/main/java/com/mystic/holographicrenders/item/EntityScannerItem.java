package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.HolographicRenders;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityScannerItem extends Item {

    public EntityScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }

    @Nullable
    public EntityType<?> getEntityType(ItemStack stack){
        return Registry.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(stack.getOrCreateNbt().getCompound("Entity").getString("id"))).orElse(null);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        NbtCompound stackTag = user.getStackInHand(hand).getOrCreateNbt();
        if (stackTag.contains("Entity")) return ActionResult.PASS;

        NbtCompound entityTag = new NbtCompound();
        entity.saveSelfNbt(entityTag);

        stackTag.put("Entity", entityTag);

        return ActionResult.success(user.world.isClient);
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
            Registry.ENTITY_TYPE.getOrEmpty(Identifier.tryParse(stackTag.getCompound("Entity").getString("id"))).ifPresent(entityType -> {
                tooltip.add(new LiteralText("§7Entity: ").append(new TranslatableText(entityType.getTranslationKey()).formatted(Formatting.AQUA)));
            });
        } else {
            tooltip.add(new LiteralText("§7Blank"));
        }
    }
}