package com.mystic.holographicrenders.blocks.projector;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ProjectorBlock extends BlockWithEntity{

    public static final DirectionProperty PROPERTY_FACING = Properties.FACING;

    private static final VoxelShape[] SHAPES = {
            //NORTH
            Stream.of(
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
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get(),
            //EAST
            Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get(),
            //SOUTH
            Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get(),
            //WEST
            Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get(),
            //DOWN
            Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get(),
            //UP : default
            Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get()
};

    public ProjectorBlock() {
        super(Settings.copy(Blocks.IRON_BLOCK).nonOpaque());
        this.setDefaultState(this.getStateManager().getDefaultState().with(PROPERTY_FACING, Direction.NORTH));
    }


    @Override
    public VoxelShape getOutlineShape(@NotNull BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (getFacing(state)) {
            case NORTH: return SHAPES[0];
            case EAST:  return SHAPES[1];
            case SOUTH: return SHAPES[2];
            case WEST:  return SHAPES[3];
            case DOWN: return SHAPES[4];
            default:  return SHAPES[5];
        }
    }

    private static @NotNull Direction getFacing(@NotNull BlockState state) {
        return state.get(PROPERTY_FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (world.isClient) return ActionResult.SUCCESS;

        NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

        if (screenHandlerFactory != null) {
            player.openHandledScreen(screenHandlerFactory);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(PROPERTY_FACING, rot.rotate(state.get(PROPERTY_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(PROPERTY_FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROPERTY_FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(PROPERTY_FACING, context.getPlayerLookDirection().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ProjectorBlockEntity) {
                ItemScatterer.spawn(world, pos, (ProjectorBlockEntity)blockEntity);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ProjectorBlockEntity();
    }
}
