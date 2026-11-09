package com.hbr_mc.item;

import net.minecraft.item.Item;

/**
 * 经验书，对应 1.12.2 的 {@code items/ItemXpBook}。
 *
 * <p>与炽天使一起合成时，把 {@link #getXp()} 点经验加到武器上（见 {@code SeraphXpBookRecipe}）。
 * 数值照搬原版：战斗报告书 I = 500，战斗报告书 II = 2000。
 */
public class XpBookItem extends Item {

    private final int xp;

    public XpBookItem(Settings settings, int xp) {
        super(settings);
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }
}
