package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;

/**
 * [SS] SupremeEdge。
 *
 * <p>对应 1.12.2 {@code new ItemWIPRanged("supreme_edge_ss", EnumSeraphType.GUN)}：占位物品，无技能
 * （原版把 SS 形态留在了 {@code WIP} 状态，这里照实移植为无技能物品，不臆造技能）。
 */
public class SUPREME_EDGE_SS extends ItemSeraphBase {

    public SUPREME_EDGE_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.GUN);
    }
}
