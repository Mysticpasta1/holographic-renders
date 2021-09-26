package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ProjectorScreenHandler extends ScreenHandler {

    private final ProjectorBlockEntity blockEntity;

    public ProjectorScreenHandler(int syncId, PlayerInventory playerInventory, ProjectorBlockEntity blockEntity) {

        this(syncId, playerInventory, PacketByteBufs.create().writeBlockPos(blockEntity.getPos()));
    }

    public ProjectorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buffer) {
        super(HolographicRenders.PROJECTOR_SCREEN_HANDLER, syncId);
        this.blockEntity = (ProjectorBlockEntity) playerInventory.player.world.getBlockEntity(buffer.readBlockPos());

        this.addSlot(new Slot(blockEntity, 0, 80, 35));

        // The player inventory
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        // The player Hotbar
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity.canPlayerUse(player);
    }

    public void setLight(boolean lights) {
        if (blockEntity.getWorld().isClient) {
            // TODO: Sync it
        } else {
            blockEntity.setLightEnabled(lights);
        }
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < blockEntity.size()) {
                if (!this.insertItem(originalStack, blockEntity.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, blockEntity.size(), false)) {
                return ItemStack.EMPTY;
            }
            slot.markDirty();
        }

        return newStack;
    }
}
