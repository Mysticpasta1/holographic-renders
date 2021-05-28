package com.mystic.holographicrenders.client;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Supplier;

public class RenderDataProviderRegistry {

    private static final HashMap<Identifier, Supplier<RenderDataProvider<?>>> REGISTRY = new HashMap<>();

    public static void register(Identifier typeId, Supplier<RenderDataProvider<?>> factory) {
        if (REGISTRY.containsKey(typeId)) throw new IllegalStateException("Tried to double-register provider with type id" + typeId + "!");
        REGISTRY.put(typeId, factory);
    }

    public static RenderDataProvider<?> getProvider(Identifier typeId) {
        return REGISTRY.get(typeId).get();
    }

}
