package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.function.Function;

public abstract class RenderDataProvider<T> {

    protected T data;

    protected RenderDataProvider(T data) {
        this.data = data;
    }

    @Environment(EnvType.CLIENT)
    public abstract void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be);

    public void toTag(CompoundTag tag, ProjectorBlockEntity be) {
        tag.putString("RendererType", getTypeId().toString());
        tag.put("RenderData", write(be));
    }

    public void fromTag(CompoundTag tag, ProjectorBlockEntity be) {
        read(tag.getCompound("RenderData"), be);
    }

    public static void registerDefaultProviders() {
        RenderDataProviderRegistry.register(ItemProvider.ID, () -> new ItemProvider(ItemStack.EMPTY));
        RenderDataProviderRegistry.register(BlockProvider.ID, () -> new BlockProvider(Blocks.AIR.getDefaultState()));
        RenderDataProviderRegistry.register(EntityProvider.ID, () -> new EntityProvider(null));
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(new Pair<>(BlockPos.ORIGIN, BlockPos.ORIGIN)));
        RenderDataProviderRegistry.register(EmptyProvider.ID, () -> EmptyProvider.INSTANCE);
        RenderDataProviderRegistry.register(TextProvider.ID, () -> new TextProvider(""));
    }

    protected abstract CompoundTag write(ProjectorBlockEntity be);

    protected abstract void read(CompoundTag tag, ProjectorBlockEntity be);

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
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            //matrices.translate(0, , 0);
            matrices.translate(0.5, 1.15, 0.5); //TODO make this usable with translation sliders
            matrices.scale(0.0f, 0.0f, 0.0f); //TODO make this usable with scaling sliders
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            MinecraftClient.getInstance().getItemRenderer().renderItem(data, ModelTransformation.Mode.GROUND, light, overlay, matrices, immediate);

        }

        @Override
        public CompoundTag write(ProjectorBlockEntity be) {
            CompoundTag tag = new CompoundTag();
            CompoundTag itemTag = new CompoundTag();
            data.toTag(itemTag);
            tag.put("Item", itemTag);
            return tag;
        }

        @Override
        public void read(CompoundTag tag, ProjectorBlockEntity be) {
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
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            matrices.translate(0.5, 1.15, 0.5); //TODO make this usable with translation sliders
            matrices.scale(0.5f, 0.5f, 0.5f); //TODO make this usable with scaling sliders

            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            matrices.translate(-0.5, 0, -0.5);

            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(data, matrices, immediate, light, overlay);
        }

        @Override
        public CompoundTag write(ProjectorBlockEntity be) {
            final CompoundTag tag = new CompoundTag();
            tag.putString("BlockId", Registry.BLOCK.getId(data.getBlock()).toString());
            return tag;
        }

        @Override
        public void read(CompoundTag tag, ProjectorBlockEntity be) {
            data = Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("BlockId"))).orElse(Blocks.AIR).getDefaultState();
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class EntityProvider extends RenderDataProvider<Entity> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "entity");
        private CompoundTag entityTag = null;

        protected EntityProvider(Entity data) {
            super(data);
            if(data == null) return;
            entityTag = new CompoundTag();
            data.saveSelfToTag(entityTag);
        }

        public static EntityProvider from(Entity entity) {
            return new EntityProvider(entity);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            if (!tryLoadEntity(MinecraftClient.getInstance().world)) return;
            
            matrices.translate(0.5, 1.15, 0.5); //TODO make this usable with translation sliders
            matrices.scale(0.5f, 0.5f, 0.5f); //TODO make this usable with scaling sliders

            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            final EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
            entityRenderDispatcher.setRenderShadows(false);
            entityRenderDispatcher.render(data, 0, 0, 0, 0, 0, matrices, immediate, light);
            entityRenderDispatcher.setRenderShadows(true);
        }

        private boolean tryLoadEntity(World world){
            if(data != null) return true;
            if(world == null) return false;
            data = EntityType.loadEntityWithPassengers(entityTag, world, Function.identity());
            return data != null;
        }

        @Override
        public CompoundTag write(ProjectorBlockEntity be) {
            CompoundTag tag = new CompoundTag();
            tag.put("Entity", entityTag);
            return tag;
        }

        @Override
        public void read(CompoundTag tag, ProjectorBlockEntity be) {
            entityTag = tag.getCompound("Entity");
            data = null;
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class TextProvider extends RenderDataProvider<String> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "text");

        public TextProvider(String data) {
            super(data);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            matrices.translate(0.5, 0.0, 0.5);

            PlayerEntity closestPlayer = MinecraftClient.getInstance().player;
            if(closestPlayer != null)
            {
                double x = closestPlayer.getX() - be.getPos().getX() - 0.5;
                double z = closestPlayer.getZ() - be.getPos().getZ() - 0.5;
                float rot = (float) MathHelper.atan2(z, x);
                matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-rot));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(80));
            }

            matrices.scale(0.1f, -0.1f, 0.1f); //TODO make this usable with scaling sliders
            matrices.translate(-(MinecraftClient.getInstance().textRenderer.getWidth(data) / 2f), -20, -0.0); //TODO make this usable with translation sliders
            MinecraftClient.getInstance().textRenderer.draw(matrices, data, 0, 0, 0);
        }

        @Override
        protected CompoundTag write(ProjectorBlockEntity be) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Text", data);
            return compoundTag;
        }

        @Override
        protected void read(CompoundTag tag, ProjectorBlockEntity be) {
            data = tag.getString("Text");
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
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
            if (!cacheValid) loadCache();

            matrices.push();

            matrices.translate(0.5, 1, 0.5);
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
            matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders
            matrices.translate(-cache[0][0].length / 2f, 0, -cache[0].length / 2f); //TODO make this usable with translation sliders

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

        @Environment(EnvType.CLIENT)
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

        @Environment(EnvType.CLIENT)
        public void invalidateCache() {
            cacheValid = false;
        }

        @Override
        public CompoundTag write(ProjectorBlockEntity be) {
            final CompoundTag tag = new CompoundTag();

            tag.putLong("Start", data.getLeft().asLong());
            tag.putLong("End", data.getRight().asLong());

            return tag;
        }

        @Override
        public void read(CompoundTag tag, ProjectorBlockEntity be) {
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
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

        }

        @Override
        protected CompoundTag write(ProjectorBlockEntity be) {
            return new CompoundTag();
        }

        @Override
        protected void read(CompoundTag tag, ProjectorBlockEntity be) {

        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

}
