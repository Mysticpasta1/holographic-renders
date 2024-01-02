package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.madgag.gif.fmsware.GifDecoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.minecraft.util.math.ColorHelper.Argb.*;

public class TextureProvider extends RenderDataProvider<Identifier> {
    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "texture");

    private static final LoadingCache<String, com.mystic.holographicrenders.client.TextureProvider> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, com.mystic.holographicrenders.client.TextureProvider>) notification -> MinecraftClient.getInstance().getTextureManager().destroyTexture(notification.getValue().data))
            .build(new CacheLoader<>() {
                @Override
                public com.mystic.holographicrenders.client.TextureProvider load(String key) {
                    try {
                        Pair<Identifier, Sprite> pair = loadImage(key);
                        return new com.mystic.holographicrenders.client.TextureProvider(pair.getKey(), pair.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();

                        Identifier id = new Identifier("missingno");

                        return new com.mystic.holographicrenders.client.TextureProvider(id, new RegularSprite(id, 16, 16));
                    }
                }
            });

    private final Sprite sprite;
    private int tick = 0;

    protected TextureProvider(Identifier data, Sprite value) {
        super(data);
        sprite = value;
    }

    public static com.mystic.holographicrenders.client.TextureProvider of(String url) throws ExecutionException {
        return cache.get(url);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        matrices.push();
        matrices.scale(0.1f, -0.1f, 0.1f);
        matrices.translate(5, -20, 5);
        RenderSystem.enableDepthTest();
        PlayerEntity player = MinecraftClient.getInstance().player;
        double x = player.getX() - be.getPos().getX() - 0.5;
        double z = player.getZ() - be.getPos().getZ() - 0.5;
        float rot = (float) MathHelper.atan2(z, x);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rot));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));

        matrices.translate(-7.5, 0, 0);

        var matrix = matrices.peek().getPositionMatrix();

        sprite.render(immediate.getBuffer(TextureRenderLayer.getTranslucent()), matrix, 0, 0, 16, 16, (int) ((MinecraftClient.getInstance().world.getTime() + tickDelta) * 50) % sprite.getFrameCount(), Color.WHITE);
        RenderSystem.disableDepthTest();
        matrices.pop();
    }

    private static Pair<Identifier, Sprite> loadImage(String loc) throws IOException {
        URL url = new URL(loc);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        Sprite sprite = null;

        NativeImage image = null;

        String type = conn.getContentType();

        Identifier id = new Identifier(HolographicRenders.MOD_ID, RandomStringUtils.random(6, true, true).toLowerCase());

        if (type.contains("gif")) {
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

            BufferedImage newImage = new BufferedImage(decoder.getFrameSize().width, decoder.getFrameSize().height * decoder.getFrameCount(), BufferedImage.TYPE_INT_RGB);

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

        } else if (!type.contains("gif") && (type.contains("png") || (type.contains("html")) || type.contains("jpeg") || type.contains("jpg") || type.contains("tiff"))) {
            if (type.contains("png")) {
                image = NativeImage.read(conn.getInputStream());
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
        return (getAlpha(color) & 255) << 24 | (getBlue(color) & 255) << 16 | (getGreen(color) & 255) << 8 | (getRed(color) & 255);
    }

    public static GifDecoder getFrames(InputStream gif) throws IOException {
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
