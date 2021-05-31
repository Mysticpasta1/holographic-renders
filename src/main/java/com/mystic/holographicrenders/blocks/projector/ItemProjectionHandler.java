package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ItemProjectionHandler {

    private static final HashMap<Predicate<ItemStack>, ItemProjectionBehaviour> REGISTRY = new HashMap<>();

    static {

        registerBehaviour(stack -> stack.getItem() instanceof SpawnEggItem, (be, stack) -> {
            final BlockPos pos = be.getPos();

            EntityType<?> type = ((SpawnEggItem) stack.getItem()).getEntityType(stack.getTag());
            Entity entity = type.create(be.getWorld());
            entity.updatePosition(pos.getX(), pos.getY(), pos.getZ());

            return RenderDataProvider.EntityProvider.from(entity);
        });

        registerBehaviour(stack -> stack.getItem() == HolographicRenders.AREA_SCANNER, (be, stack) -> {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("Pos1") && tag.contains("Pos2")) {
                BlockPos pos1 = BlockPos.fromLong(tag.getLong("Pos1"));
                BlockPos pos2 = BlockPos.fromLong(tag.getLong("Pos2"));
                return RenderDataProvider.AreaProvider.from(pos1, pos2);
            } else {
                return RenderDataProvider.EmptyProvider.INSTANCE;
            }
        });

        registerBehaviour(stack -> stack.getItem() instanceof BlockItem, (be, stack) -> RenderDataProvider.BlockProvider.from(((BlockItem) stack.getItem()).getBlock().getDefaultState()));

        registerBehaviour(stack -> stack.getItem() == Items.NAME_TAG, (be, stack) -> RenderDataProvider.TextProvider.from(stack.getName()));
    }

    public static void registerBehaviour(Predicate<ItemStack> condition, ItemProjectionBehaviour behaviour) {
        REGISTRY.put(condition, behaviour);
    }

    public static RenderDataProvider<?> getDataProvider(ProjectorBlockEntity be, ItemStack stack) {
        for (Map.Entry<Predicate<ItemStack>, ItemProjectionBehaviour> entry : REGISTRY.entrySet()) {
            if (!entry.getKey().test(stack)) continue;
            return entry.getValue().getProvider(be, stack);
        }
        return RenderDataProvider.ItemProvider.from(stack);
    }


    @FunctionalInterface
    interface ItemProjectionBehaviour {
        RenderDataProvider<?> getProvider(ProjectorBlockEntity be, ItemStack stack);
    }

}
