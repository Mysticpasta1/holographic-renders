package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.client.TextboxScreenRoot;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.AreaScannerItem;
import com.mystic.holographicrenders.item.EntityScannerItem;
import com.mystic.holographicrenders.item.TextureScannerItem;
import com.mystic.holographicrenders.item.WidgetScannerItem;
import com.mystic.holographicrenders.item.WidgetType;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.impl.LibGuiCommon;
import io.github.cottonmc.cotton.gui.impl.client.ItemUseChecker;
import io.github.cottonmc.cotton.gui.impl.client.LibGuiClient;
import io.netty.buffer.ByteBufUtil;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
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
    public static final Identifier TEXTBOX_ID = new Identifier(MOD_ID, "textbox");
    public static final ScreenHandlerType<ProjectorScreenHandler> PROJECTOR_SCREEN_HANDLER;

    static {
        PROJECTOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "projector_screen"), ProjectorScreenHandler::new);
    }

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

        ServerPlayNetworking.registerGlobalReceiver(ProjectorScreenPacket.ACTION_REQUEST_ID, ProjectorScreenPacket::onActionRequest);
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
    }
}
