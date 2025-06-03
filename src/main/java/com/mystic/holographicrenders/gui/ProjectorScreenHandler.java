package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ProjectorScreenHandler extends AbstractContainerMenu {

    private final ProjectorBlockEntity blockEntity;

    public ProjectorScreenHandler(int syncId, Inventory playerInventory, ProjectorBlockEntity blockEntity) {

        this(syncId, playerInventory, PacketByteBufs.create().writeBlockPos(blockEntity.getBlockPos()));
    }

    public ProjectorScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(HolographicRenders.PROJECTOR_SCREEN_HANDLER.get(), syncId);
        this.blockEntity = (ProjectorBlockEntity) playerInventory.player.level().getBlockEntity(buffer.readBlockPos());

        this.addSlot(new Slot(blockEntity, 0, 80, 35));

        // The player inventory
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8+ l * 18, 84 + m * 18));
            }
        }

        // The player Hotbar
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < blockEntity.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, blockEntity.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, blockEntity.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    public boolean getLight() {
        return blockEntity.lightsEnabled();
    }

    public void setLight(boolean lights) {
        if (blockEntity.getLevel().isClientSide) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeBoolean(lights);
            ClientPlayNetworking.send(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "light_packet"), buf);
        } else {
            blockEntity.setLightEnabled(lights);
        }
    }

    public boolean getSpin() {
        return blockEntity.spinEnabled();
    }

    public void setSpin(boolean spin) {
        if (blockEntity.getLevel().isClientSide) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeBoolean(spin);
            ClientPlayNetworking.send(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "spin_packet"), buf);
        } else {
            blockEntity.setSpinEnabled(spin);
        }
    }

    public int getRotate() {
        return blockEntity.getRotation();
    }

    public void setRotate(int rotate) {
        if (blockEntity.getLevel().isClientSide) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeInt(rotate);
            ClientPlayNetworking.send(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "rotate_packet"), buf);
        } else {
            blockEntity.setRotation(rotate);
        }
    }

}
