package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EmptyProvider extends RenderDataProvider<Void> {

    public static com.mystic.holographicrenders.client.EmptyProvider INSTANCE = new com.mystic.holographicrenders.client.EmptyProvider();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "empty");

    private EmptyProvider() {
        super(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {}

    @Override
    protected CompoundTag write(ProjectorBlockEntity be) {
        return new CompoundTag();
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {

    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
