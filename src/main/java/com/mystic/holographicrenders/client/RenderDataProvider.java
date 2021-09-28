package com.mystic.holographicrenders.client;

import com.glisco.worldmesher.WorldMesh;
import com.glisco.worldmesher.internals.WorldMesher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
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
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import com.sun.jna.platform.unix.solaris.LibKstat;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @param <T>
 */
public abstract class RenderDataProvider<T> {

    protected T data;

    protected RenderDataProvider(T data) {
        this.data = data;
    }

    @Environment(EnvType.CLIENT)
    public abstract void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException;

    public void toTag(NbtCompound tag, ProjectorBlockEntity be) {
        tag.putString("RendererType", getTypeId().toString());
        tag.put("RenderData", write(be));
    }

    public void fromTag(NbtCompound tag, ProjectorBlockEntity be) {
        read(tag.getCompound("RenderData"), be);
    }

    public static void registerDefaultProviders() {
        RenderDataProviderRegistry.register(ItemProvider.ID, () -> new ItemProvider(ItemStack.EMPTY));
        RenderDataProviderRegistry.register(BlockProvider.ID, () -> new BlockProvider(Blocks.AIR.getDefaultState()));
        RenderDataProviderRegistry.register(EntityProvider.ID, () -> new EntityProvider(null));
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(Pair.of(BlockPos.ORIGIN, BlockPos.ORIGIN)));
        RenderDataProviderRegistry.register(EmptyProvider.ID, () -> EmptyProvider.INSTANCE);
        RenderDataProviderRegistry.register(TextProvider.ID, () -> new TextProvider(Text.of("")));
        RenderDataProviderRegistry.register(TextureProvider.ID, () -> new TextureProvider(new Identifier("missingno")));
    }

    protected abstract NbtCompound write(ProjectorBlockEntity be);

    protected abstract void read(NbtCompound tag, ProjectorBlockEntity be);

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
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            MinecraftClient.getInstance().getItemRenderer().renderItem(data, ModelTransformation.Mode.GROUND, light, overlay, matrices, immediate, 0);
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

            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));

            matrices.translate(-0.5, 0, -0.5);

            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(data, matrices, immediate, light, overlay);
        }

        @Override
        public NbtCompound write(ProjectorBlockEntity be) {
            final NbtCompound tag = new NbtCompound();
            tag.putString("BlockId", Registry.BLOCK.getId(data.getBlock()).toString());
            return tag;
        }

        @Override
        public void read(NbtCompound tag, ProjectorBlockEntity be) {
            data = Registry.BLOCK.getOrEmpty(Identifier.tryParse(tag.getString("BlockId"))).orElse(Blocks.AIR).getDefaultState();
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class EntityProvider extends RenderDataProvider<Entity> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "entity");
        private NbtCompound entityTag = null;

        protected EntityProvider(Entity data) {
            super(data);
            if (data == null) return;
            entityTag = new NbtCompound();
            data.saveSelfNbt(entityTag);
        }

        public static EntityProvider from(Entity entity) {
            return new EntityProvider(entity);
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            if (!tryLoadEntity(MinecraftClient.getInstance().world)) return;

            matrices.translate(0.5, 0.75, 0.5);
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d)));
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

        public static void drawText(MatrixStack matrices, BlockEntity be, int color, Text text) {
            matrices.translate(0.5, 0.0, 0.5);

            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                final Direction facing = ProjectorBlock.getFacing(be.getCachedState());

                double side1;
                double side2;

                switch (facing.getAxis()) {
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
                matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rot));
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90 * (facing == Direction.EAST ? -1 : 1)));
            }

            matrices.scale(0.05f, -0.05f, 0.05f); //TODO make this usable with scaling sliders
            matrices.translate(-(MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f), -20, 0); //TODO make this usable with translation sliders

            MinecraftClient.getInstance().textRenderer.draw(matrices, text, 0, 0, color);
        }

        @Override
        protected NbtCompound write(ProjectorBlockEntity be) {
            NbtCompound NbtCompound = new NbtCompound();
            NbtCompound.putString("Text", Text.Serializer.toJson(data));
            return NbtCompound;
        }

        @Override
        protected void read(NbtCompound tag, ProjectorBlockEntity be) {
            data = Text.Serializer.fromJson(tag.getString("Text"));
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class AreaProvider extends RenderDataProvider<Pair<BlockPos, BlockPos>> {
        private static LoadingCache<Pair<BlockPos, BlockPos>, AreaProvider> cache = CacheBuilder.newBuilder()
                .maximumSize(20)
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .build(new CacheLoader<org.apache.commons.lang3.tuple.Pair<BlockPos, BlockPos>, AreaProvider>() {
                    @Override
                    public AreaProvider load(Pair<BlockPos, BlockPos> key) {
                        return new AreaProvider(key);
                    }
                });

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "area");

        private final MinecraftClient client;
        private long lastUpdateTick;
        private WorldMesh mesh;

        protected AreaProvider(Pair<BlockPos, BlockPos> data) {
            super(data);
            this.client = MinecraftClient.getInstance();
            //TODO fix this argh
            this.lastUpdateTick = this.client.world.getTime();
            invalidateCache();
        }

        public static AreaProvider from(BlockPos start, BlockPos end) throws ExecutionException {
            return cache.get(Pair.of(start, end));
        }

        @Override
        @Environment(EnvType.CLIENT)
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            if (client.world.getTime() - lastUpdateTick > 1200) {
                lastUpdateTick = client.world.getTime();
                mesh.scheduleRebuild();
            }

            if (!mesh.canRender()) {
                matrices.translate(0.5, 0, 0.5);
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.translate(-0.5, 0, -0.5);
                matrices.translate(0, 0.65, 0);
                TextProvider.drawText(matrices, be, 0, Text.of("§b[§aScanning§b]"));
            } else {
                matrices.translate(0.5, 0.5, 0.5);
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) (System.currentTimeMillis() / 60d % 360d))); //Rotate Speed
                matrices.scale(0.075f, 0.075f, 0.075f); //TODO make this usable with scaling sliders

                int xSize = 1 + Math.max(data.getLeft().getX(), data.getRight().getX()) - Math.min(data.getLeft().getX(), data.getRight().getX());
                int zSize = 1 + Math.max(data.getLeft().getZ(), data.getRight().getZ()) - Math.min(data.getLeft().getZ(), data.getRight().getZ());

                matrices.translate(-xSize / 2f, 0, -zSize / 2f); //TODO make this usable with translation sliders

                mesh.render(matrices);
            }
        }

        @Environment(EnvType.CLIENT)
        public void invalidateCache() {
            mesh = new WorldMesh.Builder(MinecraftClient.getInstance().world, data.getLeft(), data.getRight())
                    .renderActions(HologramRenderLayer.beginAction, HologramRenderLayer.endAction)
                    .build();
            rebuild();
        }

        @Environment(EnvType.CLIENT)
        public void rebuild() {
            mesh.scheduleRebuild();
        }

        @Override
        public NbtCompound write(ProjectorBlockEntity be) {
            final NbtCompound tag = new NbtCompound();

            tag.putLong("Start", data.getLeft().asLong());
            tag.putLong("End", data.getRight().asLong());

            return tag;
        }

        @Override
        public void read(NbtCompound tag, ProjectorBlockEntity be) {
            BlockPos start = BlockPos.fromLong(tag.getLong("Start"));
            BlockPos end = BlockPos.fromLong(tag.getLong("End"));

            if (!(start.equals(data.getLeft()) && end.equals(data.getRight()))) {
                this.data = Pair.of(start, end);
                invalidateCache();
            }
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class TextureProvider extends RenderDataProvider<Identifier> {
        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "texture");

        private static final LoadingCache<String, TextureProvider> cache = CacheBuilder.newBuilder()
                .maximumSize(20)
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .removalListener((RemovalListener<String, TextureProvider>) notification -> MinecraftClient.getInstance().getTextureManager().destroyTexture(notification.getValue().data))
                .build(new CacheLoader<String, TextureProvider>() {
                    @Override
                    public TextureProvider load(String key) {
                        NativeImage image = loadImage(key);

                        Identifier id = new Identifier(HolographicRenders.MOD_ID, RandomStringUtils.random(6, true, true).toLowerCase());

                        MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(image));

                        return new TextureProvider(id);
                    }
                });

        protected TextureProvider(Identifier data) {
            super(data);
        }

        public static TextureProvider of(String url) throws ExecutionException {
            return cache.get(url);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {

            matrices.push();
            matrices.scale(0.1f, -0.1f, 0.1f);
            matrices.translate(5, -20, 5);

            PlayerEntity player = MinecraftClient.getInstance().player;
            double x = player.getX() - be.getPos().getX() - 0.5;
            double z = player.getZ() - be.getPos().getZ() - 0.5;
            float rot = (float) MathHelper.atan2(z, x);

            matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(-rot));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));

            matrices.translate(-7.5, 0, 0);

            Identifier identifierTexture = new Identifier(HolographicRenders.MOD_ID, "yeet.png");
            TextureRenderLayer textureRenderLayer = new TextureRenderLayer(RenderLayer.getText(identifierTexture));

            VertexFormat vertexFormat = textureRenderLayer.getVertexFormat();
            BufferBuilder bufferBuilder = new BufferBuilder(5);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, vertexFormat);
            DrawQuad(data, 0.0f, 0.0f, 16.0f, 16.0f, matrices, bufferBuilder);
            bufferBuilder.end();
            RenderSystem.enableDepthTest();
            BufferRenderer.draw(bufferBuilder);
            matrices.pop();
        }

        public void DrawQuad(Identifier texture, float offX, float offY, float width, float height, MatrixStack stack, BufferBuilder buffer) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
            Matrix4f matrix = stack.peek().getModel();
            float x2 = offX + width, y2 = offY + height;
            buffer.vertex(matrix, offX, offY, 1.0f).texture(0.0f, 0.0f).next();
            buffer.vertex(matrix, offX, y2, 1.0f).texture(0.0f, 1.0f).next();
            buffer.vertex(matrix, x2, y2, 1.0f).texture(1.0f, 1.0f).next();
            buffer.vertex(matrix, x2, offY, 1.0f).texture(1.0f, 0.0f).next();
        }

        private static NativeImage loadImage(String loc) {
            try {
                URL url = new URL(loc);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                if (conn.getContentType().contains("png") || conn.getContentType().contains("jpeg")
                        || conn.getContentType().contains("tiff") || conn.getContentType().contains("bmp")) {
                    return getFromBuffered(ImageIO.read(url));
                }
                conn.disconnect();
            } catch (IOException ignored) {}
            return null;
        }

        private static NativeImage getFromBuffered(BufferedImage image) throws IOException {
            try (FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream()) {
                ImageIO.write(image, "PNG", outputStream);
                return NativeImage.read(new FastByteArrayInputStream(outputStream.array));
            }
        }

        @Override
        protected NbtCompound write(ProjectorBlockEntity be) {
            final NbtCompound tag = new NbtCompound();
            tag.putString("Texture", data.toString());
            return tag;
        }

        @Override
        protected void read(NbtCompound tag, ProjectorBlockEntity be) {
            this.data = new Identifier(tag.getString("Texture"));
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

}
