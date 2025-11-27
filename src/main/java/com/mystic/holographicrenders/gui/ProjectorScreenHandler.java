package com.mystic.holographicrenders.gui;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.network.ProjectorPackets;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ProjectorScreenHandler extends AbstractContainerMenu {

    private final ProjectorBlockEntity blockEntity;

    private static FriendlyByteBuf writePos(BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        return buf;
    }

    public ProjectorScreenHandler(int syncId, Inventory playerInventory, ProjectorBlockEntity blockEntity) {
        this(syncId, playerInventory, writePos(blockEntity.getBlockPos()));
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
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeBoolean(lights);
            ProjectorPackets.sendLightChange(blockEntity.getBlockPos(), lights);
        } else {
            blockEntity.setLightEnabled(lights);
        }
    }

    public boolean getSpin() {
        return blockEntity.spinEnabled();
    }

    public void setSpin(boolean spin) {
        if (blockEntity.getLevel().isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeBoolean(spin);
            ProjectorPackets.sendSpinChange(blockEntity.getBlockPos(), spin);
        } else {
            blockEntity.setSpinEnabled(spin);
        }
    }

    public int getRotate() {
        return blockEntity.getRotation();
    }

    public void setRotate(int rotate) {
        if (blockEntity.getLevel().isClientSide) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(blockEntity.getBlockPos());
            buf.writeInt(rotate);
            ProjectorPackets.sendRotateChange(blockEntity.getBlockPos(), rotate);
        } else {
            blockEntity.setRotation(rotate);
        }
    }

}
