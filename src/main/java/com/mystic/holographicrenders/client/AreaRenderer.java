package com.mystic.holographicrenders.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

//TODO free the buffers when done
public class AreaRenderer {

    private final Map<RenderLayer, VertexBuffer> buffers;
    private final HashMap<RenderLayer, BufferBuilder> initializedLayers;

    private boolean built = false;

    public AreaRenderer() {
        buffers = RenderLayer.getBlockLayers().stream().collect(Collectors.toMap((renderLayer) -> renderLayer, (renderLayer) -> new VertexBuffer(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL)));
        initializedLayers = new HashMap<>();
    }

    public boolean isReady() {
        return built;
    }

    public void render(MatrixStack matrices) {
        final Matrix4f matrix = matrices.peek().getModel();

        buffers.forEach((renderLayer, vertexBuffer) -> {
            vertexBuffer.bind();
            renderLayer.getVertexFormat().startDrawing(0);
            renderLayer.startDrawing();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.CONSTANT_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.blendColor(1, 1, 1, 0.6f); //TODO make 0.6F a redstone dependent value out of 15, 0 OFF, 15 FULL OPACITY
            vertexBuffer.draw(matrix, 7);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            renderLayer.endDrawing();
            renderLayer.getVertexFormat().endDrawing();
            VertexBuffer.unbind();
        });

    }

    public void build(BlockState[][][] states, BlockPos origin) {

        final ClientWorld world = MinecraftClient.getInstance().world;
        final BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        MatrixStack matrices = new MatrixStack();

        int y = 0;
        int x;
        int z;

        for (BlockState[][] twoDim : states) {
            matrices.push();
            z = 0;
            for (BlockState[] oneDim : twoDim) {
                matrices.push();
                x = 0;
                for (BlockState state : oneDim) {
                    final BlockPos pos = origin.add(x, y, z);

                    matrices.push();

                    ProjectorBlockEntityRenderer.blockModelRenderer.clearCullingOverrides();
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.EAST, x == states[0][0].length - 1);
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.WEST, x == 0);
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.SOUTH, z == states[0].length - 1);
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.NORTH, z == 0);
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.UP, y == states.length - 1);
                    ProjectorBlockEntityRenderer.blockModelRenderer.setCullDirection(Direction.DOWN, y == 0);

                    RenderLayer renderLayer = RenderLayers.getBlockLayer(state);

                    if (!initializedLayers.containsKey(renderLayer)) {
                        BufferBuilder builder = new BufferBuilder(renderLayer.getExpectedBufferSize());
                        initializedLayers.put(renderLayer, builder);
                        builder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
                    }

                    ProjectorBlockEntityRenderer.blockModelRenderer.render(world, blockRenderManager.getModel(state), state, pos, matrices, initializedLayers.get(renderLayer), true, world.random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);

                    //TODO fluids
                    /*if(!world.getFluidState(pos).isEmpty()){
                        RenderLayer renderLayer1 = RenderLayers.getFluidLayer(world.getFluidState(pos));
                        if (!initializedLayers.containsKey(renderLayer1)) {
                            BufferBuilder builder = new BufferBuilder(renderLayer1.getExpectedBufferSize());
                            initializedLayers.put(renderLayer1, builder);
                            builder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
                        }
                        blockRenderManager.renderFluid(pos, world, initializedLayers.get(renderLayer), world.getFluidState(pos));
                    }*/

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

        initializedLayers.entrySet().stream().map(Map.Entry::getValue).forEach(BufferBuilder::end);

        List<CompletableFuture<Void>> list = Lists.newArrayList();
        initializedLayers.forEach((renderLayer, bufferBuilder) -> {
            list.add(buffers.get(renderLayer).submitUpload(bufferBuilder));
        });
        Util.combine(list).handle((voids, throwable) -> {
            if (throwable != null) {
                MinecraftClient.getInstance().setCrashReport(CrashReport.create(throwable, "Preparing area render"));
            }
            this.built = true;
            return true;
        });
    }
}
