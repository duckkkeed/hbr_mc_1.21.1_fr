package com.hbr_mc.recipe;

import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 同名突破：两把相同的炽天使 → 保留第一把的数据，突破次数 +1。
 *
 * <p>对应 1.12.2 的 {@code SeraphBreakThroughBySame}：第二把必须是<b>未突破</b>的（突破次数为 0），
 * 且结果不能超过该稀有度的突破上限。
 */
public class SeraphBreakthroughSameRecipe extends SpecialCraftingRecipe {

    public SeraphBreakthroughSameRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (SeraphRecipeSupport.nonEmptyCount(input) != 2) {
            return false;
        }
        ItemStack origin = ItemStack.EMPTY;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            // 只要出现非炽天使物品就不匹配（否则「武器 + 经验书」会被这条配方抢走）。
            if (!SeraphRecipeSupport.isSeraph(stack)) {
                return false;
            }
            if (origin.isEmpty()) {
                origin = stack;
            } else if (!stack.isOf(origin.getItem()) || SeraphUtil.getBreakThrough(stack) > 0) {
                return false;
            }
        }
        return !origin.isEmpty() && SeraphUtil.getBreakThrough(origin) < SeraphUtil.getMaxBreakThrough(origin);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (SeraphRecipeSupport.isSeraph(stack)) {
                ItemStack result = stack.copy();
                SeraphUtil.addBreakThrough(result, 1);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SERAPH_BREAKTHROUGH_SAME;
    }
}
