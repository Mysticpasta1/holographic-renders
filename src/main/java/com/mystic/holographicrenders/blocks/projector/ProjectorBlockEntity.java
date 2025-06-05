package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.EmptyProvider;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private float alpha = 1f;
    private boolean lightEnabled = true;
    private boolean spinEnabled = true;
    private  RenderDataProvider<?> renderer = EmptyProvider.INSTANCE;
    private int rotation;


    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getItem() {
        return inventory.get(0);
    }

    public void setItem(ItemStack stack) {
        inventory.set(0, stack);
        this.setChanged();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.setChanged();
    }

    public float getAlpha() {
        return alpha;
    }

    public void setLightEnabled(boolean shouldDrawLights) {
        this.lightEnabled = shouldDrawLights;
        this.setChanged();
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        this.setChanged();
    }

    public void setSpinEnabled(boolean shouldSpin) {
        this.spinEnabled = shouldSpin;
        this.setChanged();
    }

    public boolean lightsEnabled() {
        return lightEnabled;
    }

    public int getRotation() {return rotation;}
    public boolean spinEnabled() {
        return spinEnabled;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        spinEnabled = tag.getBoolean("Spin");
        rotation = tag.getInt("Rotate");
        ResourceLocation providerId = ResourceLocation.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(renderer, providerId);
        renderer.fromTag(tag, this);
        inventory.set(0, ItemStack.of(tag.getCompound("Stack")));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.putBoolean("Spin", spinEnabled);
        tag.putInt("Rotate", rotation);
        tag.put("Stack", getItem().save(new CompoundTag()));
        renderer.toTag(tag, this);
        super.saveAdditional(tag);
    }

    public void setRenderer(@NotNull RenderDataProvider<?> renderer, boolean sync) {
        this.renderer = renderer;
        if (sync) {
            this.setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        var nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ProjectorScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
    }
}
