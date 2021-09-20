package com.mystic.holographicrenders.mixin;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.item.EntityScannerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandMixin {

    @Shadow public abstract boolean isMarker();

    @Shadow protected abstract EquipmentSlot slotFromPosition(Vec3d vec3d);

    @Shadow protected abstract boolean isSlotDisabled(EquipmentSlot slot);

    @Shadow protected abstract boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand);

    @Shadow public abstract boolean shouldShowArms();

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    public void interactAt(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir){
        cir.cancel();
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() != HolographicRenders.ENTITY_SCANNER) {
            cir.setReturnValue(ActionResult.PASS);
            if (!this.isMarker() && itemStack.getItem() != Items.NAME_TAG) {
                if (player.isSpectator()) {
                 cir.setReturnValue(ActionResult.SUCCESS);
                } else if (player.world.isClient) {
                    cir.setReturnValue(ActionResult.CONSUME);
                } else {
                    EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
                    if (itemStack.isEmpty()) {
                        EquipmentSlot equipmentSlot2 = this.slotFromPosition(hitPos);
                        EquipmentSlot equipmentSlot3 = this.isSlotDisabled(equipmentSlot2) ? equipmentSlot : equipmentSlot2;
                        if (player.hasStackEquipped(equipmentSlot3) && this.equip(player, equipmentSlot3, itemStack, hand)) {
                            cir.setReturnValue(ActionResult.SUCCESS);
                        }
                    } else {
                        if (this.isSlotDisabled(equipmentSlot)) {
                            cir.setReturnValue(ActionResult.FAIL);
                        }

                        if (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !this.shouldShowArms()) {
                            cir.setReturnValue(ActionResult.FAIL);
                        }

                        if (this.equip(player, equipmentSlot, itemStack, hand)) {
                            cir.setReturnValue(ActionResult.SUCCESS);
                        }
                    }

                    cir.setReturnValue(ActionResult.PASS);
                }
            }
        } else {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
