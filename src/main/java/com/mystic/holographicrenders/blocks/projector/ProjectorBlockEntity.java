package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.client.TextboxScreenRoot;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable, NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private float alpha;
    private boolean lightEnabled = true;
    private int[] texture;

    @NotNull
    private RenderDataProvider<?> renderer = RenderDataProvider.EmptyProvider.INSTANCE;

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
    }

    public void setAlpha(float alpha){
        if(this.alpha == alpha) return;
        this.alpha = alpha;
        this.markDirty();
    }

    public float getAlpha(){
        return alpha;
    }

    public void setLightEnabled(boolean shouldDrawLights) {
        this.lightEnabled = shouldDrawLights;
        this.markDirty();
    }

    public boolean lightsEnabled() {
        return lightEnabled;
    }

    public void setRenderer(@NotNull RenderDataProvider<?> renderer, boolean sync) {
        this.renderer = renderer;
        if (sync) {
            this.markDirty();
        }
    }

    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        alpha = tag.getFloat("Alpha");
        lightEnabled = tag.getBoolean("Lights");
        texture = tag.getIntArray("Texture");

        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? RenderDataProvider.EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(renderer, providerId);
        renderer.fromTag(tag, this);

        Inventories.fromTag(tag, inventory);
    }

    public int[] makeIntArrayTexture() {
        int[] current = new int[0];
        current = Arrays.copyOf(current, current.length + 1);
        current[current.length - 1] = new TextboxScreenRoot().getTexture();
        System.out.println(Arrays.toString(current));
        return current;
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
    public CompoundTag toTag(CompoundTag tag) {
        tag.putFloat("Alpha", alpha);
        tag.putBoolean("Lights", lightEnabled);
        tag.putIntArray("Texture", makeIntArrayTexture());

        renderer.toTag(tag, this);

        Inventories.toTag(tag, inventory);
        return super.toTag(tag);
    }

    @Override
    public void markDirty() {
        setRenderer(ItemProjectionHandler.getDataProvider(this, inventory.get(0)), false);
        super.markDirty();
        if (!world.isClient()) {
            sync();
        }
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
}
