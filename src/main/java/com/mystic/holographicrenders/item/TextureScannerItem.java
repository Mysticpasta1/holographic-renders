package com.mystic.holographicrenders.item;

import com.mystic.holographicrenders.HolographicRenders;
import com.mystic.holographicrenders.client.TextboxScreenRoot;
import com.mystic.holographicrenders.gui.TextboxScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextureScannerItem extends Item {

    public CompoundTag tag = new CompoundTag();

    public TextureScannerItem() {
        super(new Settings().maxCount(1).group(HolographicRenders.HOLOGRAPHIC_RENDERS_CREATIVE_TAB));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        ItemStack itemStack = player.getStackInHand(hand);

        if (!world.isClient) return TypedActionResult.success(itemStack);

        MinecraftClient.getInstance().openScreen(new TextboxScreen(new TextboxScreenRoot()));

        if(player.isSneaking() && itemStack.getOrCreateTag().contains("URL")) {
            itemStack.getOrCreateTag().remove("URL");
        } else {
            if(new TextboxScreenRoot().getURL() != null) {
                CompoundTag compoundTag = itemStack.getOrCreateTag();
                compoundTag.putString("URL", new TextboxScreenRoot().getURL().toString());
                itemStack.toTag(compoundTag);
                this.tag = compoundTag;
            }
        }

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        CompoundTag stackTag = stack.getOrCreateTag();

        if (stackTag.contains("URL")) {
            tooltip.add(new LiteralText("§2URL: ").append(new TranslatableText(stackTag.getString("URL")).formatted(Formatting.YELLOW)));
        } else {
            tooltip.add(new LiteralText("§7Blank"));
        }
    }
}