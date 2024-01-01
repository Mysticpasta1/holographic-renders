package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

import java.util.function.Function;

public class EntityProvider extends RenderDataProvider<Entity> {

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "entity");
    private NbtCompound entityTag = null;

    protected EntityProvider(Entity data) {
        super(data);
        if (data == null) return;
        entityTag = new NbtCompound();
        data.saveSelfNbt(entityTag);
    }

    public static com.mystic.holographicrenders.client.EntityProvider from(Entity entity) {
        return new com.mystic.holographicrenders.client.EntityProvider(entity);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        if (!tryLoadEntity(MinecraftClient.getInstance().world)) return;

        matrices.translate(0.5, 0.75, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (System.currentTimeMillis() / 60d % 360d)));
        matrices.scale(0.5f, 0.5f, 0.5f); //TODO make this usable with scaling sliders

        final EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setRenderShadows(false);
        entityRenderDispatcher.render(data, 0, 0, 0, 0, 0, matrices, immediate, light);
        entityRenderDispatcher.setRenderShadows(true);
    }

    private boolean tryLoadEntity(World world) {
        if (data != null) return true;
        if (world == null) return false;
        data = EntityType.loadEntityWithPassengers(entityTag, world, Function.identity());
        return data != null;
    }

    @Override
    public NbtCompound write(ProjectorBlockEntity be) {
        NbtCompound tag = new NbtCompound();
        tag.put("Entity", entityTag);
        return tag;
    }

    @Override
    public void read(NbtCompound tag, ProjectorBlockEntity be) {
        entityTag = tag.getCompound("Entity");
        data = null;
    }

    @Override
    public Identifier getTypeId() {
        return ID;
    }
}
