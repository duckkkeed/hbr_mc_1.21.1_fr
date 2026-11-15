package com.hbr_mc.item.seraph;

/** 强化单位是"固定值"还是"百分比"，对应 1.12.2 的 {@code designs/EnumBoostType}。 */
public enum EnumBoostType {
    /** 加在百分比属性修饰符上（{@code ADD_MULTIPLIED_TOTAL}）。 */
    PERCENT,
    /** 直接加到属性值上（{@code ADD_VALUE}）。 */
    FIXED
}
