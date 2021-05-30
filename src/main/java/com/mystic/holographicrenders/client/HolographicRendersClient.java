package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.HologramScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class HolographicRendersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY, ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();

        ScreenRegistry.register(HolographicRenders.HOLOGRAM_SCREEN_HANDLER, HologramScreen::new);
    }
}
