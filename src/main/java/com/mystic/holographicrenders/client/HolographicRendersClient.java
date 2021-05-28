package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

public class HolographicRendersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY, ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();
    }
}
