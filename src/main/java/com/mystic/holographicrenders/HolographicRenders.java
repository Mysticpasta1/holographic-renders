package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.client.RenderDataProvider;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.AreaScannerItem;
import com.mystic.holographicrenders.item.EntityScannerItem;
import com.mystic.holographicrenders.item.TextureScannerItem;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;

public class HolographicRenders implements ModInitializer {

    public static final String MOD_ID = "holographic_renders";

    public static final ItemGroup HOLOGRAPHIC_RENDERS_CREATIVE_TAB = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "general")).icon(() -> new ItemStack(HolographicRenders.PROJECTOR_BLOCK)).build().setName("holographic_renders:textures/gui/hologram_tab.png");

    public static final Item AREA_SCANNER = new AreaScannerItem();
    public static final Item TEXTURE_SCANNER = new TextureScannerItem();
    public static final Item ENTITY_SCANNER = new EntityScannerItem();

    public static final Block PROJECTOR_BLOCK = new ProjectorBlock();
    public static final Item PROJECTOR_ITEM = new BlockItem(PROJECTOR_BLOCK, new Item.Settings().group(HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    public static final BlockEntityType<ProjectorBlockEntity> PROJECTOR_BLOCK_ENTITY = BlockEntityType.Builder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK).build(null);

    public static final Identifier PROJECTOR_ID = new Identifier(MOD_ID, "projector");
    public static final Identifier TEXTBOX_ID = new Identifier(MOD_ID, "textbox");
    public static final ScreenHandlerType<ProjectorScreenHandler> PROJECTOR_SCREEN_HANDLER;


    static {
        PROJECTOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(PROJECTOR_ID, ProjectorScreenHandler::new);
    }

    @Override
    public void onInitialize() {

        Registry.register(Registry.BLOCK, PROJECTOR_ID, PROJECTOR_BLOCK);
        Registry.register(Registry.ITEM, PROJECTOR_ID, PROJECTOR_ITEM);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, PROJECTOR_ID, PROJECTOR_BLOCK_ENTITY);

        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "area_scanner"), AREA_SCANNER);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "texture_scanner"), TEXTURE_SCANNER);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "entity_scanner"), ENTITY_SCANNER);

        ServerPlayNetworking.registerGlobalReceiver(ProjectorScreenPacket.ACTION_REQUEST_ID, ProjectorScreenPacket::onActionRequest);
    }
}
