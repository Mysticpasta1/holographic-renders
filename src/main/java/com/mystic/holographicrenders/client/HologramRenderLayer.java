package com.mystic.holographicrenders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mystic.holographicrenders.HolographicRenders;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class HologramRenderLayer extends RenderType {

    //TODO refactor this and make it not shit

    private static final Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();
    private static float alpha = 0.6f;

    public static final Runnable beginAction = () -> {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, alpha); //TODO check my math! (redAlpha = 0 = ON), (redAlpha = 15 = OFF) //TODO fix this so only on is doing this at a time!!!
    };

    public static final Runnable endAction = () -> {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    };

    public static void setAlpha(float alpha) {
        HologramRenderLayer.alpha = alpha;
    }

    private HologramRenderLayer(RenderType original) {
        super(String.format("%s_%s_hologram", original.toString(), HolographicRenders.MOD_ID), original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, () -> {
            original.setupRenderState();
            beginAction.run();
        }, () -> {
            endAction.run();
            original.clearRenderState();
        });
    }

    public static RenderType remap(RenderType in) {
        if (in instanceof HologramRenderLayer) {
            return in;
        } else {
            return remappedTypes.computeIfAbsent(in, HologramRenderLayer::new);
        }
    }

    public static MultiBufferSource.BufferSource initBuffers(MultiBufferSource.BufferSource original) {
        Map<RenderType, BufferBuilder> layerBuffers = original.fixedBuffers;
        Map<RenderType, BufferBuilder> remapped = new Object2ObjectLinkedOpenHashMap<>();
        for (Map.Entry<RenderType, BufferBuilder> e : layerBuffers.entrySet()) {
            remapped.put(HologramRenderLayer.remap(e.getKey()), new BufferBuilder(e.getKey().bufferSize()));
        }
        return new HologramVertexConsumerProvider(new BufferBuilder(256), remapped);
    }

    public static class HologramVertexConsumerProvider extends MultiBufferSource.BufferSource {

        protected HologramVertexConsumerProvider(BufferBuilder fallback, Map<RenderType, BufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {

            type = HologramRenderLayer.remap(type);

            Optional<RenderType> optional = type.asOptional();
            BufferBuilder bufferBuilder = this.fixedBuffers.getOrDefault(type, this.builder);

            if (!Objects.equals(this.lastState, optional)) {
                if (this.lastState.isPresent()) {
                    RenderType renderLayer2 = this.lastState.get();
                    if (!this.fixedBuffers.containsKey(renderLayer2)) {
                        this.endBatch(renderLayer2);
                    }
                }

                if (this.startedBuffers.add(bufferBuilder)) {
                    if (!bufferBuilder.building()) {
                        bufferBuilder.begin(type.mode(), type.format());
                    }
                }

                this.lastState = optional;
            }

            return bufferBuilder;
        }
    }
}
