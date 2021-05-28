package com.mystic.holographicrenders.blocks;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.client.RenderDataProviderRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ProjectorBlockEntity extends BlockEntity implements BlockEntityClientSerializable {

    @NotNull
    private RenderDataProvider<?> renderer = RenderDataProvider.EmptyProvider.INSTANCE;

    public ProjectorBlockEntity() {
        super(HolographicRenders.PROJECTOR_BLOCK_ENTITY);
    }

    public void setRenderer(@NotNull RenderDataProvider<?> renderer) {
        this.renderer = renderer;
        this.markDirty();
    }

    public @NotNull RenderDataProvider<?> getRenderer() {
        return renderer;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        Identifier providerId = Identifier.tryParse(tag.getString("RendererType"));
        renderer = providerId == null ? RenderDataProvider.EmptyProvider.INSTANCE : RenderDataProviderRegistry.getProvider(providerId);
        renderer.fromTag(tag);

    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        renderer.toTag(tag);
        return super.toTag(tag);
    }

    @Override
    public void markDirty() {
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
}
