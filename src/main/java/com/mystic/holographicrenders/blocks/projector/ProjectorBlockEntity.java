package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable, ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float alpha = 1;
    private boolean lightEnabled = true;

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
    }

    public ItemStack getItem() {
        return inventory.get(0);
    }

    public void setItem(ItemStack stack) {
        inventory.set(0, stack);
        markDirty();
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
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        inventory.set(0, ItemStack.fromTag(tag.getCompound("Stack")));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.put("Stack", getItem().toTag(new CompoundTag()));
        return super.toTag(tag);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(getCachedState(), tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return toTag(tag);
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
