package com.hbr_mc.item.seraph;

import net.minecraft.item.Item;

/**
 * Seraph 物品的 {@link Item.Settings} 工厂，集中处理所有 Seraph 物品共有的配置。
 *
 * <p>对应 1.12.2 版 {@code ItemSeraphBase} 构造里的
 * {@code setMaxStackSize(1)} 以及 {@code getMaxDamage(ItemStack)}：
 *
 * <table>
 *   <caption>耐久对照</caption>
 *   <tr><th>稀有度</th><th>1.12.2 公式</th></tr>
 *   <tr><td>SS</td><td>{@code base * 1.7 + 200}</td></tr>
 *   <tr><td>S</td><td>{@code base * 1.5 + 50}</td></tr>
 *   <tr><td>A</td><td>{@code base * 1.3}</td></tr>
 * </table>
 * 其中原 {@code base = 600 + 7 * 等级}，等级存在 NBT 里、会成长。
 *
 * <p><b>1.21.1 的差异</b>：耐久是 {@code Item.Settings#maxDamage(...)} 决定的固定值组件
 * （{@code DataComponentTypes.MAX_DAMAGE}），不能再像原版那样覆写
 * {@code getMaxDamage(ItemStack)} 按 NBT 动态计算。所以这里的静态值按<b>默认等级 1</b> 代入公式，
 * 作为「尚未写入成长数据」时的兜底；一旦 {@link SeraphUtil#setLevel} 被调用，
 * 就会把按等级算出的上限写进物品堆的 {@code MAX_DAMAGE} 组件（见 {@code ItemSeraphBase#maxDamageForLevel}）。
 */
public final class SeraphItemSettings {

    /** 默认等级（未写入成长数据时按 1 级处理）。 */
    private static final int DEFAULT_LEVEL = 1;

    /** 原版 {@code base = 600 + 7 * level} 在默认等级下的取值。 */
    private static final int BASE_DURABILITY = 600 + 7 * DEFAULT_LEVEL;

    private SeraphItemSettings() {
    }

    /** 默认设置：不可堆叠 + EPIC 稀有度 + 按稀有度给出的耐久。 */
    public static Item.Settings of(EnumSeraphRarity rarity) {
        return new Item.Settings()
                .maxCount(1)
                .rarity(ItemSeraphBase.rarityFor(rarity))
                .maxDamage(durabilityFor(rarity));
    }

    /** 不指定耐久的设置（例如以后要做无耐久物品时使用）。 */
    public static Item.Settings noDurability(EnumSeraphRarity rarity) {
        return new Item.Settings()
                .maxCount(1)
                .rarity(ItemSeraphBase.rarityFor(rarity));
    }

    /** 按稀有度计算耐久上限（见类注释的公式来源）。 */
    public static int durabilityFor(EnumSeraphRarity rarity) {
        return switch (rarity) {
            case SS -> (int) (BASE_DURABILITY * 1.7F) + 200;
            case S -> (int) (BASE_DURABILITY * 1.5F) + 50;
            case A -> (int) (BASE_DURABILITY * 1.3F);
        };
    }
}
