package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.TextboxScreen;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;

public class HolographicRendersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY, ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();

        ScreenRegistry.register(HolographicRenders.PROJECTOR_SCREEN_HANDLER, ProjectorScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ProjectorScreenPacket.UPDATE_ID, ProjectorScreenPacket::onClientUpdate);
    }
}
