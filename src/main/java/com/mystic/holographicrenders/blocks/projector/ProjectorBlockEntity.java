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
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable, ExtendedScreenHandlerFactory, ImplementedInventory, Tickable {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    protected int alpha;
    protected long lastCheck = -1; //Not Saved
    private boolean shouldDrawLights = true;

    @NotNull
    private RenderDataProvider<?> renderer = RenderDataProvider.EmptyProvider.INSTANCE;

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
    }

    public int setAlpha(int alpha) {
        this.alpha = alpha;
        return alpha;
    }

    @Override
    public void tick() {
        if(world == null || world.isClient) return;

        if(world.getTime() == lastCheck) return;
        lastCheck = world.getTime();

        BlockState state = getCachedState();
        Direction facing = state.get(ProjectorBlock.PROPERTY_FACING);
        BlockPos posToCheck = pos.offset(facing);
        int alpha = world.getReceivedRedstonePower(posToCheck);
        setAlpha(alpha);
        markDirty();

        BlockPos reversePos = pos.offset(facing.getOpposite());
        world.updateNeighbor(reversePos, state.getBlock(), pos);
        world.updateNeighborsExcept(reversePos, state.getBlock(), facing);

    }

    public void setShouldDrawLights(boolean shouldDrawLights, boolean sync){
        this.shouldDrawLights = shouldDrawLights;
        if (sync) {
            this.markDirty();
        }
    }

    public boolean shouldDrawLights() {
        return shouldDrawLights;
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
        alpha = tag.getInt("Alpha");
        shouldDrawLights = tag.getBoolean("Lights");
        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? RenderDataProvider.EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(providerId);
        renderer.fromTag(tag, this);
        Inventories.fromTag(tag, inventory);
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
        tag.putInt("Alpha", alpha);
        tag.putBoolean("Lights", shouldDrawLights());
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

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBoolean(shouldDrawLights());
    }
}
