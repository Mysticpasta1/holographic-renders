package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.util.Identifier;

public class HolographicRendersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY, ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();

        ScreenRegistry.register(HolographicRenders.HOLOGRAM_SCREEN_HANDLER, ProjectorScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "sent_alpha_redstone"), (client, handler, buf, responseSender) -> {
            int readBuf2 = buf.readInt();
            client.execute(() -> {
                HologramRenderLayer.setRedAlpha(readBuf2);
            });
        });
    }
}
