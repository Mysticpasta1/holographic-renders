package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.client.AreaProvider;
import com.mystic.holographicrenders.client.BlockProvider;
import com.mystic.holographicrenders.client.EmptyProvider;
import com.mystic.holographicrenders.client.EntityProvider;
import com.mystic.holographicrenders.client.ItemProvider;
import com.mystic.holographicrenders.client.MapProvider;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.TextureProvider;
import com.mystic.holographicrenders.client.TextProvider;
import com.mystic.holographicrenders.item.AreaScannerItem;
import com.mystic.holographicrenders.item.EntityScannerItem;
import com.mystic.holographicrenders.item.TextureScannerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class ItemProjectionHandler {

    private static final HashMap<Predicate<ItemStack>, ItemProjectionBehaviour> REGISTRY = new HashMap<>();

    static {
        registerBehaviour(stack -> stack.getItem() instanceof EntityScannerItem, (be, stack) -> {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (data.isEmpty()) return EmptyProvider.INSTANCE;
            CompoundTag tag = data.copyTag();
            if (!tag.contains("Entity")) return EmptyProvider.INSTANCE;

            EntityType<?> type = ((EntityScannerItem) stack.getItem()).getEntityType(stack);
            if (type == null) return EmptyProvider.INSTANCE;

            Entity entity = type.create(be.getLevel());
            if (entity == null) return EmptyProvider.INSTANCE;

            entity.load(tag.getCompound("Entity"));
            entity.absMoveTo(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ());
            return EntityProvider.from(entity);
        });

        registerBehaviour(stack -> stack.getItem() instanceof AreaScannerItem, (be, stack) -> {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (data.isEmpty()) return EmptyProvider.INSTANCE;
            CompoundTag tag = data.copyTag();

            if (tag.contains("Pos1") && tag.contains("Pos2")) {
                BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
                BlockPos pos2 = BlockPos.of(tag.getLong("Pos2"));
                try {
                    return AreaProvider.from(pos1, pos2);
                } catch (ExecutionException ignored) {
                    return EmptyProvider.INSTANCE;
                }
            }

            return EmptyProvider.INSTANCE;
        });

        registerBehaviour(stack -> stack.getItem() instanceof TextureScannerItem, (be, stack) -> {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (data.isEmpty()) return EmptyProvider.INSTANCE;
            CompoundTag tag = data.copyTag();
            String url = tag.getString("URL");
            if (url == null || url.isEmpty()) return EmptyProvider.INSTANCE;
            try {
                return TextureProvider.of(url);
            } catch (ExecutionException e) {
                return EmptyProvider.INSTANCE;
            }
        });

        registerBehaviour(
                itemStack -> itemStack.getItem() == Items.FILLED_MAP,
                (be, stack) -> {
                    MapId mapId = stack.get(DataComponents.MAP_ID);
                    if (mapId == null) {
                        return EmptyProvider.INSTANCE;
                    }

                    return MapProvider.of(mapId);
                }
        );

        registerBehaviour(stack -> stack.getItem() instanceof BlockItem, (be, stack) ->
                BlockProvider.from(((BlockItem) stack.getItem()).getBlock().defaultBlockState())
        );

        registerBehaviour(stack -> stack.getItem() == Items.NAME_TAG, (be, stack) ->
                TextProvider.from(stack.getHoverName())
        );
    }

    public static void registerBehaviour(Predicate<ItemStack> condition, ItemProjectionBehaviour behaviour) {
        REGISTRY.put(condition, behaviour);
    }

    public static RenderDataProvider<?> getDataProvider(ProjectorBlockEntity be, ItemStack stack) {
        for (Map.Entry<Predicate<ItemStack>, ItemProjectionBehaviour> entry : REGISTRY.entrySet()) {
            if (!entry.getKey().test(stack)) continue;
            return entry.getValue().getProvider(be, stack);
        }
        return ItemProvider.from(stack);
    }

    @FunctionalInterface
    interface ItemProjectionBehaviour {
        RenderDataProvider<?> getProvider(ProjectorBlockEntity be, ItemStack stack);
    }
}
