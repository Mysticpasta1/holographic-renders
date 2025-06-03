package com.mystic.holographicrenders.client;

import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class RenderDataProviderRegistry {

    private static final HashMap<ResourceLocation, Supplier<RenderDataProvider<?>>> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation typeId, Supplier<RenderDataProvider<?>> factory) {
        if (REGISTRY.containsKey(typeId)) throw new IllegalStateException("Tried to double-register provider with type id" + typeId + "!");
        REGISTRY.put(typeId, factory);
    }

    public static RenderDataProvider<?> getProvider(RenderDataProvider<?> previousProvider, ResourceLocation typeId) {
        return previousProvider != null && previousProvider.getTypeId().equals(typeId) ? previousProvider : REGISTRY.get(typeId).get();
    }

}
