package com.hbr_mc.recipe;

import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 前 8 次强化：1 把炽天使 + 5 个癌壳 → 强化次数 +1（对应 1.12.2 的 {@code RecipeSeraphBoost}）。
 *
 * <p>原版用 5 个癌壳强化到第 8 次为止，第 9 次起改用对应的强化材料（见 {@link SeraphBoostMaterialRecipe}）。
 * 这里同样只在 {@code boost < 8} 时匹配。
 */
public class SeraphBoostShellRecipe extends SpecialCraftingRecipe {

    private static final int SHELL_COST = 5;
    private static final int SHELL_LIMIT = 8;

    public SeraphBoostShellRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (SeraphRecipeSupport.nonEmptyCount(input) != SHELL_COST + 1) {
            return false;
        }
        ItemStack seraph = ItemStack.EMPTY;
        int shells = 0;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (SeraphRecipeSupport.isSeraph(stack)) {
                if (!seraph.isEmpty()) return false;
                seraph = stack;
            } else if (stack.isOf(ModItems.CANCER_SHELL)) {
                shells++;
            } else {
                return false;
            }
        }
        return !seraph.isEmpty() && shells == SHELL_COST && SeraphUtil.getBoost(seraph) < SHELL_LIMIT;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (SeraphRecipeSupport.isSeraph(stack)) {
                ItemStack result = stack.copy();
                SeraphUtil.addBoost(result, 1);
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
        return ModRecipes.SERAPH_BOOST_SHELL;
    }
}
