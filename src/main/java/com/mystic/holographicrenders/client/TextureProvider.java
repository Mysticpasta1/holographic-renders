package com.mystic.holographicrenders.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.madgag.gif.fmsware.GifDecoder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.renderer.RenderType;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TextureProvider extends RenderDataProvider<ResourceLocation> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "texture");

    private static final LoadingCache<String, com.mystic.holographicrenders.client.TextureProvider> cache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, com.mystic.holographicrenders.client.TextureProvider>) notification -> Minecraft.getInstance().getTextureManager().release(notification.getValue().data))
            .build(new CacheLoader<>() {
                @Override
                public com.mystic.holographicrenders.client.TextureProvider load(String key) {
                    try {
                        Pair<ResourceLocation, Sprite> pair = loadImage(key);
                        return new com.mystic.holographicrenders.client.TextureProvider(pair.getKey(), pair.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();

                        ResourceLocation id = ResourceLocation.withDefaultNamespace("missingno");

                        return new com.mystic.holographicrenders.client.TextureProvider(id, new RegularSprite(id, 16, 16));
                    }
                }
            });
    private static ProjectorBlockEntity entity;

    private final Sprite sprite;
    private int tick = 0;

    protected TextureProvider(ResourceLocation data, Sprite value) {
        super(data);
        sprite = value;
    }

    public static com.mystic.holographicrenders.client.TextureProvider of(String url) throws ExecutionException {
        return cache.get(url);
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        TextureProvider.entity = entity;
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource.BufferSource immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        matrices.pushPose();
        matrices.scale(0.1f, -0.1f, 0.1f);
        matrices.translate(5, -20, 5);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Player player = Minecraft.getInstance().player;
        double x = player.getX() - be.getBlockPos().getX() - 0.5;
        double z = player.getZ() - be.getBlockPos().getZ() - 0.5;
        float rot = (float) Mth.atan2(z, x);
        matrices.mulPose(Axis.YP.rotation(-rot));
        matrices.mulPose(Axis.YP.rotationDegrees(90));

        matrices.translate(-7.5, 0, 0);

        var matrix = matrices.last().pose();
        sprite.render(immediate.getBuffer(RenderType.translucent()), matrix, 0, 0, 16, 16, (int) ((Minecraft.getInstance().level.getGameTime() + tickDelta) * 50) % sprite.getFrameCount(), Color.WHITE, entity);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrices.popPose();
    }

    private static int clamp(int val) {
        return val < 0 ? 0 : (val > 255 ? 255 : val);
    }

    private static Pair<ResourceLocation, Sprite> loadImage(String loc) throws IOException {
        URL url = new URL(loc);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Set user-agent for better compatibility

        NativeImage image = null;
        Sprite sprite = null;

        // Normalize content-type
        String type = conn.getContentType();
        if (type == null) type = "";
        type = type.toLowerCase(Locale.ROOT);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                HolographicRenders.MOD_ID,
                RandomStringUtils.random(6, true, true).toLowerCase()
        );

        try (InputStream inputStream = conn.getInputStream()) {
            if (type.contains("html")) {
                // Parse HTML, find first <img> and load image from its src
                try {
                    Document doc = Jsoup.connect(loc).userAgent("Mozilla/5.0").get();
                    Element img = doc.selectFirst("img");
                    if (img != null) {
                        String imgSrc = img.absUrl("src");
                        System.out.println("Found image URL: " + imgSrc);

                        URLConnection connection = new URL(imgSrc).openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        try (InputStream imgStream = connection.getInputStream()) {
                            String contentType = connection.getContentType();
                            if (contentType == null || contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
                                System.out.println("❌ Invalid image: got HTML instead.");
                                return null;
                            }

                            BufferedImage rawImage = ImageIO.read(imgStream);
                            if (rawImage == null) {
                                System.out.println("❌ ImageIO failed to decode.");
                                return null;
                            }

                            BufferedImage bufferedImage = convertToARGB(rawImage);
                            image = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
                            for (int x = 0; x < image.getWidth(); x++) {
                                for (int y = 0; y < image.getHeight(); y++) {
                                    image.setPixelRGBA(x, y, convertColor(bufferedImage.getRGB(x, y)));
                                }
                            }
                            sprite = new RegularSprite(id, image.getWidth(), image.getHeight());
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Can't find html image: " + e.getMessage());
                    return null;
                }
            } else if (type.contains("gif")) {
                GifSprite.GifDefinition definition = new GifSprite.GifDefinition();
                GifDecoder decoder = getFrames(inputStream);

                definition.width = decoder.getFrameSize().width;
                definition.height = decoder.getFrameSize().height * decoder.getFrameCount();
                definition.uvHeight = 1f / decoder.getFrameCount();

                List<Integer> frames = new ArrayList<>();
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    for (int delay = 0; delay < decoder.getDelay(i); delay++) {
                        frames.add(i);
                    }
                }
                definition.frames = frames.stream().mapToInt(Integer::intValue).toArray();

                BufferedImage newImage = new BufferedImage(
                        decoder.getFrameSize().width,
                        decoder.getFrameSize().height * decoder.getFrameCount(),
                        BufferedImage.TYPE_INT_ARGB
                );

                Graphics bg = newImage.getGraphics();
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    bg.drawImage(decoder.getFrame(i), 0, i * decoder.getFrameSize().height, null); // fixed y offset
                }
                bg.dispose();

                image = new NativeImage(NativeImage.Format.RGBA, newImage.getWidth(), newImage.getHeight(), false);
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        image.setPixelRGBA(x, y, convertColor(newImage.getRGB(x, y)));
                    }
                }
                sprite = new GifSprite(id, definition);
            } else if (type.contains("png") || type.contains("webp") || type.contains("svg") || type.contains("jpeg") || type.contains("jpg") || type.contains("tiff")) {
                if (type.contains("png")) {
                    image = NativeImage.read(inputStream);
                } else if (type.contains("webp")) {
                    // Load WebP from stream, ImageIO might need plugin or use alternative lib
                    BufferedImage webpImage = ImageIO.read(inputStream);
                    if (webpImage == null) {
                        System.out.println("❌ Failed to decode WebP image.");
                        return null;
                    }
                    image = new NativeImage(NativeImage.Format.RGBA, webpImage.getWidth(), webpImage.getHeight(), false);
                    for (int x = 0; x < image.getWidth(); x++) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            image.setPixelRGBA(x, y, convertColor(webpImage.getRGB(x, y)));
                        }
                    }
                } else if (type.contains("svg")) {
                    PNGTranscoder transcoder = new PNGTranscoder();
                    transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 512f);
                    transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 512f);

                    TranscoderInput input = new TranscoderInput(inputStream);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    TranscoderOutput output = new TranscoderOutput(os);

                    transcoder.transcode(input, output);

                    byte[] pngData = os.toByteArray();

                    try (InputStream pngStream = new ByteArrayInputStream(pngData)) {
                        BufferedImage bufferedImage = ImageIO.read(pngStream);
                        if (bufferedImage == null) {
                            System.out.println("❌ Failed to decode transcoded SVG PNG.");
                            return null;
                        }

                        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
                        for (int x = 0; x < bufferedImage.getWidth(); x++) {
                            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                                nativeImage.setPixelRGBA(x, y, convertColor(bufferedImage.getRGB(x, y)));
                            }
                        }
                        image = nativeImage;
                        sprite = new RegularSprite(id, nativeImage.getWidth(), nativeImage.getHeight());
                    }
                } else if (type.contains("jpeg") || type.contains("jpg") || type.contains("tiff")) {
                    BufferedImage bufferedImage = convertToARGB(ImageIO.read(inputStream));
                    if (bufferedImage == null) {
                        System.out.println("❌ Failed to decode JPEG/TIFF image.");
                        return null;
                    }
                    image = new NativeImage(NativeImage.Format.RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), false);
                    for (int x = 0; x < image.getWidth(); x++) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            image.setPixelRGBA(x, y, convertColor(bufferedImage.getRGB(x, y)));
                        }
                    }
                }
                sprite = new RegularSprite(id, image.getWidth(), image.getHeight());
            } else {
                System.out.println("❌ Unsupported content type: " + type);
                return null;
            }
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        } finally {
            conn.disconnect();
        }

        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
        return Pair.of(id, (Sprite) sprite);
    }

    public static int convertColor(int color) {
        return (getAlpha(color) & 255) << 24 | (getBlue(color) & 255) << 16 | (getGreen(color) & 255) << 8 | (getRed(color) & 255);
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
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
    protected CompoundTag write(ProjectorBlockEntity be) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("Texture", data.toString());
        return tag;
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {
        this.data = ResourceLocation.withDefaultNamespace(tag.getString("Texture"));
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
