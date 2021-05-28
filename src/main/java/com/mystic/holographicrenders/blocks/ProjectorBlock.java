package com.mystic.holographicrenders.blocks;

import com.mystic.holographicrenders.client.RenderDataProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ProjectorBlock extends BlockWithEntity {

    private static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(7, 9, 7, 9, 12, 9),
            Block.createCuboidShape(3, 0, 3, 13, 9.25, 13),
            Block.createCuboidShape(6, 9.25, 6, 10, 10.25, 10),
            Block.createCuboidShape(7.5, 13.35355, 7.5, 8.5, 15.72855, 8.5),
            Block.createCuboidShape(2, 12.60355, 2, 14, 13.35355, 14),
            Block.createCuboidShape(6.25, 11.85355, 6.25, 9.75, 12.60355, 9.75),
            Block.createCuboidShape(14, 13.10355, 1, 15, 14.10355, 14),
            Block.createCuboidShape(1, 13.10355, 2, 2, 14.10355, 15),
            Block.createCuboidShape(2, 13.10355, 14, 15, 14.10355, 15),
            Block.createCuboidShape(1, 13.10355, 1, 14, 14.10355, 2)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public ProjectorBlock() {
        super(Settings.copy(Blocks.IRON_BLOCK).nonOpaque());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        ProjectorBlockEntity be = (ProjectorBlockEntity) world.getBlockEntity(pos);
        final ItemStack playerStack = player.getStackInHand(hand);

        if (playerStack.getItem() instanceof BlockItem) {
            be.setRenderer(RenderDataProvider.BlockProvider.from(((BlockItem) playerStack.getItem()).getBlock().getDefaultState()));
        } else if (playerStack.getItem() instanceof SpawnEggItem) {
            EntityType<?> type = ((SpawnEggItem) playerStack.getItem()).getEntityType(playerStack.getTag());
            Entity entity = type.create(world);
            entity.updatePosition(pos.getX(), pos.getY(), pos.getZ());
            be.setRenderer(RenderDataProvider.EntityProvider.from(entity));
        } else {
            be.setRenderer(RenderDataProvider.ItemProvider.from(playerStack));
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ProjectorBlockEntity();
    }
}
