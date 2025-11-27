package com.mystic.holographicrenders.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mystic.holographicrenders.item.AreaScannerItem;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 1
            )
    )
    private void drawAreaSelection(DeltaTracker deltaTracker,
                                   boolean renderBlockOutline,
                                   Camera camera,
                                   GameRenderer gameRenderer,
                                   LightTexture lightTexture,
                                   Matrix4f modelViewMatrix,
                                   Matrix4f projectionMatrix,
                                   CallbackInfo ci) {

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        if (!(player.getMainHandItem().getItem() instanceof AreaScannerItem)) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AreaScannerItem)) return;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty() || !data.contains("Pos1")) return;

        CompoundTag tag = data.copyTag();

        BlockPos origin = BlockPos.of(tag.getLong("Pos1"));

        HitResult result = player.pick(player.getAbilities().instabuild ? 5.0F : 4.5F, 0, false);

        BlockPos size = tag.contains("Pos2")
                ? BlockPos.of(tag.getLong("Pos2"))
                : (result instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos()
                : new BlockPos(
                (int) result.getLocation().x,
                (int) result.getLocation().y,
                (int) result.getLocation().z
        ));

        size = size.subtract(origin);

        origin = origin.offset(
                size.getX() < 0 ? 1 : 0,
                size.getY() < 0 ? 1 : 0,
                size.getZ() < 0 ? 1 : 0
        );
        size = size.offset(
                size.getX() >= 0 ? 1 : -1,
                size.getY() >= 0 ? 1 : -1,
                size.getZ() >= 0 ? 1 : -1
        );

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        VertexConsumer consumer = client.renderBuffers()
                .bufferSource()
                .getBuffer(RenderType.lines());

        var camPos = camera.getPosition();
        poseStack.translate(
                origin.getX() - camPos.x,
                origin.getY() - camPos.y,
                origin.getZ() - camPos.z
        );

        LevelRenderer.renderLineBox(
                poseStack,
                consumer,
                new AABB(new Vec3(0, 0, 0), new Vec3(size.getX(), size.getY(), size.getZ())),
                0.25f, 0.25f, 1.0f, 1.0f
        );

        poseStack.popPose();
    }
}
