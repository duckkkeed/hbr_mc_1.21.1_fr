package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;

/**
 * SS 级 Seraph 物品 {@code rapid_fire_ss}（终焉 Spitfire）。
 *
 * <p>右键传送、冷却、耐久消耗、蓄力技能流程等行为都继承自 {@link ItemSeraphBase}，
 * 本类只负责声明稀有度与武器类型。
 */
public class RAPID_FIRE_SS extends ItemSeraphBase {

    public RAPID_FIRE_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.GUN);
    }
}
