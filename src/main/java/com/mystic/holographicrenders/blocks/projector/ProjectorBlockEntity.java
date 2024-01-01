package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.EmptyProvider;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float alpha = 1;
    private boolean lightEnabled = true;
    private  RenderDataProvider<?> renderer = EmptyProvider.INSTANCE;


    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY, pos, state);
    }

    public ItemStack getItem() {
        return inventory.get(0);
    }

    public void setItem(ItemStack stack) {
        inventory.set(0, stack);
        this.markDirty();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.markDirty();
    }

    public float getAlpha() {
        return alpha;
    }

    public void setLightEnabled(boolean shouldDrawLights) {
        this.lightEnabled = shouldDrawLights;
        this.markDirty();
    }

    public boolean lightsEnabled() {
        return lightEnabled;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(renderer, providerId);
        renderer.fromTag(tag, this);
        inventory.set(0, ItemStack.fromNbt(tag.getCompound("Stack")));
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.put("Stack", getItem().writeNbt(new NbtCompound()));
        renderer.toTag(tag, this);
        super.writeNbt(tag);
    }

    public void setRenderer(@NotNull RenderDataProvider<?> renderer, boolean sync) {
        this.renderer = renderer;
        if (sync) {
            this.markDirty();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 2);
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        var nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ProjectorScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.getPos());
    }
}
