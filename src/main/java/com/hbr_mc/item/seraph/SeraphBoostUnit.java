package com.hbr_mc.item.seraph;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * 一次强化带来的单项加成，对应 1.12.2 的 {@code designs/SeraphBoostUnit}。
 *
 * @param attr  受影响的属性；{@code null} 表示"全局"条目（原版里这类条目也是加在某个属性上，
 *              只是用 {@code null} 表示"对所有属性都算"——见 {@link SeraphAttrData} 的注释）
 * @param value 数值；{@link EnumBoostType#PERCENT} 时是比例（0.1 = +10%）
 * @param type  固定值还是百分比
 */
public record SeraphBoostUnit(RegistryEntry<EntityAttribute> attr, float value, EnumBoostType type) {

    public SeraphBoostUnit(RegistryEntry<EntityAttribute> attr, float value) {
        this(attr, value, EnumBoostType.FIXED);
    }
}
