package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable, ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float alpha = 1;
    private boolean lightEnabled = true;
    private  RenderDataProvider<?> renderer = RenderDataProvider.EmptyProvider.INSTANCE;


    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
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
    public void fromTag(BlockState state, NbtCompound tag) {
        super.fromTag(state, tag);
        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? RenderDataProvider.EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(renderer, providerId);
        renderer.fromNbt(tag, this);
        inventory.set(0, ItemStack.fromNbt(tag.getCompound("Stack")));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.put("Stack", getItem().writeNbt(new NbtCompound()));
        renderer.toNbt(tag, this);
        return super.writeNbt(tag);
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
            sync();
        }
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
    public void fromClientTag(NbtCompound tag) {
        fromTag(getCachedState(), tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
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
