package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.AreaScannerItem;
import com.mystic.holographicrenders.item.EntityScannerItem;
import com.mystic.holographicrenders.item.TextureScannerItem;
import com.mystic.holographicrenders.network.ProjectorPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HolographicRenders.MOD_ID)
public class HolographicRenders {

    public static final String MOD_ID = "holographic_renders";

    public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, MOD_ID);

    public static final DeferredRegister<MenuType<?>> SCREEN_HANDLERS =
            DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final String PROJECTOR_ID = "iron_projector";

    public static final DeferredHolder<Block, Block> PROJECTOR_BLOCK =
            BLOCKS.register(PROJECTOR_ID, ProjectorBlock::new);

    public static final DeferredHolder<Item, Item> AREA_SCANNER =
            ITEMS.register("area_scanner", AreaScannerItem::new);

    public static final DeferredHolder<Item, Item> TEXTURE_SCANNER =
            ITEMS.register("texture_scanner", TextureScannerItem::new);

    public static final DeferredHolder<Item, Item> ENTITY_SCANNER =
            ITEMS.register("entity_scanner", EntityScannerItem::new);

    public static final DeferredHolder<Item, Item> PROJECTOR_ITEM =
            ITEMS.register(PROJECTOR_ID,
                    () -> new BlockItem(PROJECTOR_BLOCK.get(), new Item.Properties()));

    private static final CreativeModeTab OWO_TAB = CreativeModeTab.builder()
            .icon(() -> HolographicRenders.PROJECTOR_BLOCK.get().asItem().getDefaultInstance())
            .title(Component.literal("Holographic Renders"))
            .displayItems((displayContext, entries) -> {
                entries.accept(AREA_SCANNER.get());
                entries.accept(ENTITY_SCANNER.get());
                entries.accept(TEXTURE_SCANNER.get());
                entries.accept(PROJECTOR_ITEM.get());
            })
            .build();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OWO =
            ITEM_GROUPS.register("group", () -> OWO_TAB);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProjectorBlockEntity>>
            PROJECTOR_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            PROJECTOR_ID,
            () -> BlockEntityType.Builder.of(
                    ProjectorBlockEntity::new,
                    PROJECTOR_BLOCK.get()
            ).build(null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ProjectorScreenHandler>>
            PROJECTOR_SCREEN_HANDLER = SCREEN_HANDLERS.register(
            "projector_screen",
            () -> IMenuTypeExtension.create(ProjectorScreenHandler::new)
    );

    public HolographicRenders(ModContainer modContainer) {
        IEventBus modBus = modContainer.getEventBus();
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        ITEM_GROUPS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SCREEN_HANDLERS.register(modBus);
    }
}
