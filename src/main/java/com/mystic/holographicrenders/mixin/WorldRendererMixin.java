package com.mystic.holographicrenders.mixin;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.item.AreaScannerItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", opcode = Opcodes.GETFIELD, ordinal = 1))
    public void drawAreaSelection(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final ClientPlayerEntity player = client.player;
        if (!(player.getMainHandStack().getItem() instanceof AreaScannerItem)) return;

        NbtCompound tag = player.getMainHandStack().getOrCreateNbt();

        if (!tag.contains("Pos1")) return;

        BlockPos origin = BlockPos.fromLong(tag.getLong("Pos1"));

        HitResult result = player.raycast(player.getAbilities().creativeMode ? 5.0F : 4.5F, 0, false);
        BlockPos size = tag.contains("Pos2") ? BlockPos.fromLong(tag.getLong("Pos2")) : (result instanceof BlockHitResult ? ((BlockHitResult) result).getBlockPos() : new BlockPos((int) result.getPos().x, (int) result.getPos().y, (int) result.getPos().z));
        size = size.subtract(origin);

        origin = origin.add(size.getX() < 0 ? 1 : 0, size.getY() < 0 ? 1 : 0, size.getZ() < 0 ? 1 : 0);
        size = size.add(size.getX() >= 0 ? 1 : -1, size.getY() >= 0 ? 1 : -1, size.getZ() >= 0 ? 1 : -1);

        matrices.push();

        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());
        matrices.translate(origin.getX() - camera.getPos().x, origin.getY() - camera.getPos().y, origin.getZ() - camera.getPos().z);
        WorldRenderer.drawBox(matrices, consumer, new Box(BlockPos.ORIGIN, size), 0.25f, 0.25f, 1, 1);

        matrices.pop();
    }

}
