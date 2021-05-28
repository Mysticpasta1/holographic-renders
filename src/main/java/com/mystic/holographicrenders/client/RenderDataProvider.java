package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

public abstract class RenderDataProvider<T> {

    protected T data;

    protected RenderDataProvider(T data) {
        this.data = data;
    }

    public abstract void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay);

    public void toTag(CompoundTag tag) {
        tag.putString("RendererType", getTypeId().toString());
        tag.put("RenderData", write());
    }

    public void fromTag(CompoundTag tag) {
        read(tag.getCompound("RenderData"));
    }

    public static void registerDefaultProviders() {
        RenderDataProviderRegistry.register(ItemProvider.ID, () -> new ItemProvider(ItemStack.EMPTY));
        RenderDataProviderRegistry.register(BlockProvider.ID, () -> new BlockProvider(Blocks.AIR.getDefaultState()));
        RenderDataProviderRegistry.register(EntityProvider.ID, () -> new EntityProvider(null));
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(null));
        RenderDataProviderRegistry.register(EmptyProvider.ID, () -> EmptyProvider.INSTANCE);
    }

    protected abstract CompoundTag write();

    protected abstract void read(CompoundTag tag);

    public abstract Identifier getTypeId();

    public static class ItemProvider extends RenderDataProvider<ItemStack> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "item");

        protected ItemProvider(ItemStack data) {
            super(data);
        }

        public static ItemProvider from(ItemStack stack) {
            return new ItemProvider(stack);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {

            matrices.translate(0, 1.15, 0);
            matrices.translate(0.5, 0, 0.5);
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            MinecraftClient.getInstance().getItemRenderer().renderItem(data, ModelTransformation.Mode.GROUND, light, overlay, matrices, immediate);

        }

        @Override
        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            CompoundTag itemTag = new CompoundTag();
            data.toTag(itemTag);
            tag.put("Item", itemTag);
            return tag;
        }

        @Override
        public void read(CompoundTag tag) {
            data = ItemStack.fromTag(tag.getCompound("Item"));
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class BlockProvider extends RenderDataProvider<BlockState> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "block");

        protected BlockProvider(BlockState data) {
            super(data);
        }

        public static BlockProvider from(BlockState state) {
            return new BlockProvider(state);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {
            matrices.translate(0, 1.15, 0);

            matrices.translate(0.5, 0, 0.5);
            matrices.scale(0.5f, 0.5f, 0.5f);

            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            matrices.translate(-0.5, 0, -0.5);

            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(data, matrices, immediate, light, overlay);
        }

        @Override
        public CompoundTag write() {
            final CompoundTag tag = new CompoundTag();
            tag.putString("BlockId", Registry.BLOCK.getId(data.getBlock()).toString());
            return tag;
        }

        @Override
        public void read(CompoundTag tag) {
            data = Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("BlockId"))).orElse(Blocks.AIR).getDefaultState();
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class EntityProvider extends RenderDataProvider<Entity> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "entity");

        protected EntityProvider(Entity data) {
            super(data);
        }

        public static EntityProvider from(Entity entity) {
            return new EntityProvider(entity);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {
            matrices.translate(0, 1.15, 0);

            matrices.translate(0.5, 0, 0.5);
            matrices.scale(0.5f, 0.5f, 0.5f);

            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            final EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
            entityRenderDispatcher.setRenderShadows(false);
            entityRenderDispatcher.render(data, 0, 0, 0, 0, 0, matrices, immediate, light);
            entityRenderDispatcher.setRenderShadows(true);
        }

        @Override
        public CompoundTag write() {
            final CompoundTag tag = new CompoundTag();
            final CompoundTag entityTag = new CompoundTag();
            if (data == null) {
                System.err.println("Entity is null!");
                return tag;
            }
            data.saveSelfToTag(entityTag);
            tag.put("Entity", entityTag);
            return tag;
        }

        @Override
        public void read(CompoundTag tag) {
            data = EntityType.loadEntityWithPassengers(tag.getCompound("Entity"), MinecraftClient.getInstance().world, Function.identity());
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "area");

        protected AreaProvider(Pair<BlockPos, BlockPos> data) {
            super(data);
        }

        public static AreaProvider from(BlockPos start, BlockPos end) {
            return new AreaProvider(new Pair<>(start, end));
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {

        }

        @Override
        public CompoundTag write() {
            return new CompoundTag();
        }

        @Override
        public void read(CompoundTag tag) {

        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class EmptyProvider extends RenderDataProvider<Void> {

        public static EmptyProvider INSTANCE = new EmptyProvider();

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "empty");

        private EmptyProvider() {
            super(null);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {

        }

        @Override
        protected CompoundTag write() {
            return new CompoundTag();
        }

        @Override
        protected void read(CompoundTag tag) {

        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

}
