package com.mystic.holographicrenders.mixin;

import com.mystic.holographicrenders.HolographicRenders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", opcode = Opcodes.GETFIELD, ordinal = 1))
    public void drawAreaSelection(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final ClientPlayerEntity player = client.player;
        if (player.getMainHandStack().getItem() != HolographicRenders.AREA_SCANNER) return;


        CompoundTag tag = player.getMainHandStack().getOrCreateTag();

        if (!tag.contains("Pos1")) return;

        BlockPos origin = BlockPos.fromLong(tag.getLong("Pos1"));
        BlockPos target = tag.contains("Pos2") ? BlockPos.fromLong(tag.getLong("Pos2")) : new BlockPos(player.raycast(player.abilities.creativeMode ? 5.0F : 4.5F, 0, false).getPos());

        matrices.push();

        VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getLines());

        matrices.translate(-origin.getX() - camera.getPos().x, -origin.getY() - camera.getPos().y, -origin.getZ() - camera.getPos().z);
        System.out.println("draw");
        WorldRenderer.drawBox(matrices, consumer, new Box(0, 0, 0, 5, 5, 5), 1, 1, 1, 1);

        matrices.pop();
    }

}
