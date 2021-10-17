package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.Common;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.TextboxScreen;
import com.mystic.holographicrenders.gui.WidgetScreen;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HolographicRendersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(HolographicRenders.PROJECTOR_BLOCK_ENTITY, ProjectorBlockEntityRenderer::new);

        RenderDataProvider.registerDefaultProviders();

        ScreenRegistry.register(HolographicRenders.PROJECTOR_SCREEN_HANDLER, ProjectorScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ProjectorScreenPacket.UPDATE_ID, ProjectorScreenPacket::onClientUpdate);
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "render_packet"), (client, handler, buf, responseSender) -> {
            ItemStack stack = buf.readItemStack();
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setRenderer(ItemProjectionHandler.getDataProvider((ProjectorBlockEntity) blockEntity, stack), false);
                }
            });
        });
        Common.textScreenRunnable = (hand -> MinecraftClient.getInstance().setScreen(new TextboxScreen(hand)));
        Common.widgetScreenRunnable = (hand ->  MinecraftClient.getInstance().setScreen(new WidgetScreen(hand)));
    }
}
