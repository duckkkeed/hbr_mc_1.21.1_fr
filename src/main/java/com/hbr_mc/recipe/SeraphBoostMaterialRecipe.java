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
 * 第 9 次起的强化：1 把炽天使 + 1 个强化材料（诺瓦原水）→ 强化次数 +1。
 *
 * <p>对应 1.12.2 的 {@code RecipeSeraphBoost2}：原版按武器类型（斩/突/打）要求对应的材料，
 * 但 1.21.1 这边大部分炽天使物品还没有设置 {@code seraphType}（为 {@code null}），
 * 无法可靠判断类型，所以这里接受三种强化材料中的任意一种。
 */
public class SeraphBoostMaterialRecipe extends SpecialCraftingRecipe {

    private static final int MATERIAL_LIMIT = 8;

    public SeraphBoostMaterialRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (SeraphRecipeSupport.nonEmptyCount(input) != 2) {
            return false;
        }
        ItemStack seraph = ItemStack.EMPTY;
        boolean material = false;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (SeraphRecipeSupport.isSeraph(stack)) {
                if (!seraph.isEmpty()) return false;
                seraph = stack;
            } else if (isBoostMaterial(stack)) {
                if (material) return false;
                material = true;
            } else {
                return false;
            }
        }
        return !seraph.isEmpty() && material && SeraphUtil.getBoost(seraph) >= MATERIAL_LIMIT;
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

    private static boolean isBoostMaterial(ItemStack stack) {
        return stack.isOf(ModItems.BOOST_1_A)
                || stack.isOf(ModItems.BOOST_1_B)
                || stack.isOf(ModItems.BOOST_1_C);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SERAPH_BOOST_MATERIAL;
    }
}
