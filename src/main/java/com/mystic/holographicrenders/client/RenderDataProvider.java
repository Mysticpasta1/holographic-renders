package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(new Pair<>(BlockPos.ORIGIN, BlockPos.ORIGIN)));
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

            if (data == null) return;

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
            if (MinecraftClient.getInstance().world == null) return;
            data = EntityType.loadEntityWithPassengers(tag.getCompound("Entity"), MinecraftClient.getInstance().world, Function.identity());
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "area");

        private BlockState[][][] cache;
        private boolean cacheValid = false;

        protected AreaProvider(Pair<BlockPos, BlockPos> data) {
            super(data);
        }

        public static AreaProvider from(BlockPos start, BlockPos end) {
            return new AreaProvider(new Pair<>(start, end));
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay) {
            if (!cacheValid) loadCache();

            matrices.push();

            matrices.translate(0.5, 1, 0.5);
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));
            matrices.scale(0.075f, 0.075f, 0.075f);
            matrices.translate(-cache[0][0].length / 2f, 0, -cache[0].length / 2f);

            BlockPos origin = new BlockPos(Math.min(data.getLeft().getX(), data.getRight().getX()), Math.min(data.getLeft().getY(), data.getRight().getY()), Math.min(data.getLeft().getZ(), data.getRight().getZ()));

            int y = 0;
            int x;
            int z;

            for (BlockState[][] twoDim : cache) {
                matrices.push();
                z = 0;
                for (BlockState[] oneDim : twoDim) {
                    matrices.push();
                    x = 0;
                    for (BlockState state : oneDim) {

                        matrices.push();

                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.EAST, x != cache[0][0].length - 1);
                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.WEST, x != 0);
                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.SOUTH, z != cache[0].length - 1);
                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.NORTH, z != 0);
                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.UP, y != cache.length - 1);
                        ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.DOWN, y != 0);

                        ProjectorBlockEntityRenderer.blockModelRenderer.render(MinecraftClient.getInstance().world, MinecraftClient.getInstance().getBlockRenderManager().getModel(state), state, origin.add(x, y, z), matrices, immediate.getBuffer(RenderLayers.getBlockLayer(state)), true, MinecraftClient.getInstance().world.random, state.getRenderingSeed(origin.add(x, y, z)), overlay);

                        matrices.pop();
                        x++;
                        matrices.translate(1, 0, 0);
                    }
                    matrices.pop();
                    z++;
                    matrices.translate(0, 0, 1);
                }
                matrices.pop();
                y++;
                matrices.translate(0, 1, 0);
            }

            matrices.pop();
        }

        private void loadCache() {
            final ClientWorld world = MinecraftClient.getInstance().world;

            BlockPos pos1 = data.getLeft();
            BlockPos pos2 = data.getRight();

            BlockPos start = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos end = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));

            int xDiff = end.getX() - start.getX();
            int yDiff = end.getY() - start.getY();
            int zDiff = end.getZ() - start.getZ();

            BlockState[][][] states = new BlockState[yDiff + 1][zDiff + 1][xDiff + 1];

            for (int y = 0; y <= yDiff; y++) {
                for (int z = 0; z <= zDiff; z++) {
                    for (int x = 0; x <= xDiff; x++) {
                        states[y][z][x] = world.getBlockState(start.add(x, y, z));
                    }
                }
            }

            cache = states;
            cacheValid = true;
        }

        public void invalidateCache() {
            cacheValid = false;
        }

        @Override
        public CompoundTag write() {
            final CompoundTag tag = new CompoundTag();

            tag.putLong("Start", data.getLeft().asLong());
            tag.putLong("End", data.getRight().asLong());

            return tag;
        }

        @Override
        public void read(CompoundTag tag) {
            BlockPos start = BlockPos.fromLong(tag.getLong("Start"));
            BlockPos end = BlockPos.fromLong(tag.getLong("End"));

            this.data = new Pair<>(start, end);

            invalidateCache();
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
