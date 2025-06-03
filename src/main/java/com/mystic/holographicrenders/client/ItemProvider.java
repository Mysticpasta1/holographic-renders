package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemProvider extends RenderDataProvider<ItemStack> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "item");

    protected ItemProvider(ItemStack data) {
        super(data);
    }

    public static com.mystic.holographicrenders.client.ItemProvider from(ItemStack stack) {
        return new com.mystic.holographicrenders.client.ItemProvider(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
        //matrices.scale(0.0f, 0.0f, 0.0f); //TODO make this usable with scaling sliders
        matrices.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));

        Minecraft.getInstance().getItemRenderer().renderStatic(data, ItemDisplayContext.GROUND, light, overlay, matrices, immediate, null, 0); //TODO: FIX
    }

    @Override
    public CompoundTag write(ProjectorBlockEntity be) {
        CompoundTag tag = new CompoundTag();
        CompoundTag itemTag = new CompoundTag();
        data.save(itemTag);
        tag.put("Item", itemTag);
        return tag;
    }

    @Override
    public void read(CompoundTag tag, ProjectorBlockEntity be) {
        data = ItemStack.of(tag.getCompound("Item"));
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
