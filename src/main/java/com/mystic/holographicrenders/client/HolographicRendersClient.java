package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.Common;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.TextboxScreen;
import com.mystic.holographicrenders.network.LightPacket;
import com.mystic.holographicrenders.network.RotatePacket;
import com.mystic.holographicrenders.network.SpinPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "holographic_renders", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HolographicRendersClient {

   @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY.get(), ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();

        ScreenRegistry.register(HolographicRenders.PROJECTOR_SCREEN_HANDLER.get(), ProjectorScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(LightPacket.UPDATE_ID, LightPacket::onClientUpdate);
        ClientPlayNetworking.registerGlobalReceiver(SpinPacket.UPDATE_ID, SpinPacket::onClientUpdate);
        ClientPlayNetworking.registerGlobalReceiver(RotatePacket.UPDATE_ID, RotatePacket::onClientUpdate);
        ClientPlayNetworking.registerGlobalReceiver(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "render_packet"), (client, handler, buf, responseSender) -> {
            ItemStack stack = buf.readItem();
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setRenderer(ItemProjectionHandler.getDataProvider((ProjectorBlockEntity) blockEntity, stack), false);
                }
            });
        });
        Common.textScreenRunnable = (hand -> Minecraft.getInstance().setScreen(new TextboxScreen(new TextboxScreenRoot(hand))));
    }
}
