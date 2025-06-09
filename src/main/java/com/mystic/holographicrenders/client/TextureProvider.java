package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.player.videolan.VideoPlayer;
import com.mojang.blaze3d.vertex.PoseStack;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class TextureProvider extends RenderDataProvider<String> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "texture");
    private static ProjectorBlockEntity entity;

    private int tick = 0;
    private ImageCache imageCache;
    private VideoPlayer videoPlayer;
    private boolean isStarted;
    private long lastRenderTick = 0; // Track last render time

    private static final HashMap<String, TextureProvider> hashMap = new HashMap<>();

    protected TextureProvider(String data) {
        super(data);
        imageCache = ImageAPI.getCache(URI.create(data.trim()), Minecraft.getInstance());
        videoPlayer = new VideoPlayer(Minecraft.getInstance());
    }

    public static TextureProvider of(String url) throws ExecutionException {
        return hashMap.computeIfAbsent(url, s -> new TextureProvider(url));
    }

    public static void setEntity(ProjectorBlockEntity entity) {
        TextureProvider.entity = entity;
    }

    public void release() {
        videoPlayer.release();
        imageCache.deuse();
        isStarted = false;
    }

    public static void tickCleanup() {
        if (Minecraft.getInstance().level == null) return;

        long currentTick = Minecraft.getInstance().level.getGameTime();
        List<String> toRemove = new ArrayList<>();

        for (var entry : hashMap.entrySet()) {
            TextureProvider provider = entry.getValue();
            if (currentTick - provider.lastRenderTick > 100) {
                provider.release();
                toRemove.add(entry.getKey());
            }
        }

        for (String key : toRemove) {
            hashMap.remove(key);
        }
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

        switch (imageCache.getStatus()) {
            case READY -> imageCache.getRenderer();
            case WAITING -> imageCache.load();
            case LOADING -> {
                // Optional: image loading animation
            }
            case FAILED -> imageCache.getException();
        }

        if (imageCache.getStatus() == ImageCache.Status.READY) {
            if (imageCache.isVideo()) {
                if (!isStarted) {
                    videoPlayer.start(imageCache.uri);
                    isStarted = true;
                    System.out.println("Video started");
                }
                if (videoPlayer.isSafeUse() && videoPlayer.isReady()) {
                    videoPlayer.preRender();
                    final Matrix4f matrix4f = matrices.last().pose();
                    int a = videoPlayer.texture();
                    RegularSprite regularSprite = new RegularSprite(videoPlayer.width(), videoPlayer.height());
                    regularSprite.render(immediate.getBuffer(RenderType.translucent()), a, matrix4f, 0, 0, 16, 16,
                            (int) ((Minecraft.getInstance().level.getGameTime() + tickDelta) * 50) % regularSprite.getFrameCount(),
                            Color.WHITE, entity);
                }
            } else {
                ImageRenderer imageRenderer = imageCache.getRenderer();
                int a = imageRenderer.texture(tick, tickDelta, true);
                final Matrix4f matrix4f = matrices.last().pose();
                RegularSprite regularSprite = new RegularSprite(imageRenderer.width, imageRenderer.height);
                regularSprite.render(immediate.getBuffer(RenderType.translucent()), a, matrix4f, 0, 0, 16, 16,
                        (int) ((Minecraft.getInstance().level.getGameTime() + tickDelta) * 50) % regularSprite.getFrameCount(),
                        Color.WHITE, entity);
            }
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrices.popPose();

        // ✅ Mark as active this frame
        lastRenderTick = Minecraft.getInstance().level.getGameTime();
    }

    @Override
    protected CompoundTag write(ProjectorBlockEntity be) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("url", data);
        return tag;
    }

    @Override
    protected void read(CompoundTag tag, ProjectorBlockEntity be) {
        this.data = tag.getString("url");
    }

    @Override
    public ResourceLocation getTypeId() {
        return ID;
    }
}
