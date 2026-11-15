package com.hbr_mc.item.seraph;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

/**
 * "破坏型"炽天使（镰刀类）的基类，移植自 1.12.2 的 {@code ItemSeraphBlaster}。
 *
 * <p>原版覆写了 {@code getDestroySpeed} 恒为 {@code 80.0F}，并让 {@code getHarvestLevel} 返回 -1：
 * 挖得飞快，但拿不到需要工具等级的战利品。1.21.1 上：
 * <ul>
 *   <li>{@code getDestroySpeed} → {@link Item#getMiningSpeed}；</li>
 *   <li>{@code getHarvestLevel = -1} → 不覆写 {@link Item#isSuitableFor}（默认 false），
 *       效果同样是"不提供工具等级"。</li>
 * </ul>
 */
public class ItemSeraphBlaster extends ItemSeraphBase {

    /** 挖掘速度（原版 {@code getDestroySpeed} 恒 80）。 */
    private static final float MINING_SPEED = 80.0F;

    public ItemSeraphBlaster(Settings settings, EnumSeraphRarity rarity) {
        this(settings, rarity, EnumSeraphType.SCYTHE);
    }

    public ItemSeraphBlaster(Settings settings, EnumSeraphRarity rarity, EnumSeraphType type) {
        super(settings, rarity, type);
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        return MINING_SPEED;
    }
}
