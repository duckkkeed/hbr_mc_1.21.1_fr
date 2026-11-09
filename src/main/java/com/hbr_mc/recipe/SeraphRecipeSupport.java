package com.hbr_mc.recipe;

import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.CraftingRecipeInput;

/** 合成配方共用的几个小工具。 */
final class SeraphRecipeSupport {

    private SeraphRecipeSupport() {
    }

    static boolean isSeraph(ItemStack stack) {
        return stack.getItem() instanceof ItemSeraphBase;
    }

    static int nonEmptyCount(CraftingRecipeInput input) {
        int count = 0;
        for (int i = 0; i < input.getSize(); i++) {
            if (!input.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** 3×3 配方用：中心槽位（行优先第 4 格）。 */
    static ItemStack center(CraftingRecipeInput input) {
        return input.getStackInSlot(4);
    }

    /** 3×3 配方用：除中心外 8 格是否全部为指定物品。 */
    static boolean restIs(CraftingRecipeInput input, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (i == 4) continue;
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty() || !stack.isOf(item)) {
                return false;
            }
        }
        return true;
    }
}
