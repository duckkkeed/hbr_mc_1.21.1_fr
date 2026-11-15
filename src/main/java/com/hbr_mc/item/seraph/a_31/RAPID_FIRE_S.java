package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;

/**
 * [S] RapidFire。
 *
 * <p>对应 1.12.2 {@code RegisterItem} 里的 {@code new ItemWIPRanged("rapid_fire_s", EnumSeraphType.GUN)}：
 * 原版是占位物品（{@code WIP}），没有技能实现。本移植同样只保留类型与稀有度。
 */
public class RAPID_FIRE_S extends ItemSeraphBase {

    public RAPID_FIRE_S(Settings settings) {
        super(settings, EnumSeraphRarity.S, EnumSeraphType.GUN);
    }
}
