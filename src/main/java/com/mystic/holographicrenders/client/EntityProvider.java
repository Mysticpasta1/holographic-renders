package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Function;

public class EntityProvider extends RenderDataProvider<Entity> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "entity");
    private static ProjectorBlockEntity entity;
    private CompoundTag entityTag = null;

    protected EntityProvider(Entity data) {
        super(data);
        if (data == null) return;
        entityTag = new CompoundTag();
        data.saveAsPassenger(entityTag);
    }

    private <T extends Entity> void renderEntity(T entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int light) {
        EntityRenderer<? super T> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        entityRenderer.render(entity, entity.getViewYRot(partialTicks), partialTicks, stack, bufferSource, light);
    }

    public static com.mystic.holographicrenders.client.EntityProvider from(Entity entity) {
        return new com.mystic.holographicrenders.client.EntityProvider(entity);
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        EntityProvider.entity = entity;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1,1,1, entity.getAlpha());
        if (!tryLoadEntity(Minecraft.getInstance().level)) return;
        matrices.translate(0.5, 0.75, 0.5);
        matrices.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));
        matrices.scale(0.5f, 0.5f, 0.5f);
        final EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadow(false);
        renderEntity(data,0, matrices, immediate, light);
        entityRenderDispatcher.setRenderShadow(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    private boolean tryLoadEntity(Level world) {
        if (data != null) return true;
        if (world == null) return false;
        data = EntityType.loadEntityRecursive(entityTag, world, Function.identity());
        return data != null;
    }

    @Override
    public CompoundTag write(ProjectorBlockEntity be) {
        CompoundTag tag = new CompoundTag();
        tag.put("Entity", entityTag);
        return tag;
    }

    @Override
    public void read(CompoundTag tag, ProjectorBlockEntity be) {
        entityTag = tag.getCompound("Entity");
        data = null;
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
