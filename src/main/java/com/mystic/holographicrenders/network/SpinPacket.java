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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class SpinPacket {

    public static final Identifier UPDATE_ID = new Identifier(HolographicRenders.MOD_ID, "update_spin");
    public static final Identifier ACTION_REQUEST_ID = new Identifier(HolographicRenders.MOD_ID, "update_spin_action");
    public static Packet<?> createUpdate(boolean spin) {
        PacketByteBuf buffer = PacketByteBufs.create();

        buffer.writeBoolean(spin);

        return ServerPlayNetworking.createS2CPacket(UPDATE_ID, buffer);
    }

    public static Packet<?> createSpinAction(boolean spin) {
        PacketByteBuf buffer = PacketByteBufs.create();

        buffer.writeVarInt(ActionRequestType.SET_SPIN.ordinal());
        buffer.writeBoolean(spin);

        return ClientPlayNetworking.createC2SPacket(ACTION_REQUEST_ID, buffer);
    }

    public static void onClientUpdate(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        boolean spin = packetByteBuf.readBoolean();

        minecraftClient.execute(() -> {
            if (minecraftClient.currentScreen instanceof ProjectorScreen) {
                ((ProjectorScreen) minecraftClient.currentScreen).setSpin(spin);
            }
        });
    }

    public static void onActionRequest(MinecraftServer minecraftServer, ServerPlayerEntity serverPlayerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {

        if (!(serverPlayerEntity.currentScreenHandler instanceof ProjectorScreenHandler handler)) return;

        ActionRequestType type = ActionRequestType.values()[packetByteBuf.readVarInt()];

        if (Objects.requireNonNull(type) == ActionRequestType.SET_SPIN) {
            boolean spin = packetByteBuf.readBoolean();
            minecraftServer.execute(() -> handler.setSpin(spin));
        }
    }
}
