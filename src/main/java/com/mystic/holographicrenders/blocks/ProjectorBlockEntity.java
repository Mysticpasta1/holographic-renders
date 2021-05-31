package com.mystic.holographicrenders.blocks;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import com.mystic.holographicrenders.gui.HologramScreenHandler;
import com.mystic.holographicrenders.gui.ImplementedInventory;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable, NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    @NotNull
    private RenderDataProvider<?> renderer = RenderDataProvider.EmptyProvider.INSTANCE;

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
    }

    public void setRenderer(@NotNull RenderDataProvider<?> renderer, boolean sync) {
        this.renderer = renderer;
        if(sync) {
            this.markDirty();
        }
    }

    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? RenderDataProvider.EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(providerId);
        renderer.fromTag(tag, this);

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
        renderer.toTag(tag, this);
        return super.toTag(tag);
    }

    @Override
    public void markDirty() {
        final ItemStack itemStack = inventory.get(0);

        if (itemStack.getItem() instanceof BlockItem) {
            setRenderer(RenderDataProvider.BlockProvider.from(((BlockItem) itemStack.getItem()).getBlock().getDefaultState()), false);
        } else if (itemStack.getItem() instanceof SpawnEggItem) {
            EntityType<?> type = ((SpawnEggItem) itemStack.getItem()).getEntityType(itemStack.getTag());
            Entity entity = type.create(getWorld());
            entity.updatePosition(getPos().getX(), getPos().getY(), getPos().getZ());
            setRenderer(RenderDataProvider.EntityProvider.from(entity), false);
        } else if (itemStack.getItem() == HolographicRenders.AREA_RENDER_ITEM) {
            CompoundTag tag = itemStack.getOrCreateTag();
            if(tag.contains("Pos1") && tag.contains("Pos2")){
                BlockPos pos1 = BlockPos.fromLong(tag.getLong("Pos1"));
                BlockPos pos2 = BlockPos.fromLong(tag.getLong("Pos2"));
                setRenderer(RenderDataProvider.AreaProvider.from(pos1, pos2), false);
            } else {
                setRenderer(RenderDataProvider.EmptyProvider.INSTANCE, false);
            }
        } else if (itemStack.getItem() == Items.NAME_TAG) {
            setRenderer(new RenderDataProvider.TextProvider(itemStack.getName().asString()), false);
        } else {
            setRenderer(RenderDataProvider.ItemProvider.from(itemStack), false);
        }
        super.markDirty();
        if(!world.isClient()){
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
        return new HologramScreenHandler(syncId, playerInventory, this);
    }
}
