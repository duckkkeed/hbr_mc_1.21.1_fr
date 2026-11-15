package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;

/**
 * [S] BraveBlue。
 *
 * <p>对应 1.12.2 的 {@code new ItemSeraphForNPC("brave_blue_s", EnumSeraphType.DOUBLE_SWORD)}：
 * 原版里 S 级只是"占位/给 NPC 用"的物品，<b>没有技能实现</b>，只有类型、稀有度与耐久。
 * 本移植同样保留为无技能物品，让 A → SS 的升阶链在数据上完整。
 */
public class BRAVE_BLUE_S extends ItemSeraphBase {

    public BRAVE_BLUE_S(Settings settings) {
        super(settings, EnumSeraphRarity.S, EnumSeraphType.DOUBLE_SWORD);
    }
}
