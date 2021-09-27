package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.item.EntityScannerItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class ItemProjectionHandler {

    private static final HashMap<Predicate<ItemStack>, ItemProjectionBehaviour> REGISTRY = new HashMap<>();

    static {
        registerBehaviour(stack -> stack.getItem() == HolographicRenders.ENTITY_SCANNER, (be, stack) -> {
            if (!stack.getOrCreateTag().contains("Entity")) return RenderDataProvider.EmptyProvider.INSTANCE;
            EntityType<?> type = ((EntityScannerItem) stack.getItem()).getEntityType(stack);
            if (type == null) return RenderDataProvider.EmptyProvider.INSTANCE;
            Entity entity = type.create(be.getWorld());
            entity.fromTag(stack.getOrCreateTag().getCompound("Entity"));
            entity.updatePosition(be.getPos().getX(), be.getPos().getY(), be.getPos().getZ());
            return RenderDataProvider.EntityProvider.from(entity);
        });

        registerBehaviour(stack -> stack.getItem() == HolographicRenders.AREA_SCANNER, (be, stack) -> {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("Pos1") && tag.contains("Pos2")) {
                BlockPos pos1 = BlockPos.fromLong(tag.getLong("Pos1"));
                BlockPos pos2 = BlockPos.fromLong(tag.getLong("Pos2"));
                try {
                    return RenderDataProvider.AreaProvider.from(pos1, pos2);
                } catch (ExecutionException ignored) {
                    return RenderDataProvider.EmptyProvider.INSTANCE;
                }
            }

            return RenderDataProvider.EmptyProvider.INSTANCE;

        });

        registerBehaviour(stack -> stack.getItem() == HolographicRenders.TEXTURE_SCANNER, (be, stack) -> RenderDataProvider.TextureProvider.of(be.getStack(0).getOrCreateTag().getString("URL")));

        registerBehaviour(stack -> stack.getItem() instanceof BlockItem, (be, stack) -> RenderDataProvider.BlockProvider.from(((BlockItem) stack.getItem()).getBlock().getDefaultState()));

        registerBehaviour(stack -> stack.getItem() == Items.NAME_TAG, (be, stack) -> RenderDataProvider.TextProvider.from(stack.getName()));
    }

    /**
     * Registers a new behaviour that should be applied if the given predicate is met
     *
     * @param condition The {@link Predicate} to satisfy to apply the given behaviour
     * @param behaviour The behaviour to register
     */
    public static void registerBehaviour(Predicate<ItemStack> condition, ItemProjectionBehaviour behaviour) {
        REGISTRY.put(condition, behaviour);
    }

    /**
     * Creates a {@link RenderDataProvider} for the given ItemStack or an {@link RenderDataProvider.ItemProvider} if there is no special behaviour registered
     *
     * @param be The {@link ProjectorBlockEntity} the item is in
     * @param stack The {@link ItemStack} that's used to provide data
     * @return The provider for the given stack
     */
    public static RenderDataProvider<?> getDataProvider(ProjectorBlockEntity be, ItemStack stack) {
        for (Map.Entry<Predicate<ItemStack>, ItemProjectionBehaviour> entry : REGISTRY.entrySet()) {
            if (!entry.getKey().test(stack)) continue;
            return entry.getValue().getProvider(be, stack);
        }
        return RenderDataProvider.ItemProvider.from(stack);
    }

    /**
     * A behaviour that defines how to create a {@link RenderDataProvider} for a given {@link ItemStack}
     */
    @FunctionalInterface
    interface ItemProjectionBehaviour {
        RenderDataProvider<?> getProvider(ProjectorBlockEntity be, ItemStack stack);
    }

}
