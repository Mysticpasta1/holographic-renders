package com.mystic.holographicrenders.network;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import java.util.Objects;

public class RotatePacket {

    public static final ResourceLocation UPDATE_ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_rotate");
    public static final ResourceLocation ACTION_REQUEST_ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_rotate_action");
    public static Packet<?> createUpdate(int rotate) {
        FriendlyByteBuf buffer = PacketByteBufs.create();

        buffer.writeInt(rotate);

        return ServerPlayNetworking.createS2CPacket(UPDATE_ID, buffer);
    }

    public static Packet<?> createRotateAction(int rotate) {
        FriendlyByteBuf buffer = PacketByteBufs.create();

        buffer.writeVarInt(ActionRequestType.SET_ROTATE.ordinal());
        buffer.writeInt(rotate);

        return ClientPlayNetworking.createC2SPacket(ACTION_REQUEST_ID, buffer);
    }

    public static void onClientUpdate(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {

        int rotate = packetByteBuf.readInt();

        minecraftClient.execute(() -> {
            if (minecraftClient.screen instanceof ProjectorScreen) {
                ((ProjectorScreen) minecraftClient.screen).setRotationInt(rotate);
            }
        });
    }

    public static void onActionRequest(MinecraftServer minecraftServer, ServerPlayer serverPlayerEntity, ServerGamePacketListenerImpl serverPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {

        if (!(serverPlayerEntity.containerMenu instanceof ProjectorScreenHandler handler)) return;

        ActionRequestType type = ActionRequestType.values()[packetByteBuf.readVarInt()];

        if (Objects.requireNonNull(type) == ActionRequestType.SET_ROTATE) {
            int rotate = packetByteBuf.readInt();
            minecraftServer.execute(() -> handler.setRotate(rotate));
        }
    }
}
