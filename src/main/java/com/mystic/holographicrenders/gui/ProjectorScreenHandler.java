package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class ProjectorScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public ProjectorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    public void setShouldDrawLights2(boolean shouldDrawLights){
        ProjectorBlockEntity projectorBlockEntity = (ProjectorBlockEntity) inventory;
        projectorBlockEntity.setShouldDrawLights(shouldDrawLights);
    }

    public ProjectorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(HolographicRenders.HOLOGRAM_SCREEN_HANDLER, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;

        this.addSlot(new Slot(inventory, 0, 80, 35));

        int m;
        int l;
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }
}
