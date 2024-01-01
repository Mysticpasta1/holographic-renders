package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ItemProvider extends RenderDataProvider<ItemStack> {

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "item");

    protected ItemProvider(ItemStack data) {
        super(data);
    }

    public static com.mystic.holographicrenders.client.ItemProvider from(ItemStack stack) {
        return new com.mystic.holographicrenders.client.ItemProvider(stack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
        //matrices.scale(0.0f, 0.0f, 0.0f); //TODO make this usable with scaling sliders
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));

        MinecraftClient.getInstance().getItemRenderer().renderItem(data, ModelTransformationMode.GROUND, light, overlay, matrices, immediate, null, 0); //TODO: FIX
    }

    @Override
    public NbtCompound write(ProjectorBlockEntity be) {
        NbtCompound tag = new NbtCompound();
        NbtCompound itemTag = new NbtCompound();
        data.writeNbt(itemTag);
        tag.put("Item", itemTag);
        return tag;
    }

    @Override
    public void read(NbtCompound tag, ProjectorBlockEntity be) {
        data = ItemStack.fromNbt(tag.getCompound("Item"));
    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
