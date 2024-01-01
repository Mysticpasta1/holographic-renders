package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class BlockProvider extends RenderDataProvider<BlockState> {

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "block");

    protected BlockProvider(BlockState data) {
        super(data);
    }

    public static com.mystic.holographicrenders.client.BlockProvider from(BlockState state) {
        return new com.mystic.holographicrenders.client.BlockProvider(state);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
        matrices.scale(0.5f, 0.5f, 0.5f); //TODO make this usable with scaling sliders

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));

        matrices.translate(-0.5, 0, -0.5);

        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(data, matrices, immediate, light, overlay);
    }

    @Override
    public NbtCompound write(ProjectorBlockEntity be) {
        final NbtCompound tag = new NbtCompound();
        tag.putString("BlockId", Registries.BLOCK.getId(data.getBlock()).toString());
        return tag;
    }

    @Override
    public void read(NbtCompound tag, ProjectorBlockEntity be) {
        data = Registries.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("BlockId"))).orElse(Blocks.AIR).getDefaultState();
    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
