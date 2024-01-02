package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.*;
import com.mystic.holographicrenders.network.LightPacket;
import com.mystic.holographicrenders.network.RotatePacket;
import com.mystic.holographicrenders.network.SpinPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

public class HolographicRenders implements ModInitializer {

    public static final String MOD_ID = "holographic_renders";

    public static final List<Supplier<? extends ItemConvertible>> MAIN_BLOCKS = new ArrayList<>();
    public static final ItemGroup owo = new FabricItemGroupBuilderImpl().icon(() -> HolographicRenders.PROJECTOR_BLOCK.asItem().getDefaultStack()).displayName(Text.literal("Holographic Renders")).entries((displayContext, entries) -> {
        MAIN_BLOCKS.forEach((itemLike -> entries.add(itemLike.get())));
    }).build();

    public static ItemConvertible addToMainTab (ItemConvertible itemLike) {
        MAIN_BLOCKS.add(() -> itemLike);
        return itemLike;
    }

    public static final Item AREA_SCANNER = new AreaScannerItem();
    public static final Item TEXTURE_SCANNER = new TextureScannerItem();
    public static final Item ENTITY_SCANNER = new EntityScannerItem();
    public static final Item WIDGET_SCANNER = new WidgetScannerItem();

    public static final Block PROJECTOR_BLOCK = new ProjectorBlock();
    public static final Item PROJECTOR_ITEM = new BlockItem(PROJECTOR_BLOCK, new Item.Settings());
    public static final BlockEntityType<ProjectorBlockEntity> PROJECTOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK).build(null);

    public static final Identifier PROJECTOR_ID = new Identifier(MOD_ID, "projector");
    public static final ScreenHandlerType<ProjectorScreenHandler> PROJECTOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "projector_screen"), new ExtendedScreenHandlerType<>(ProjectorScreenHandler::new));
    
    @Override
    public void onInitialize() {
        Properties props = System.getProperties();
        props.setProperty("libgui.allowItemUse", "true");

        Registry.register(Registries.BLOCK, PROJECTOR_ID, PROJECTOR_BLOCK);
        Registry.register(Registries.ITEM, PROJECTOR_ID, PROJECTOR_ITEM);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, PROJECTOR_ID, PROJECTOR_BLOCK_ENTITY);

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "area_scanner"), AREA_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "texture_scanner"), TEXTURE_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "entity_scanner"), ENTITY_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "widget_scanner"), WIDGET_SCANNER);

        addToMainTab(AREA_SCANNER.asItem());
        addToMainTab(TEXTURE_SCANNER.asItem());
        addToMainTab(ENTITY_SCANNER.asItem());
        //addToMainTab(WIDGET_SCANNER.asItem());
        addToMainTab(PROJECTOR_ITEM.asItem());

        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "group"), owo);

        ServerPlayNetworking.registerGlobalReceiver(LightPacket.ACTION_REQUEST_ID, LightPacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(SpinPacket.ACTION_REQUEST_ID, SpinPacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(RotatePacket.ACTION_REQUEST_ID, RotatePacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "url_packet"), (server, player, handler, buf, responseSender) -> {
            String url = buf.readString();
            ItemStack stack = player.getStackInHand(buf.readEnumConstant(Hand.class));
            server.execute(() -> {
                if(stack.getItem() instanceof TextureScannerItem) {
                    NbtCompound tag = stack.getOrCreateNbt();
                    tag.putString("URL", url);
                    stack.writeNbt(tag);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "widget_packet"), (server, player, handler, buf, responseSender) -> {
            Hand hand = buf.readEnumConstant(Hand.class);
            WidgetType type = buf.readEnumConstant(WidgetType.class);
            server.execute(() -> {
                ItemStack stack = player.getStackInHand(hand);
                if(stack.getItem() instanceof WidgetScannerItem) {
                    NbtCompound tag = stack.getOrCreateNbt();
                    tag.putInt("Widget", type.ordinal());
                    stack.writeNbt(tag);
                }
            });
        });


        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "light_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean lights = buf.readBoolean();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setLightEnabled(lights);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "spin_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean spin = buf.readBoolean();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setSpinEnabled(spin);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "rotate_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int rotation = buf.readInt();
            server.execute(() -> {
                BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setRotation(rotation);
                }
            });
        });
    }
}
