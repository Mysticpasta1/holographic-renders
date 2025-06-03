package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.*;
import com.mystic.holographicrenders.network.LightPacket;
import com.mystic.holographicrenders.network.RotatePacket;
import com.mystic.holographicrenders.network.SpinPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod("holographic_renders")
public class HolographicRenders {

    public static final String MOD_ID = "holographic_renders";

    public static final DeferredRegister<CreativeModeTab> ITEM_GROUPS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), MOD_ID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<MenuType<?>> SCREEN_HANDLERS = DeferredRegister.create(BuiltInRegistries.MENU.key(), MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final String PROJECTOR_ID = "projector";
    public static final RegistryObject<Block> PROJECTOR_BLOCK = BLOCKS.register(PROJECTOR_ID, ProjectorBlock::new);
    public static final RegistryObject<Item> AREA_SCANNER = ITEMS.register("area_scanner", AreaScannerItem::new);
    public static final RegistryObject<Item> TEXTURE_SCANNER = ITEMS.register("texture_scanner", TextureScannerItem::new);
    public static final RegistryObject<Item> ENTITY_SCANNER = ITEMS.register("entity_scanner", EntityScannerItem::new);
    public static final RegistryObject<Item> PROJECTOR_ITEM = ITEMS.register(PROJECTOR_ID, () -> new BlockItem(PROJECTOR_BLOCK.get(), new Item.Properties()));
    public static final CreativeModeTab owo = CreativeModeTab.builder().icon(() -> HolographicRenders.PROJECTOR_BLOCK.get().asItem().getDefaultInstance()).title(Component.literal("Holographic Renders")).displayItems((displayContext, entries) -> {
        entries.accept(AREA_SCANNER.get());
        entries.accept(ENTITY_SCANNER.get());
        entries.accept(TEXTURE_SCANNER.get());
        entries.accept(PROJECTOR_ITEM.get());
    }).build();
    public static final RegistryObject<CreativeModeTab> OWO = ITEM_GROUPS.register("group", () -> owo);
    public static final RegistryObject<BlockEntityType<ProjectorBlockEntity>> PROJECTOR_BLOCK_ENTITY = TILE_ENTITIES.register(PROJECTOR_ID, () -> FabricBlockEntityTypeBuilder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<ProjectorScreenHandler>> PROJECTOR_SCREEN_HANDLER = SCREEN_HANDLERS.register("projector_screen", () -> new ExtendedScreenHandlerType<>(ProjectorScreenHandler::new));

    public HolographicRenders(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        ITEM_GROUPS.register(bus);
        TILE_ENTITIES.register(bus);
        SCREEN_HANDLERS.register(bus);

        ServerPlayNetworking.registerGlobalReceiver(LightPacket.ACTION_REQUEST_ID, LightPacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(SpinPacket.ACTION_REQUEST_ID, SpinPacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(RotatePacket.ACTION_REQUEST_ID, RotatePacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "url_packet"), (server, player, handler, buf, responseSender) -> {
            String url = buf.readUtf();
            ItemStack stack = player.getItemInHand(buf.readEnum(InteractionHand.class));
            server.execute(() -> {
                if(stack.getItem() instanceof TextureScannerItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putString("URL", url);
                    stack.save(tag);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "light_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean lights = buf.readBoolean();
            server.execute(() -> {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setLightEnabled(lights);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "spin_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            boolean spin = buf.readBoolean();
            server.execute(() -> {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setSpinEnabled(spin);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ResourceLocation.fromNamespaceAndPath(HolographicRenders.MOD_ID, "rotate_packet"), (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int rotation = buf.readInt();
            server.execute(() -> {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if(blockEntity instanceof ProjectorBlockEntity){
                    ((ProjectorBlockEntity) blockEntity).setRotation(rotation);
                }
            });
        });
    }
}
