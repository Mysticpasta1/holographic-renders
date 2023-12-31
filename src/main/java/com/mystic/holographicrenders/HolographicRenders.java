package com.mystic.holographicrenders;

import com.mystic.holographicrenders.blocks.projector.ItemProjectionHandler;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlock;
import com.mystic.holographicrenders.blocks.projector.ProjectorBlockEntity;
import com.mystic.holographicrenders.gui.ProjectorScreenHandler;
import com.mystic.holographicrenders.item.AreaScannerItem;
import com.mystic.holographicrenders.item.EntityScannerItem;
import com.mystic.holographicrenders.item.TextureScannerItem;
import com.mystic.holographicrenders.item.WidgetScannerItem;
import com.mystic.holographicrenders.item.WidgetType;
import com.mystic.holographicrenders.network.ProjectorScreenPacket;
import io.netty.buffer.ByteBufUtil;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HolographicRenders implements ModInitializer {

    public static final String MOD_ID = "holographic_renders";

    public static final ItemGroup HOLOGRAPHIC_RENDERS_CREATIVE_TAB = FabricItemGroup.create(new Identifier(MOD_ID, "general")).icon(() -> new ItemStack(HolographicRenders.PROJECTOR_BLOCK)).build().setName("holographic_renders:textures/gui/hologram_tab.png");

    public static final Item AREA_SCANNER = new AreaScannerItem();
    public static final Item TEXTURE_SCANNER = new TextureScannerItem();
    public static final Item ENTITY_SCANNER = new EntityScannerItem();
    public static final Item WIDGET_SCANNER = new WidgetScannerItem();

    public static final Block PROJECTOR_BLOCK = new ProjectorBlock();
    public static final Item PROJECTOR_ITEM = new BlockItem(PROJECTOR_BLOCK, new Item.Settings().group(HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    public static final BlockEntityType<ProjectorBlockEntity> PROJECTOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(ProjectorBlockEntity::new, PROJECTOR_BLOCK).build(null);

    public static final Identifier PROJECTOR_ID = new Identifier(MOD_ID, "projector");
    public static final Identifier TEXTBOX_ID = new Identifier(MOD_ID, "textbox");
    public static final ScreenHandlerType<ProjectorScreenHandler> PROJECTOR_SCREEN_HANDLER;

    static {
        PROJECTOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(PROJECTOR_ID, ProjectorScreenHandler::new);
    }

    @Override
    public void onInitialize() {

        Registry.register(Registries.BLOCK, PROJECTOR_ID, PROJECTOR_BLOCK);
        Registry.register(Registries.ITEM, PROJECTOR_ID, PROJECTOR_ITEM);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, PROJECTOR_ID, PROJECTOR_BLOCK_ENTITY);

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "area_scanner"), AREA_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "texture_scanner"), TEXTURE_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "entity_scanner"), ENTITY_SCANNER);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "widget_scanner"), WIDGET_SCANNER);

        ServerPlayNetworking.registerGlobalReceiver(ProjectorScreenPacket.ACTION_REQUEST_ID, ProjectorScreenPacket::onActionRequest);
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(HolographicRenders.MOD_ID, "url_packet"), (server, player, handler, buf, responseSender) -> {
            String url = buf.readString();
            Hand hand = buf.readEnumConstant(Hand.class);
            server.execute(() -> {
                ItemStack stack = player.getStackInHand(hand);
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
