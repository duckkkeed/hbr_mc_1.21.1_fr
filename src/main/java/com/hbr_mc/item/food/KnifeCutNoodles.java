package com.hbr_mc.item.food;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KnifeCutNoodles extends Item {
    public KnifeCutNoodles(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.hbr_mc.knife_cut_noodles.line1").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("tooltip.hbr_mc.knife_cut_noodles.line2").formatted(Formatting.GOLD));
    }
}
