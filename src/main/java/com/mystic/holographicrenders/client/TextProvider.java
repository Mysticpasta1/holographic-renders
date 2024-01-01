package com.mystic.holographicrenders.client;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class TextProvider extends RenderDataProvider<Text> {

    public static final Identifier ID = new Identifier(HolographicRenders.MOD_ID, "text");

    protected TextProvider(Text data) {
        super(data);
    }

    public static com.mystic.holographicrenders.client.TextProvider from(Text text) {
        return new com.mystic.holographicrenders.client.TextProvider(text);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, float tickDelta, int light, int overlay, BlockEntity be) {
        drawText(matrices, be, 0, data, immediate);
    }

    public static void drawText(MatrixStack matrices, BlockEntity be, int color, Text text, VertexConsumerProvider.Immediate immediate) {
        matrices.translate(0.5, 0.0, 0.5);

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            final Direction facing = ProjectorBlock.getFacing(be.getCachedState());

            double side1;
            double side2;

            switch (facing.getAxis()) {
                case X -> {
                    side1 = player.getY() - be.getPos().getY() - 0.5;
                    side2 = player.getZ() - be.getPos().getZ() - 0.5;
                }
                case Z -> {
                    side1 = player.getX() - be.getPos().getX() - 0.5;
                    side2 = player.getY() - be.getPos().getY() - 0.5;
                }
                default -> {
                    side1 = player.getX() - be.getPos().getX() - 0.5;
                    side2 = player.getZ() - be.getPos().getZ() - 0.5;
                }
            }

            float rot = (float) MathHelper.atan2(side2, side1);
            rot *= facing == Direction.UP ? -1 : 1;
            rot *= facing.getAxis() == Direction.Axis.Z ? facing.getOffsetZ() : 1;
            rot *= facing.getAxis() == Direction.Axis.X ? facing.getOffsetX() : 1;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rot));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * (facing == Direction.EAST ? -1 : 1)));
        }

        matrices.scale(0.05f, -0.05f, 0.05f); //TODO make this usable with scaling sliders
        matrices.translate(-(MinecraftClient.getInstance().textRenderer.getWidth(text) / 2f), -20, 0); //TODO make this usable with translation sliders

            MinecraftClient.getInstance().textRenderer.draw(text, 0, 0, color, false, matrices.peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xf000f0); //TODO; fix this
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
