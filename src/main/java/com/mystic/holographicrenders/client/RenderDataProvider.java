package com.mystic.holographicrenders.client;

import com.glisco.worldmesher.WorldMesh;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.item.TextureScannerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T>
 */
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
        final CompoundTag compoundTag = new CompoundTag();
        RenderDataProviderRegistry.register(ItemProvider.ID, () -> new ItemProvider(ItemStack.EMPTY));
        RenderDataProviderRegistry.register(BlockProvider.ID, () -> new BlockProvider(Blocks.AIR.getDefaultState()));
        RenderDataProviderRegistry.register(EntityProvider.ID, () -> new EntityProvider(null));
        RenderDataProviderRegistry.register(AreaProvider.ID, () -> new AreaProvider(new Pair<>(BlockPos.ORIGIN, BlockPos.ORIGIN)));
        RenderDataProviderRegistry.register(EmptyProvider.ID, () -> EmptyProvider.INSTANCE);
        RenderDataProviderRegistry.register(TextProvider.ID, () -> new TextProvider(Text.of("")));
        RenderDataProviderRegistry.register(TextureProvider.ID, () -> new TextureProvider(compoundTag.getString("URL")));
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
                matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(rot));
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90 * (facing == Direction.EAST ? -1 : 1)));
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

        private final MinecraftClient client;

        private WorldMesh mesh;
        private long lastUpdateTick;

        protected AreaProvider(Pair<BlockPos, BlockPos> data) {
            super(data);
            this.client = MinecraftClient.getInstance();
            //TODO fix this arrgh
            this.lastUpdateTick = this.client.world.getTime();
            invalidateCache();
        }

        public static AreaProvider from(BlockPos start, BlockPos end) {
            return new AreaProvider(new Pair<>(start, end));
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

            if (!(start.equals(data.getLeft()) && end.equals(data.getRight()))) {
                this.data = new Pair<>(start, end);
                invalidateCache();
            }
        }

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    public static class TextureProvider extends RenderDataProvider<String> {

        private static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "texture");
        private boolean textureLoaded = false;

        protected TextureProvider(String data) {
            super(data);
            if (data == null) return;
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.getCompound("URL").putString("URL", data);
        }

        public static TextureProvider of(String url) {
            return new TextureProvider(url);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
            if (!textureLoaded) {
                loadTexture(MinecraftClient.getInstance().getTextureManager());
                return;
            }

            matrices.scale(0.1f, -0.1f, 0.1f);
            matrices.translate(5, -20, 5);

            PlayerEntity player = MinecraftClient.getInstance().player;
            double x = player.getX() - be.getPos().getX() - 0.5;
            double z = player.getZ() - be.getPos().getZ() - 0.5;
            float rot = (float) MathHelper.atan2(z, x);

            matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-rot));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));

            matrices.translate(-7.5, 0, 0);

            final CompoundTag compoundTag = new CompoundTag();
            Identifier texture = new Identifier(HolographicRenders.MOD_ID, HolographicRenders.MOD_ID + "/hologramimages/" + compoundTag.getString("URL"));

            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

            TextureRenderLayer textureRenderLayer = new TextureRenderLayer(RenderLayer.getText(texture));
            VertexFormat vertexFormat = textureRenderLayer.getVertexFormat();
            BufferBuilder bufferBuilder = new BufferBuilder(5);
            bufferBuilder.begin(7, vertexFormat);
            DrawQuad(texture, 0.0f, 0.0f, 16.0f, 16.0f, matrices, textureManager, bufferBuilder);
            bufferBuilder.end();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            BufferRenderer.draw(bufferBuilder);
        }

        public void DrawQuad(Identifier texture, float offX, float offY, float width, float height, MatrixStack stack, TextureManager textureManager, BufferBuilder buffer) {
            RenderSystem.bindTexture(Objects.requireNonNull(textureManager.getTexture(texture)).getGlId());
            Matrix4f matrix = stack.peek().getModel();
            float x2 = offX + width, y2 = offY + height;
            buffer.vertex(matrix, offX, offY, 1.0f).texture(0.0f, 0.0f).next();
            buffer.vertex(matrix, offX, y2, 1.0f).texture(0.0f, 1.0f).next();
            buffer.vertex(matrix, x2, y2, 1.0f).texture(1.0f, 1.0f).next();
            buffer.vertex(matrix, x2, offY, 1.0f).texture(1.0f, 0.0f).next();
        }


        private void loadTexture(TextureManager textureManager) {
            if (this.textureLoaded) return;

            final CompoundTag compoundTag = new CompoundTag();
            if (textureManager.getTexture(new Identifier(HolographicRenders.MOD_ID, compoundTag.getString("URL"))) != null) { //
                this.textureLoaded = true;
                return;
            }

            System.out.println(new TextboxScreenRoot());

            textureManager.registerTexture(new Identifier(HolographicRenders.MOD_ID, HolographicRenders.MOD_ID + "/hologramimages/"), new AbstractTexture() {
                @Override
                public void load(ResourceManager manager) throws IOException {
                    System.out.println(new Identifier(HolographicRenders.MOD_ID, HolographicRenders.MOD_ID + "/hologramimages/"));
                }
            });

            textureLoaded = true;

        }

        public void createFileAndLoad(){
            CompoundTag compoundTag = new CompoundTag();
            new TextureScannerItem();
            try {
                ArrayList<String> URLArray = new ArrayList<>();
                String dir = HolographicRenders.MOD_ID + "/hologramimages/";
                File folder = new File(dir);
                if (folder.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
                for(int i = 0; i < 1; i ++) {
                    Random random = new Random();
                    URL url = new TextboxScreenRoot().getURL();
                    if(url != null) {
                        URLArray.add(url.toString());
                        for (String s : URLArray) {
                            final Pattern pattern = Pattern.compile("^.*[a-zA-Z]+*.[a-zA-Z]+*.[a-zA-Z]+*.[a-zA-Z]+*.[a-zA-Z]+*.[a-zA-Z]+*.[a-zA-Z]+$", Pattern.CASE_INSENSITIVE);
                            String input = pattern.pattern();
                            final Matcher matcher = pattern.matcher(input);
                            if (!matcher.matches()) {
                                File files = new File(s);
                                FileUtils.copyURLToFile(new URL(s), new File(HolographicRenders.MOD_ID + "/hologramimages/" + url.getFile() + ".png"));
                                System.out.println("image downloaded");
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println("no URL found or null url");
            } catch (IOException e) {
                System.out.println("file not found or Failed to create directory!" + e.getMessage());
            }
        }

        @Override
        protected CompoundTag write(ProjectorBlockEntity be) {
            final CompoundTag tag = new CompoundTag();
            tag.putString("URL", data);
            return tag;
        }

        @Override
        protected void read(CompoundTag tag, ProjectorBlockEntity be) {
            this.data = tag.getString("URL");
            textureLoaded = false;
//            loadTexture();
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
