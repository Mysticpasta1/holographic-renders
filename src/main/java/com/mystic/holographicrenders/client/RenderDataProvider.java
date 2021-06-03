package com.mystic.holographicrenders.client;

import com.glisco.worldmesher.WorldMesh;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
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
        RenderDataProviderRegistry.register(TextProvider.ID, () -> new TextProvider(Text.of("")));
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

            matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
            //matrices.scale(0.0f, 0.0f, 0.0f); //TODO make this usable with scaling sliders
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

            matrices.translate(0.5, 0.75, 0.5); //TODO make this usable with translation sliders
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
            if (data == null) return;
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

            matrices.translate(0.5, 0.75, 0.5);
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));
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

    public static class TextProvider extends RenderDataProvider<Text> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "text");

        protected TextProvider(Text data) {
            super(data);
        }

        public static TextProvider from(Text text) {
            return new TextProvider(text);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
            drawText(matrices, be, 0, data);
        }

        public static void drawText(MatrixStack matrices, BlockEntity be, int color, Text text){
            matrices.translate(0.5, 0.0, 0.5);

            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                final Direction facing = ProjectorBlock.getFacing(be.getCachedState());

                double side1;
                double side2;

                switch (facing.getAxis()){
                    case X:
                        side1 = player.getY() - be.getPos().getY() - 0.5;
                        side2 = player.getZ() - be.getPos().getZ() - 0.5;
                        break;
                    case Z:
                        side1 = player.getX() - be.getPos().getX() - 0.5;
                        side2 = player.getY() - be.getPos().getY() - 0.5;
                        break;
                    default:
                        side1 = player.getX() - be.getPos().getX() - 0.5;
                        side2 = player.getZ() - be.getPos().getZ() - 0.5;
                        break;
                }

                float rot = (float) MathHelper.atan2(side2, side1);
                rot *= facing == Direction.UP ? -1 : 1;
                rot *= facing.getAxis() == Direction.Axis.Z ? facing.getOffsetZ() : 1;
                rot *= facing.getAxis() == Direction.Axis.X ? facing.getOffsetX() : 1;
                matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(rot));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(80 * (facing == Direction.EAST ? -1 : 1)));
            }

            matrices.scale(0.05f, -0.05f, 0.05f); //TODO make this usable with scaling sliders
            matrices.translate(-(MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f), -20, 0); //TODO make this usable with translation sliders

            MinecraftClient.getInstance().textRenderer.draw(matrices, text, 0, 0, color);
        }

        @Override
        protected CompoundTag write(ProjectorBlockEntity be) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Text", Text.Serializer.toJson(data));
            return compoundTag;
        }

        @Override
        protected void read(CompoundTag tag, ProjectorBlockEntity be) {
            data = Text.Serializer.fromJson(tag.getString("Text"));
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "area");

        private WorldMesh mesh;

        protected AreaProvider(Pair<BlockPos, BlockPos> data) {
            super(data);
            mesh = new WorldMesh.Builder(MinecraftClient.getInstance().world, data.getLeft(), data.getRight()).build();
        }

        public static AreaProvider from(BlockPos start, BlockPos end) {
            return new AreaProvider(new Pair<>(start, end));
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
            if (!mesh.isBuilt()) {
                mesh.scheduleRebuild();
                matrices.translate(0.5, 0, 0.5);
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.translate(-0.5, 0, -0.5);
                matrices.translate(0, 0.65, 0);
                TextProvider.drawText(matrices, be, 0, Text.of("§b[§aScanning§b]"));
            } else {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
                matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders

                int xSize = 1 + Math.max(data.getLeft().getX(), data.getRight().getX()) - Math.min(data.getLeft().getX(), data.getRight().getX());
                int zSize = 1 + Math.max(data.getLeft().getZ(), data.getRight().getZ()) - Math.min(data.getLeft().getZ(), data.getRight().getZ());

                matrices.translate(-xSize / 2f, 0, -zSize / 2f); //TODO make this usable with translation sliders

                mesh.render(matrices.peek().getModel());
            }
        }

        @Environment(EnvType.CLIENT)
        public void invalidateCache() {
            mesh = new WorldMesh.Builder(MinecraftClient.getInstance().world, data.getLeft(), data.getRight()).renderActions(() -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.CONSTANT_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
                RenderSystem.blendColor(1, 1, 1, 0.6f);
            }, () -> {
                RenderSystem.blendColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }).build();
            mesh.scheduleRebuild();
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
