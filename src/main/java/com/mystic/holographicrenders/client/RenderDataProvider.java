package com.mystic.holographicrenders.client;

import static com.teamwizardry.librarianlib.core.util.Shorthand.vec;
import static net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer.getAlpha;
import static net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer.getBlue;
import static net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer.getGreen;
import static net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer.getRed;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.text.NumberFormatter;

import com.glisco.worldmesher.WorldMesh;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.madgag.gif.fmsware.GifDecoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.Textures;
import com.mystic.holographicrenders.item.WidgetType;
import com.teamwizardry.librarianlib.core.util.Client;
import com.teamwizardry.librarianlib.facade.FacadeScreen;
import com.teamwizardry.librarianlib.facade.FacadeWidget;
import com.teamwizardry.librarianlib.facade.layer.GuiLayer;
import com.teamwizardry.librarianlib.facade.layer.GuiLayerEvents;
import com.teamwizardry.librarianlib.facade.layers.SpriteLayer;
import com.teamwizardry.librarianlib.facade.layers.text.TextFit;
import com.teamwizardry.librarianlib.facade.pastry.layers.PastryLabel;
import com.teamwizardry.librarianlib.math.Matrix4d;
import com.teamwizardry.librarianlib.mosaic.Sprite;
import gui.ava.html.Html2Image;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
        RenderDataProviderRegistry.register(TextureProvider.ID, () -> {
            Identifier id = new Identifier("missingno");
            return new TextureProvider(id, new RegularSprite(id, 16,16));
        });
        RenderDataProviderRegistry.register(WidgetProvider.ID, () -> new WidgetProvider(WidgetType.Blank, BlockPos.ORIGIN));
        RenderDataProviderRegistry.register(MapProvider.ID, () -> new MapProvider(-1));
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
        private static final LoadingCache<Pair<BlockPos, BlockPos>, AreaProvider> cache = CacheBuilder.newBuilder()
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
                        try {
                            Pair<Identifier, Sprite> pair = loadImage(key);
                            return new TextureProvider(pair.getKey(), pair.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();

                            Identifier id = new Identifier("missingno");

                            return new TextureProvider(id, new RegularSprite(id, 16,16));
                        }
                    }
                });

        private final Sprite sprite;
        private int tick = 0;

        protected TextureProvider(Identifier data, Sprite value) {
            super(data);
            sprite = value;
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

            Matrix4d matrix = new Matrix4d(matrices.peek().getModel());

            sprite.draw(matrix, 0,0, 16,16, (int) ((Client.getTime().getTime() * 50) % sprite.getFrameCount()), Color.WHITE);

            RenderSystem.enableDepthTest();
            matrices.pop();
        }

        private static Pair<Identifier, Sprite> loadImage(String loc) throws IOException {
            URL url = new URL(loc);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            Sprite sprite = null;
            NativeImage image = null;

            String type = conn.getContentType();

            Identifier id = new Identifier(HolographicRenders.MOD_ID, RandomStringUtils.random(6, true, true).toLowerCase());

            if(type.contains("gif")) {
                GifSprite.GifDefinition definition = new GifSprite.GifDefinition();
                GifDecoder decoder = getFrames(conn.getInputStream());

                definition.width = decoder.getFrameSize().width;
                definition.height = decoder.getFrameSize().height * decoder.getFrameCount();
                definition.uvHeight = 1 / (float) decoder.getFrameCount();

                List<Integer> frames = new ArrayList<>();

                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    for (int time = 0; time < decoder.getDelay(i); time++) {
                        frames.add(i);
                    }
                }

                definition.frames = frames.stream().mapToInt(Integer::intValue).toArray();

                BufferedImage newImage = new BufferedImage(decoder.getFrameSize().width, decoder.getFrameSize().height * decoder.getFrameCount(), BufferedImage.TYPE_INT_ARGB);

                Graphics bg = newImage.getGraphics();

                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    bg.drawImage(decoder.getFrame(i), 0, i * decoder.getFrameSize().width, null);
                }
                bg.dispose();

                image = new NativeImage(NativeImage.Format.RGBA, newImage.getWidth(), newImage.getHeight(), false);

                for (int x = 0; x < image.getWidth(); x++)
                    for (int y = 0; y < image.getHeight(); y++) {
                        image.setColor(x, y, convertColor(newImage.getRGB(x, y)));
                    }

                sprite = new GifSprite(id, definition);

            } else if(!type.contains("gif") && (type.contains("png") || (type.contains("html")) || type.contains("jpeg") || type.contains("jpg") || type.contains("tiff"))) {
                if (type.contains("png")) {
                    image = NativeImage.read(conn.getInputStream());
                } else if (type.contains("html")) {
                        /*BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        String line;
                        StringBuilder sb = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                            sb.append(System.lineSeparator());
                        }*/
                    BufferedImage imageFromHtml = Html2Image.fromURL(url).getImageRenderer().getBufferedImage();
                    image = new NativeImage(NativeImage.Format.RGBA, imageFromHtml.getWidth(), imageFromHtml.getHeight(), false);

                    for (int x = 0; x < image.getWidth(); x++)
                        for (int y = 0; y < image.getHeight(); y++) {
                            image.setColor(x, y, convertColor(imageFromHtml.getRGB(x, y)));
                        }

                } else if (type.contains("jpeg") || type.contains("jpg") || type.contains("tiff")) {
                    BufferedImage bufferedImage = convertToARGB(ImageIO.read(conn.getInputStream()));

                    image = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);

                    for (int x = 0; x < image.getWidth(); x++)
                        for (int y = 0; y < image.getHeight(); y++) {
                            image.setColor(x, y, convertColor(bufferedImage.getRGB(x, y)));
                        }
                }

                sprite = new RegularSprite(id, image.getWidth(), image.getHeight());
            }

            conn.disconnect();
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(image));

            return Pair.of(id, sprite);
        }

        public static int convertColor(int color) {
            return (getAlpha(color) & 255) << 24 | (getBlue(color) & 255) << 16 | (getGreen(color) & 255) << 8 | (getRed(color) & 255) << 0;
        }

        public static GifDecoder getFrames(InputStream gif) throws IOException{
            GifDecoder decoder = new GifDecoder();
            decoder.read(gif);
            return decoder;
        }

        private static BufferedImage convertToARGB(BufferedImage srcImage) {
            BufferedImage newImage = new BufferedImage(srcImage.getWidth(null),
                    srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics bg = newImage.getGraphics();
            bg.drawImage(srcImage, 0, 0, null);
            bg.dispose();
            return newImage;
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

    public static class MapProvider extends RenderDataProvider<Integer> {
        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "map");

        private static final LoadingCache<Integer, MapProvider> cache = CacheBuilder.newBuilder()
                .maximumSize(20)
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, MapProvider>() {
                    @Override
                    public MapProvider load(Integer key) {
                        return new MapProvider(key);
                    }
                });

        protected MapProvider(Integer id) {
            super(id);
        }

        public static RenderDataProvider<?> of(Integer id) {
            try {
                return cache.get(id);
            } catch (ExecutionException e) {
                return new MapProvider(-1);
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException {
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

            MapState state = FilledMapItem.getMapState(data, be.getWorld());
            if(state != null) {
                matrices.scale(0.125f, 0.125f, 0.125f);
                MinecraftClient.getInstance().gameRenderer.getMapRenderer().draw(matrices, immediate, data, state, false, light);
            }

            RenderSystem.enableDepthTest();
            matrices.pop();
        }

        @Override
        protected NbtCompound write(ProjectorBlockEntity be) {
            final NbtCompound tag = new NbtCompound();
            tag.putInt("Id", data);
            return tag;
        }

        @Override
        protected void read(NbtCompound tag, ProjectorBlockEntity be) {
            this.data = tag.getInt("Id");
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class WidgetProvider extends RenderDataProvider<Pair<WidgetType, BlockPos>> {
        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "widget");

        private FacadeWidget widget;

        private GuiLayer root;

        private FacadeWidget createWidget(WidgetType type, BlockPos pos) {
            FacadeScreen screen = new FacadeScreen(LiteralText.EMPTY);
            FacadeWidget widget = screen.getFacade();
            widget.getRoot().add(root = new GuiLayer());

            switch (type) {
                case Blank: {
                    root.setSize(vec(200, 200));
                    root.add(new SpriteLayer(Textures.projector, 0, 0, 200, 200));
                    break;
                }
                case Clock: {
                    SpriteLayer background = new SpriteLayer(Textures.textfield, 0, 0, 100, 100);
                    PastryLabel clock = new PastryLabel(0,0, "Avacado");
                    clock.setColor(Color.WHITE);
                    root.setScale(0.5);
                    background.add(clock);
                    background.hook(GuiLayerEvents.Update.class, event -> {
                        float time = Client.getWorldTime().getTime();

                        int hour = (int) (time / 1000f);

                        int minutes = (int) ((time % 1000f /1000f) * 60);

                        clock.setText(Strings.padStart("" + hour, 2, '0') + ":" + Strings.padStart("" + minutes, 2, '0'));
                        clock.fitToText(TextFit.BOTH);
                        background.setSize(clock.getSize());
                        background.setX(background.getWidth()/2);
                        root.setSize(background.getSize());
                    });

                    root.add(background);
                    break;
                }
            }

            widget.getRoot().hook(GuiLayerEvents.LayoutChildren.class, event -> {
                root.setPos(vec(-root.getWidth()/2, root.getHeight()));
            });

            return widget;
        }

            private static final LoadingCache<Pair<WidgetType, BlockPos>, WidgetProvider> cache = CacheBuilder.newBuilder()
                    .maximumSize(20)
                    .expireAfterAccess(20, TimeUnit.SECONDS)
                    .build(new CacheLoader<Pair<WidgetType, BlockPos>, WidgetProvider>() {
                        @Override
                        public WidgetProvider load(Pair<WidgetType, BlockPos> key) {
                            return new WidgetProvider(key.getKey(), key.getValue());
                        }
                    });


        public WidgetProvider(WidgetType type, BlockPos pos) {
            super(Pair.of(type, pos));
            widget = createWidget(type, pos);
        }

        public static WidgetProvider of(WidgetType widget, BlockPos pos) throws ExecutionException {
            return cache.get(Pair.of(widget, pos));
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) throws MalformedURLException {
            RenderSystem.enableDepthTest();

            matrices.push();

            float height = root.getHeightf();
            float width = root.getHeightf();

            matrices.translate(0.5,1,0.5);

            PlayerEntity player = MinecraftClient.getInstance().player;
            double x = player.getX() - be.getPos().getX() - 0.5;
            double z = player.getZ() - be.getPos().getZ() - 0.5;
            float rot = (float) MathHelper.atan2(z, x);

            matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(-rot));
            matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(90));

            matrices.translate(0, 0.5,0);

            matrices.scale(-1/width, -1/height, 1f);

            matrices.translate(0, -height,0);

            widget.update();
            widget.render(matrices);

            matrices.pop();

            RenderSystem.disableDepthTest();
        }

        @Override
        protected NbtCompound write(ProjectorBlockEntity be) {
            final NbtCompound tag = new NbtCompound();
            tag.putLong("Pos", data.getValue().asLong());
            tag.putInt("Widget", data.getKey().ordinal());
            return tag;
        }

        @Override
        protected void read(NbtCompound tag, ProjectorBlockEntity be) {
            this.data = Pair.of(WidgetType.fromId(tag.getInt("Widget")), BlockPos.fromLong(tag.getLong("Pos")));
        }


        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }
}
