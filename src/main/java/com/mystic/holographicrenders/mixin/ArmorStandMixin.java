package com.mystic.holographicrenders.mixin;

import com.mystic.holographicrenders.item.EntityScannerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {

    @Shadow protected abstract boolean isDisabled(EquipmentSlot slot);

    @Shadow protected abstract boolean swapItem(Player player, EquipmentSlot slot, ItemStack stack, InteractionHand hand);

    @Shadow public abstract boolean isShowArms();

    @Shadow protected abstract EquipmentSlot getClickedSlot(Vec3 hitPos);

    /**
     * @author Mysticpasta1
     * @reason Inject was failing for some reason
     */
    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    public void interactAt(Player player, Vec3 hitPos, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        cir.cancel();
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(itemStack.getItem() instanceof EntityScannerItem)) {
            if (!((ArmorStand) (Object) this).isMarker() && itemStack.getItem() != Items.NAME_TAG) {
                if (player.isSpectator()) {
                    cir.setReturnValue(InteractionResult.SUCCESS);
                } else if (player.level().isClientSide) {
                    cir.setReturnValue(InteractionResult.CONSUME);
                } else {
                    EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
                    if (itemStack.isEmpty()) {
                        EquipmentSlot equipmentSlot2 = this.getClickedSlot(hitPos);
                        EquipmentSlot equipmentSlot3 = this.isDisabled(equipmentSlot2) ? equipmentSlot : equipmentSlot2;
                        if (player.hasItemInSlot(equipmentSlot3) && this.swapItem(player, equipmentSlot3, itemStack, hand)) {
                            cir.setReturnValue(InteractionResult.SUCCESS);
                        }
                    } else {
                        if (this.isDisabled(equipmentSlot)) {
                            cir.setReturnValue(InteractionResult.FAIL);
                        }

                        if (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !this.isShowArms()) {
                            cir.setReturnValue(InteractionResult.FAIL);
                        }

                        if (this.swapItem(player, equipmentSlot, itemStack, hand)) {
                            cir.setReturnValue(InteractionResult.SUCCESS);
                        }
                    }
                    cir.setReturnValue(InteractionResult.PASS);
                }
            }
        }
        cir.setReturnValue(InteractionResult.PASS);
    }
}
