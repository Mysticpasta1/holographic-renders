package com.mystic.holographicrenders.blocks.projector;

import com.mystic.holographicrenders.network.LightPacket;
import com.mystic.holographicrenders.network.RotatePacket;
import com.mystic.holographicrenders.network.SpinPacket;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ProjectorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape[] SHAPES = {
            //NORTH
            Stream.of(
                    Block.box(2, 14.000000000000009, 1.800183982822018, 15, 15.000000000000009, 2.8001839828220163),
                    Block.box(1, 1.0000000000000062, 1.800183982822018, 14, 2.000000000000006, 2.8001839828220163),
                    Block.box(14, 0.9999999999999849, 1.800183982822011, 15, 13.999999999999988, 2.800183982822009),
                    Block.box(1, 2.000000000000006, 1.800183982822018, 2, 15.000000000000009, 2.8001839828220163),
                    Block.box(6.25, 6.250000000000001, 3.3001839828220128, 9.75, 9.750000000000002, 4.050183982822013),
                    Block.box(2, 1.999999999999985, 2.5501839828220128, 14, 13.999999999999988, 3.3001839828220128),
                    Block.box(7.5, 7.500000000000001, 0.1751839828220323, 8.5, 8.500000000000002, 2.5501839828220305),
                    Block.box(6, 6.000000000000001, 5.653737373415293, 10, 10.000000000000002, 6.653737373415293),
                    Block.box(3, 2.999999999999999, 6.653737373415293, 13, 13.000000000000002, 15.903737373415296),
                    Block.box(7, 7.000000000000001, 3.903737373415293, 9, 9.000000000000002, 6.903737373415293)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
            //EAST
            Stream.of(
                    Block.box(13.199816017177985, 14.00000000000001, 1.9999999999999947, 14.199816017177984, 15.00000000000001, 14.999999999999996),
                    Block.box(13.199816017177985, 1.000000000000008, 0.9999999999999947, 14.199816017177984, 2.000000000000008, 13.999999999999996),
                    Block.box(13.199816017177993, 0.9999999999999867, 13.999999999999996, 14.19981601717799, 13.99999999999999, 14.999999999999996),
                    Block.box(13.199816017177985, 2.000000000000008, 0.9999999999999947, 14.199816017177984, 15.00000000000001, 1.9999999999999947),
                    Block.box(11.949816017177989, 6.250000000000003, 6.2499999999999964, 12.699816017177989, 9.750000000000004, 9.749999999999996),
                    Block.box(12.699816017177989, 1.9999999999999867, 1.9999999999999947, 13.449816017177989, 13.99999999999999, 13.999999999999996),
                    Block.box(13.449816017177971, 7.500000000000003, 7.4999999999999964, 15.82481601717797, 8.500000000000004, 8.499999999999996),
                    Block.box(9.346262626584709, 6.000000000000003, 5.9999999999999964, 10.346262626584709, 10.000000000000004, 9.999999999999996),
                    Block.box(0.0962626265847053, 3.000000000000001, 2.9999999999999964, 9.346262626584709, 13.000000000000004, 12.999999999999996),
                    Block.box(9.096262626584709, 7.000000000000003, 6.9999999999999964, 12.096262626584709, 9.000000000000004, 8.999999999999996)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
            //SOUTH
            Stream.of(
                    Block.box(1, 14.000000000000007, 13.199816017177978, 14, 15.000000000000007, 14.19981601717798),
                    Block.box(2, 1.0000000000000062, 13.199816017177978, 15, 2.000000000000006, 14.19981601717798),
                    Block.box(1, 0.9999999999999849, 13.199816017177985, 2, 13.999999999999986, 14.199816017177987),
                    Block.box(14, 2.000000000000006, 13.199816017177978, 15, 15.000000000000007, 14.19981601717798),
                    Block.box(6.25, 6.249999999999999, 11.949816017177982, 9.75, 9.75, 12.699816017177982),
                    Block.box(2, 1.999999999999985, 12.699816017177984, 14, 13.999999999999986, 13.449816017177984),
                    Block.box(7.5, 7.499999999999999, 13.449816017177964, 8.5, 8.5, 15.824816017177966),
                    Block.box(6, 5.999999999999999, 9.346262626584702, 10, 10, 10.346262626584702),
                    Block.box(3, 2.999999999999999, 0.09626262658470353, 13, 13, 9.346262626584702),
                    Block.box(7, 6.999999999999999, 9.096262626584702, 9, 9, 12.096262626584702)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
            //WEST
            Stream.of(
                    Block.box(3.903737373415294, 7, 7, 6.903737373415293, 9, 9),
                    Block.box(2.55018398282201, 1.9999999999999858, 2, 3.30018398282201, 13.999999999999986, 14),
                    Block.box(1.8001839828220172, 2.000000000000007, 14, 2.800183982822017, 15.000000000000007, 15),
                    Block.box(1.80018398282201, 0.9999999999999858, 1, 2.80018398282201, 13.999999999999986, 2),
                    Block.box(1.8001839828220172, 1.000000000000007, 2, 2.800183982822017, 2.000000000000007, 15),
                    Block.box(1.8001839828220172, 14.000000000000007, 1, 2.800183982822017, 15.000000000000007, 14),
                    Block.box(0.1751839828220314, 7.5, 7.5, 2.5501839828220314, 8.5, 8.5),
                    Block.box(3.3001839828220136, 6.25, 6.25, 4.050183982822013, 9.75, 9.75),
                    Block.box(6.653737373415293, 3, 3, 15.903737373415293, 13, 13),
                    Block.box(5.653737373415293, 6, 6, 6.653737373415293, 10, 10)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
            //DOWN
            Stream.of(
                    Block.box(1, 1.8001839828220163, 14.000000000000009, 14, 2.8001839828220163, 15.000000000000009),
                    Block.box(2, 1.8001839828220163, 1.0000000000000089, 15, 2.8001839828220163, 2.000000000000009),
                    Block.box(1, 1.8001839828220092, 0.9999999999999876, 2, 2.800183982822009, 13.999999999999988),
                    Block.box(14, 1.8001839828220163, 2.000000000000009, 15, 2.8001839828220163, 15.000000000000009),
                    Block.box(6.25, 3.3001839828220128, 6.250000000000002, 9.75, 4.050183982822013, 9.750000000000002),
                    Block.box(2, 2.550183982822011, 1.9999999999999876, 14, 3.300183982822011, 13.999999999999988),
                    Block.box(7.5, 0.17518398282203052, 7.500000000000002, 8.5, 2.5501839828220305, 8.500000000000002),
                    Block.box(6, 5.653737373415293, 6.000000000000002, 10, 6.653737373415293, 10.000000000000002),
                    Block.box(3, 6.653737373415293, 3.0000000000000018, 13, 15.903737373415293, 13.000000000000002),
                    Block.box(7, 3.903737373415293, 7.000000000000002, 9, 6.903737373415293, 9.000000000000002)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get(),
            //UP : default
            Stream.of(
                    Block.box(7, 9, 7, 9, 12, 9),
                    Block.box(3, 0, 3, 13, 9.25, 13),
                    Block.box(6, 9.25, 6, 10, 10.25, 10),
                    Block.box(7.5, 13.35355, 7.5, 8.5, 15.72855, 8.5),
                    Block.box(2, 12.60355, 2, 14, 13.35355, 14),
                    Block.box(6.25, 11.85355, 6.25, 9.75, 12.60355, 9.75),
                    Block.box(14, 13.10355, 1, 15, 14.10355, 14),
                    Block.box(1, 13.10355, 2, 2, 14.10355, 15),
                    Block.box(2, 13.10355, 14, 15, 14.10355, 15),
                    Block.box(1, 13.10355, 1, 14, 14.10355, 2)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get()
    };

    public ProjectorBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion().lightLevel((state) -> 7));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getShape(@NotNull BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (getFacing(state)) {
            case NORTH:
                return SHAPES[0];
            case EAST:
                return SHAPES[1];
            case SOUTH:
                return SHAPES[2];
            case WEST:
                return SHAPES[3];
            case DOWN:
                return SHAPES[4];
            default:
                return SHAPES[5];
        }
    }

    public static Direction getFacing(@NotNull BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (world.isClientSide) return InteractionResult.SUCCESS;

        ExtendedScreenHandlerFactory screenHandlerFactory = (ExtendedScreenHandlerFactory) state.getMenuProvider(world, pos);
        ProjectorBlockEntity be = (ProjectorBlockEntity) screenHandlerFactory;

        if (screenHandlerFactory != null) {
            player.openMenu(screenHandlerFactory);
            ((ServerPlayer) player).connection.send(LightPacket.createUpdate(be.lightsEnabled()));
            ((ServerPlayer) player).connection.send(SpinPacket.createUpdate(be.spinEnabled()));
            ((ServerPlayer) player).connection.send(RotatePacket.createUpdate(be.getRotation()));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ProjectorBlockEntity) {
                Containers.dropContents(world, pos, (ProjectorBlockEntity) blockEntity);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        updateAlpha(world, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        updateAlpha(world, pos);
    }

    private void updateAlpha(Level world, BlockPos pos) {
        ProjectorBlockEntity be = (ProjectorBlockEntity) world.getBlockEntity(pos);
        be.setAlpha(Math.max(0.01f, Math.min((15 - world.getBestNeighborSignal(pos)) / 15f, 1f)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProjectorBlockEntity(pos, state);
    }
}
