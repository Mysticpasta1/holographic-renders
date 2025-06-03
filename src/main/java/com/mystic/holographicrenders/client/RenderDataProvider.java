package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.net.MalformedURLException;

/**
 * @param <T>
 */
public abstract class RenderDataProvider<T> {
    protected T data;

    protected RenderDataProvider(T data) {
        this.data = data;
    }

    @Environment(EnvType.CLIENT)
    public abstract void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException;

    public void toTag(CompoundTag tag, ProjectorBlockEntity be) {
        tag.putString("RendererType", getTypeId().toString());
        tag.put("RenderData", write(be));
    }

    public void fromTag(CompoundTag tag, ProjectorBlockEntity be) {
        read(tag.getCompound("RenderData"), be);
    }

    public static void registerDefaultProviders() {
        RenderDataProviderRegistry.register(ItemProvider.ID, () -> new ItemProvider(ItemStack.EMPTY));
        RenderDataProviderRegistry.register(BlockProvider.ID, () -> new BlockProvider(Blocks.AIR.defaultBlockState()));
        RenderDataProviderRegistry.register(EntityProvider.ID, () -> new EntityProvider(null));
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(Pair.of(BlockPos.ZERO, BlockPos.ZERO)));
        RenderDataProviderRegistry.register(EmptyProvider.ID, () -> EmptyProvider.INSTANCE);
        RenderDataProviderRegistry.register(TextProvider.ID, () -> new TextProvider(Component.nullToEmpty("")));
        RenderDataProviderRegistry.register(TextureProvider.ID, () -> {
            ResourceLocation id = ResourceLocation.withDefaultNamespace("missingno");
            return new TextureProvider(id, new RegularSprite(id, 16,16));
        });
        RenderDataProviderRegistry.register(MapProvider.ID, () -> new MapProvider(-1));
    }

    protected abstract CompoundTag write(ProjectorBlockEntity be);

    protected abstract void read(CompoundTag tag, ProjectorBlockEntity be);

    public abstract ResourceLocation getTypeId();

}
