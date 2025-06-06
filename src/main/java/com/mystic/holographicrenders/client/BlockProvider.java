package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class BlockProvider extends RenderDataProvider<BlockState> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "block");
    private static ProjectorBlockEntity entity;

    protected BlockProvider(BlockState data) {
        super(data);
    }

    public static com.mystic.holographicrenders.client.BlockProvider from(BlockState state) {
        return new com.mystic.holographicrenders.client.BlockProvider(state);
    }

    private <T extends BlockEntity> void renderBlockEntity(T entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int overlay, int light) {
        BlockEntityRenderer<T> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        if (!(entity instanceof ProjectorBlockEntity)) {
            if(blockEntityRenderer != null) {
                blockEntityRenderer.render(entity, partialTicks, stack, bufferSource, light, overlay);
            }
        }
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        BlockProvider.entity = entity;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, entity.getAlpha());

        matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
        matrices.scale(0.5f, 0.5f, 0.5f); //TODO make this usable with scaling sliders

        matrices.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));

        matrices.translate(-0.5, 0, -0.5);

        RenderType type = ItemBlockRenderTypes.getRenderType(data, true);

        if (data.getBlock() instanceof BaseEntityBlock baseEntityBlock) {
            BlockEntity blockEntity = baseEntityBlock.newBlockEntity(BlockPos.ZERO, data);
            if (blockEntity != null) {
                blockEntity.setLevel(Minecraft.getInstance().level);
                renderBlockEntity(blockEntity, 0, matrices, immediate, overlay, light);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(data, matrices, immediate, light, overlay, ModelData.builder().build(), type);
            }
        } else {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(data, matrices, immediate, light, overlay, ModelData.builder().build(), type);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }

    @Override
    public CompoundTag write(ProjectorBlockEntity be) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("BlockId", BuiltInRegistries.BLOCK.getKey(data.getBlock()).toString());
        return tag;
    }

    @Override
    public void read(CompoundTag tag, ProjectorBlockEntity be) {
        data = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(tag.getString("BlockId"))).orElse(Blocks.AIR).defaultBlockState();
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
