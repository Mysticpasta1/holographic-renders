package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.ProjectorBlock;
import com.mystic.holographicrenders.blocks.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.HologramScreenHandler;
import com.mystic.holographicrenders.item.AreaRenderItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HolographicRenders implements ModInitializer {

	public static final String MOD_ID = "holographic_renders";

	public static final Block PROJECTOR_BLOCK = new ProjectorBlock();
	public static final Item PROJECTOR_ITEM = new BlockItem(PROJECTOR_BLOCK, new Item.Settings());
	public static final Item AREA_RENDER_ITEM = new AreaRenderItem();
	public static final BlockEntityType<ProjectorBlockEntity> PROJECTOR_BLOCK_ENTITY = BlockEntityType.Builder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK).build(null);

	public static final Identifier PROJECTOR_ID = new Identifier(MOD_ID, "projector");
	public static final ScreenHandlerType<HologramScreenHandler> HOLOGRAM_SCREEN_HANDLER;
	static {
		HOLOGRAM_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(PROJECTOR_ID, HologramScreenHandler::new);
	}

	@Override
	public void onInitialize() {

		Registry.register(Registry.BLOCK, PROJECTOR_ID, PROJECTOR_BLOCK);
		Registry.register(Registry.ITEM, PROJECTOR_ID, PROJECTOR_ITEM);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, PROJECTOR_ID, PROJECTOR_BLOCK_ENTITY);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "area_render_item"), AREA_RENDER_ITEM);

	}
}
