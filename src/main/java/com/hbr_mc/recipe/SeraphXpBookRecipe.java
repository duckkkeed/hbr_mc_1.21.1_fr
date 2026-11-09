package com.hbr_mc.recipe;

import com.hbr_mc.item.XpBookItem;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 经验书强化：1 把炽天使 + 1 本经验书 → 保留原数据并把书里的经验加进去。
 * 对应 1.12.2 的 {@code recipes/RecipeXpBookUp}（数值：报告书 I 500、II 2000）。
 */
public class SeraphXpBookRecipe extends SpecialCraftingRecipe {

    public SeraphXpBookRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (SeraphRecipeSupport.nonEmptyCount(input) != 2) {
            return false;
        }
        boolean hasSeraph = false;
        boolean hasBook = false;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (SeraphRecipeSupport.isSeraph(stack)) {
                if (hasSeraph) return false;
                hasSeraph = true;
            } else if (stack.getItem() instanceof XpBookItem) {
                if (hasBook) return false;
                hasBook = true;
            } else {
                return false;
            }
        }
        return hasSeraph && hasBook;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack seraph = ItemStack.EMPTY;
        int xp = 0;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (SeraphRecipeSupport.isSeraph(stack)) {
                seraph = stack;
            } else if (stack.getItem() instanceof XpBookItem book) {
                xp += book.getXp();
            }
        }
        if (seraph.isEmpty() || xp <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = seraph.copy();
        SeraphUtil.addXP(result, xp, null);
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public net.minecraft.recipe.RecipeSerializer<?> getSerializer() {
        return com.hbr_mc.recipe.ModRecipes.SERAPH_XP_BOOK;
    }
}
