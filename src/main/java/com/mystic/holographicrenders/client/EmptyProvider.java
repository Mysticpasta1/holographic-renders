package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class EmptyProvider extends RenderDataProvider<Void> {

    public static com.mystic.holographicrenders.client.EmptyProvider INSTANCE = new com.mystic.holographicrenders.client.EmptyProvider();

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "empty");

    private EmptyProvider() {
        super(null);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

    }

    @Override
    protected NbtCompound write(ProjectorBlockEntity be) {
        return new NbtCompound();
    }

    @Override
    protected void read(NbtCompound tag, ProjectorBlockEntity be) {

    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
