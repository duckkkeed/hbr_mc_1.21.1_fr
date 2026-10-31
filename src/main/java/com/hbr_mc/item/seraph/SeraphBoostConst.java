package com.hbr_mc.item.seraph;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Seraph 成长系统的上限与系数，对应 1.12.2 的 {@code designs/SeraphBoostConst}。
 *
 * <table>
 *   <caption>稀有度对照</caption>
 *   <tr><th>稀有度</th><th>突破上限</th><th>强化上限</th><th>每次突破的属性百分比</th></tr>
 *   <tr><td>SS</td><td>4</td><td>57</td><td>10%</td></tr>
 *   <tr><td>S</td><td>10</td><td>38</td><td>3%</td></tr>
 *   <tr><td>A</td><td>20</td><td>18</td><td>1%</td></tr>
 * </table>
 *
 * <p>{@link #attrPercentagePerBreak} 依赖属性系统，目前只保留数值，等属性移植后再接。
 */
public final class SeraphBoostConst {

    private SeraphBoostConst() {
    }

    public static int getMaxBreakThrough(ItemStack stack) {
        return getMaxBreakThrough(stack.getItem());
    }

    public static int getMaxBreakThrough(Item item) {
        if (item instanceof ItemSeraphBase base) {
            return getMaxBreakThrough(base.getSeraphRarity());
        }
        return getMaxBreakThrough(EnumSeraphRarity.A);
    }

    public static int getMaxBreakThrough(EnumSeraphRarity rarity) {
        return switch (rarity) {
            case SS -> 4;
            case S -> 10;
            case A -> 20;
        };
    }

    public static int getMaxBoost(ItemStack stack) {
        return getMaxBoost(stack.getItem());
    }

    public static int getMaxBoost(Item item) {
        if (item instanceof ItemSeraphBase base) {
            return getMaxBoost(base.getSeraphRarity());
        }
        return getMaxBoost(EnumSeraphRarity.A);
    }

    public static int getMaxBoost(EnumSeraphRarity rarity) {
        return switch (rarity) {
            case SS -> 57;
            case S -> 38;
            case A -> 18;
        };
    }

    /**
     * 每次突破给「属性百分比」加的系数（对应原版 {@code attrPercentagePerBreak}）。
     *
     * <p>属性系统尚未移植，这里先保留数值，供将来 {@code getAttrValuePercent} 使用。
     */
    public static float attrPercentagePerBreak(EnumSeraphRarity rarity) {
        return switch (rarity) {
            case SS -> 0.1F;
            case S -> 0.03F;
            case A -> 0.01F;
        };
    }
}
