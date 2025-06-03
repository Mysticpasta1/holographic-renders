package com.mystic.holographicrenders.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mystic.holographicrenders.item.AreaScannerItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", opcode = Opcodes.GETFIELD, ordinal = 1))
    public void drawAreaSelection(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final Minecraft client = Minecraft.getInstance();
        final LocalPlayer player = client.player;
        if (!(player.getMainHandItem().getItem() instanceof AreaScannerItem)) return;

        CompoundTag tag = player.getMainHandItem().getOrCreateTag();

        if (!tag.contains("Pos1")) return;

        BlockPos origin = BlockPos.of(tag.getLong("Pos1"));

        HitResult result = player.pick(player.getAbilities().instabuild ? 5.0F : 4.5F, 0, false);
        BlockPos size = tag.contains("Pos2") ? BlockPos.of(tag.getLong("Pos2")) : (result instanceof BlockHitResult ? ((BlockHitResult) result).getBlockPos() : new BlockPos((int) result.getLocation().x, (int) result.getLocation().y, (int) result.getLocation().z));
        size = size.subtract(origin);

        origin = origin.offset(size.getX() < 0 ? 1 : 0, size.getY() < 0 ? 1 : 0, size.getZ() < 0 ? 1 : 0);
        size = size.offset(size.getX() >= 0 ? 1 : -1, size.getY() >= 0 ? 1 : -1, size.getZ() >= 0 ? 1 : -1);

        matrices.pushPose();

        VertexConsumer consumer = client.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        matrices.translate(origin.getX() - camera.getPosition().x, origin.getY() - camera.getPosition().y, origin.getZ() - camera.getPosition().z);
        LevelRenderer.renderLineBox(matrices, consumer, new AABB(BlockPos.ZERO, size), 0.25f, 0.25f, 1, 1);

        matrices.popPose();
    }

}
