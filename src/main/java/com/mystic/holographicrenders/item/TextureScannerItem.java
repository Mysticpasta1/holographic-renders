package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.gui.TextboxScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextureScannerItem extends Item implements NamedScreenHandlerFactory{
    public TextureScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack itemStack = player.getStackInHand(hand);

        if (world.isClient) return TypedActionResult.success(itemStack);

            BlockPos pos = new BlockPos(player.getPos());
            BlockState state = world.getBlockState(pos);

            NamedScreenHandlerFactory screenHandlerFactory = handlerFactory();

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }

            return TypedActionResult.success(itemStack);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TextboxScreenHandler(syncId, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.of("this text will not matter!!!");
    }

    public static NamedScreenHandlerFactory handlerFactory(){
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.of("Please grab a url!");
            }

            @Override
            public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new TextboxScreenHandler(syncId, new TextureScannerItem());
            }
        };
    }
}