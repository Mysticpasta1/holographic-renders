package com.mystic.holographicrenders.network;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ProjectorScreenPacket {

    public static final Identifier UPDATE_ID = new Identifier(HolographicRenders.MOD_ID, "update_projector_screen");
    public static final Identifier ACTION_REQUEST_ID = new Identifier(HolographicRenders.MOD_ID, "projector_screen_action_request");

    public static Packet<?> createUpdate(boolean lights) {
        PacketByteBuf buffer = PacketByteBufs.create();

        buffer.writeBoolean(lights);

        return ServerPlayNetworking.createS2CPacket(UPDATE_ID, buffer);
    }

    public static Packet<?> createLightAction(boolean light) {
        PacketByteBuf buffer = PacketByteBufs.create();

        buffer.writeVarInt(ActionRequestType.SET_LIGHT.ordinal());
        buffer.writeBoolean(light);

        return ClientPlayNetworking.createC2SPacket(ACTION_REQUEST_ID, buffer);
    }

    public static void onClientUpdate(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        boolean lights = packetByteBuf.readBoolean();

        minecraftClient.execute(() -> {
            if (minecraftClient.currentScreen instanceof ProjectorScreen) {
                ((ProjectorScreen) minecraftClient.currentScreen).setLights(lights);
            }
        });
    }

    public static void onActionRequest(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        if (!(serverPlayerEntity.currentScreenHandler instanceof ProjectorScreenHandler)) return;
        ProjectorScreenHandler handler = (ProjectorScreenHandler) serverPlayerEntity.currentScreenHandler;

        ActionRequestType type = ActionRequestType.values()[packetByteBuf.readVarInt()];

        switch (type) {
            case SET_LIGHT:
                boolean lights = packetByteBuf.readBoolean();
                minecraftServer.execute(() -> handler.setLight(lights));
                break;
        }

    }

    private enum ActionRequestType {
        SET_LIGHT
    }
}
