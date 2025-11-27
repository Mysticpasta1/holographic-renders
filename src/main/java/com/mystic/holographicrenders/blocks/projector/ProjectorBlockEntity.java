package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.EmptyProvider;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectorBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private float alpha = 1f;
    private boolean lightEnabled = true;
    private boolean spinEnabled = true;
    private RenderDataProvider<?> renderer = EmptyProvider.INSTANCE;
    private int rotation;

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    public ItemStack getItem() {
        return inventory.get(0);
    }

    public void setItemStack(ItemStack stack) {
        inventory.set(0, stack);
        setChanged();
    }

    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
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

    public int getRotation() {
        return rotation;
    }

    public boolean spinEnabled() {
        return spinEnabled;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        spinEnabled = tag.getBoolean("Spin");
        rotation = tag.getInt("Rotate");

        ResourceLocation providerId = ResourceLocation.tryParse(tag.getString("RendererType"));
        renderer = providerId == null
                ? EmptyProvider.INSTANCE
                : RenderDataProviderRegistry.getProvider(renderer, providerId);

        renderer.fromTag(tag, this);

        if (tag.contains("Stack")) {
            inventory.set(0, ItemStack.parseOptional(registries, tag.getCompound("Stack")));
        } else {
            inventory.set(0, ItemStack.EMPTY);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.putBoolean("Spin", spinEnabled);
        tag.putInt("Rotate", rotation);

        tag.putString("RendererType", renderer.getTypeId().toString());
        renderer.toTag(tag, this);

        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            tag.put("Stack", stack.save(registries));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt, registries);
        return nbt;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ProjectorScreenHandler(syncId, playerInventory, this);
    }
}
