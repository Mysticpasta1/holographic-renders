package com.mystic.holographicrenders.network;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreen;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.TextureScannerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = HolographicRenders.MOD_ID)
public final class ProjectorPackets {

    private ProjectorPackets() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(HolographicRenders.MOD_ID);
        registrar.playToClient(UpdateLight.TYPE, UpdateLight.STREAM_CODEC, UpdateLight::handle);
        registrar.playToClient(UpdateSpin.TYPE, UpdateSpin.STREAM_CODEC, UpdateSpin::handle);
        registrar.playToClient(UpdateRotate.TYPE, UpdateRotate.STREAM_CODEC, UpdateRotate::handle);
        registrar.playToServer(SetLight.TYPE, SetLight.STREAM_CODEC, SetLight::handle);
        registrar.playToServer(SetSpin.TYPE, SetSpin.STREAM_CODEC, SetSpin::handle);
        registrar.playToServer(SetRotate.TYPE, SetRotate.STREAM_CODEC, SetRotate::handle);
        registrar.playToServer(BlockLightChange.TYPE, BlockLightChange.STREAM_CODEC, BlockLightChange::handle);
        registrar.playToServer(BlockSpinChange.TYPE, BlockSpinChange.STREAM_CODEC, BlockSpinChange::handle);
        registrar.playToServer(BlockRotateChange.TYPE, BlockRotateChange.STREAM_CODEC, BlockRotateChange::handle);
        registrar.playToServer(UrlToItem.TYPE, UrlToItem.STREAM_CODEC, UrlToItem::handle);
    }

    public record UpdateLight(boolean lights) implements CustomPacketPayload {

        public static final Type<UpdateLight> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_light"));

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLight> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> buf.writeBoolean(payload.lights),
                        buf -> new UpdateLight(buf.readBoolean())
                );

        @Override
        public Type<UpdateLight> type() {
            return TYPE;
        }

        public static void handle(UpdateLight payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof ProjectorScreen screen) {
                    screen.setLights(payload.lights());
                }
            });
        }
    }

    public record UpdateSpin(boolean spin) implements CustomPacketPayload {

        public static final Type<UpdateSpin> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_spin"));

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSpin> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> buf.writeBoolean(payload.spin),
                        buf -> new UpdateSpin(buf.readBoolean())
                );

        @Override
        public Type<UpdateSpin> type() {
            return TYPE;
        }

        public static void handle(UpdateSpin payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof ProjectorScreen screen) {
                    screen.setSpin(payload.spin());
                }
            });
        }
    }

    public record UpdateRotate(int rotation) implements CustomPacketPayload {

        public static final Type<UpdateRotate> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_rotate"));

        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRotate> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> buf.writeInt(payload.rotation),
                        buf -> new UpdateRotate(buf.readInt())
                );

        @Override
        public Type<UpdateRotate> type() {
            return TYPE;
        }

        public static void handle(UpdateRotate payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof ProjectorScreen screen) {
                    screen.setRotationInt(payload.rotation());
                }
            });
        }
    }

    public record SetLight(boolean lights) implements CustomPacketPayload {

        public static final Type<SetLight> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_light_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetLight> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeVarInt(ActionRequestType.SET_LIGHT.ordinal());
                            buf.writeBoolean(payload.lights);
                        },
                        buf -> {
                            buf.readVarInt();
                            boolean lights = buf.readBoolean();
                            return new SetLight(lights);
                        }
                );

        @Override
        public Type<SetLight> type() {
            return TYPE;
        }

        public static void handle(SetLight payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                if (!(player.containerMenu instanceof ProjectorScreenHandler handler)) return;

                handler.setLight(payload.lights());
            });
        }
    }

    public record SetSpin(boolean spin) implements CustomPacketPayload {

        public static final Type<SetSpin> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_spin_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetSpin> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeVarInt(ActionRequestType.SET_SPIN.ordinal());
                            buf.writeBoolean(payload.spin);
                        },
                        buf -> {
                            buf.readVarInt();
                            boolean spin = buf.readBoolean();
                            return new SetSpin(spin);
                        }
                );

        @Override
        public Type<SetSpin> type() {
            return TYPE;
        }

        public static void handle(SetSpin payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                if (!(player.containerMenu instanceof ProjectorScreenHandler handler)) return;

                handler.setSpin(payload.spin());
            });
        }
    }

    public record SetRotate(int rotation) implements CustomPacketPayload {

        public static final Type<SetRotate> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "update_rotate_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetRotate> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeVarInt(ActionRequestType.SET_ROTATE.ordinal());
                            buf.writeInt(payload.rotation);
                        },
                        buf -> {
                            buf.readVarInt();
                            int rot = buf.readInt();
                            return new SetRotate(rot);
                        }
                );

        @Override
        public Type<SetRotate> type() {
            return TYPE;
        }

        public static void handle(SetRotate payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                if (!(player.containerMenu instanceof ProjectorScreenHandler handler)) return;

                handler.setRotate(payload.rotation());
            });
        }
    }

    public record BlockLightChange(BlockPos pos, boolean lights) implements CustomPacketPayload {

        public static final Type<BlockLightChange> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "light_packet"));

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockLightChange> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeBoolean(payload.lights);
                        },
                        buf -> {
                            BlockPos pos = buf.readBlockPos();
                            boolean lights = buf.readBoolean();
                            return new BlockLightChange(pos, lights);
                        }
                );

        @Override
        public Type<BlockLightChange> type() {
            return TYPE;
        }

        public static void handle(BlockLightChange payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof ProjectorBlockEntity projector) {
                    projector.setLightEnabled(payload.lights());
                }
            });
        }
    }

    public record BlockSpinChange(BlockPos pos, boolean spin) implements CustomPacketPayload {

        public static final Type<BlockSpinChange> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "spin_packet"));

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockSpinChange> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeBoolean(payload.spin);
                        },
                        buf -> {
                            BlockPos pos = buf.readBlockPos();
                            boolean spin = buf.readBoolean();
                            return new BlockSpinChange(pos, spin);
                        }
                );

        @Override
        public Type<BlockSpinChange> type() {
            return TYPE;
        }

        public static void handle(BlockSpinChange payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof ProjectorBlockEntity projector) {
                    projector.setSpinEnabled(payload.spin());
                }
            });
        }
    }

    public record BlockRotateChange(BlockPos pos, int rotation) implements CustomPacketPayload {

        public static final Type<BlockRotateChange> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "rotate_packet"));

        public static final StreamCodec<RegistryFriendlyByteBuf, BlockRotateChange> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeBlockPos(payload.pos);
                            buf.writeInt(payload.rotation);
                        },
                        buf -> {
                            BlockPos pos = buf.readBlockPos();
                            int rotation = buf.readInt();
                            return new BlockRotateChange(pos, rotation);
                        }
                );

        @Override
        public Type<BlockRotateChange> type() {
            return TYPE;
        }

        public static void handle(BlockRotateChange payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                BlockEntity be = player.level().getBlockEntity(payload.pos());
                if (be instanceof ProjectorBlockEntity projector) {
                    projector.setRotation(payload.rotation());
                }
            });
        }
    }

    public record UrlToItem(String url, InteractionHand hand) implements CustomPacketPayload {

        public static final Type<UrlToItem> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "url_packet"));

        public static final StreamCodec<RegistryFriendlyByteBuf, UrlToItem> STREAM_CODEC =
                StreamCodec.of(
                        (buf, payload) -> {
                            buf.writeUtf(payload.url, 2000);
                            buf.writeEnum(payload.hand);
                        },
                        buf -> new UrlToItem(buf.readUtf(2000), buf.readEnum(InteractionHand.class))
                );

        @Override
        public Type<UrlToItem> type() {
            return TYPE;
        }

        public static void handle(UrlToItem payload, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) ctx.player();
                ItemStack stack = player.getItemInHand(payload.hand());
                if (!(stack.getItem() instanceof TextureScannerItem)) return;
                CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> nbt.putString("URL", payload.url()));
            });
        }
    }

    public static void sendLightChange(BlockPos pos, boolean lights) {
        PacketDistributor.sendToServer(new BlockLightChange(pos, lights));
    }

    public static void sendSpinChange(BlockPos pos, boolean spin) {
        PacketDistributor.sendToServer(new BlockSpinChange(pos, spin));
    }

    public static void sendRotateChange(BlockPos pos, int rotation) {
        PacketDistributor.sendToServer(new BlockRotateChange(pos, rotation));
    }

    public static void sendSetLightToServer(boolean lights) {
        PacketDistributor.sendToServer(new SetLight(lights));
    }

    public static void sendSetSpinToServer(boolean spin) {
        PacketDistributor.sendToServer(new SetSpin(spin));
    }

    public static void sendSetRotateToServer(int rotation) {
        PacketDistributor.sendToServer(new SetRotate(rotation));
    }
}
