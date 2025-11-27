package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.Common;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.TextboxScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = HolographicRenders.MOD_ID, value = Dist.CLIENT)
public class HolographicRendersClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY.get(), ProjectorBlockEntityRenderer::new);
            RenderDataProvider.registerDefaultProviders();
            Common.textScreenRunnable = hand ->
                    Minecraft.getInstance().setScreen(new TextboxScreen(hand));
        });
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HolographicRenders.PROJECTOR_SCREEN_HANDLER.get(), ProjectorScreen::new);
    }
}
