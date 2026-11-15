package com.hbr_mc.recipe;

import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 炽天使分解：一把武器 → SS 万能碎片。
 *
 * <p>对应 1.12.2 {@code recipes/SeraphDecomp}（一个继承 {@code ShapedRecipes} 的 1×1 配方）。
 * 原版在 {@code RegisterRecipes#getRegister} 里为每把武器各注册一条：
 * <b>A 级分解得 1 个</b>、<b>SS 级分解得 4 个</b>。因为产物数量只取决于稀有度，
 * 这里合并成一条特殊配方，匹配条件为"合成格里只有一件炽天使"。
 *
 * <p>原版的分解<b>不保留</b>成长数据（产物是碎片，武器本身消失），这里同样如此。
 */
public class SeraphDecompRecipe extends SpecialCraftingRecipe {

    /** A 级（以及 S 级）分解得到的碎片数。 */
    private static final int SHARDS_FOR_A = 1;
    /** SS 级分解得到的碎片数。 */
    private static final int SHARDS_FOR_SS = 4;

    public SeraphDecompRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (SeraphRecipeSupport.nonEmptyCount(input) != 1) {
            return false;
        }
        ItemStack stack = singleStack(input);
        return SeraphRecipeSupport.isSeraph(stack);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack stack = singleStack(input);
        if (!(stack.getItem() instanceof ItemSeraphBase seraph)) {
            return ItemStack.EMPTY;
        }
        int shards = seraph.getSeraphRarity() == EnumSeraphRarity.SS ? SHARDS_FOR_SS : SHARDS_FOR_A;
        return new ItemStack(ModItems.SHARD_SS, shards);
    }

    private static ItemStack singleStack(CraftingRecipeInput input) {
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return stack;
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
        return ModRecipes.SERAPH_DECOMP;
    }
}
