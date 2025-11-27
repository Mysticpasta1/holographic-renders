package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TextProvider extends RenderDataProvider<Component> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "text");
    private static ProjectorBlockEntity entity;

    protected TextProvider(Component data) {
        super(data);
    }

    public static TextProvider from(Component text) {
        return new TextProvider(text);
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        TextProvider.entity = entity;
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        drawText(matrices, be, 0, data, immediate);
    }

    public static void drawText(PoseStack matrices, BlockEntity be, int color, Component text, MultiBufferSource.BufferSource immediate) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        matrices.translate(0.5, 0.0, 0.5);

        RenderSystem.setShaderColor(1, 1, 1, entity.getAlpha());

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            final Direction facing = ProjectorBlock.getFacing(be.getBlockState());

            double side1;
            double side2;

            switch (facing.getAxis()) {
                case X -> {
                    side1 = player.getY() - be.getBlockPos().getY() - 0.5;
                    side2 = player.getZ() - be.getBlockPos().getZ() - 0.5;
                }
                case Z -> {
                    side1 = player.getX() - be.getBlockPos().getX() - 0.5;
                    side2 = player.getY() - be.getBlockPos().getY() - 0.5;
                }
                default -> {
                    side1 = player.getX() - be.getBlockPos().getX() - 0.5;
                    side2 = player.getZ() - be.getBlockPos().getZ() - 0.5;
                }
            }

            float rot = (float) Mth.atan2(side2, side1);
            rot *= facing == Direction.UP ? -1 : 1;
            rot *= facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() : 1;
            rot *= facing.getAxis() == Direction.Axis.X ? facing.getStepX() : 1;
            matrices.mulPose(Axis.YP.rotation(rot));
            matrices.mulPose(Axis.YP.rotationDegrees(90 * (facing == Direction.EAST ? -1 : 1)));
        }

        matrices.scale(0.05f, -0.05f, 0.05f);
        matrices.translate(-(Minecraft.getInstance().font.width(text) / 2f), -20, 0);

        Minecraft.getInstance().font.drawInBatch(
                text.getString(),
                0,
                0,
                color,
                false,
                matrices.last().pose(),
                immediate,
                Font.DisplayMode.SEE_THROUGH,
                0,
                0xf000f0,
                false
        );
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    protected CompoundTag write(ProjectorBlockEntity be) {
        CompoundTag tag = new CompoundTag();
        String json = Component.Serializer.toJson(this.data, be.getLevel().registryAccess());
        tag.putString("Text", json);
        return tag;
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {
        String json = tag.getString("Text");
        Component component = Component.Serializer.fromJson(json, be.getLevel().registryAccess());
        this.data = component != null ? component : Component.empty();
    }



    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
