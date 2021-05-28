package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.ProjectorBlock;
import com.mystic.holographicrenders.blocks.ProjectorBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HolographicRenders implements ModInitializer {

	public static final String MOD_ID = "holographic_renders";

	public static final Block PROJECTOR_BLOCK = new ProjectorBlock();
	public static final Item PROJECTOR_ITEM = new BlockItem(PROJECTOR_BLOCK, new Item.Settings());
	public static final BlockEntityType<ProjectorBlockEntity> PROJECTOR_BLOCK_ENTITY = BlockEntityType.Builder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK).build(null);

	@Override
	public void onInitialize() {

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "projector"), PROJECTOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "projector"), PROJECTOR_ITEM);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "projector"), PROJECTOR_BLOCK_ENTITY);

	}
}
