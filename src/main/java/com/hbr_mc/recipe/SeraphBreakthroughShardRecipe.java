package com.hbr_mc.recipe;

import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * 碎片突破：3×3 里中心放 SS 炽天使、周围 8 个 SS 万能碎片 → 突破次数 +1。
 *
 * <p>对应 1.12.2 的 {@code SeraphBreakThroughByShard}。原版注册时把上限写成 5，
 * 但 {@code SeraphBoostConst} 里 SS 的突破上限是 4；这里按实际上限匹配，
 * 避免已经 4 次突破时还匹配成功、白白消耗 8 个碎片。
 */
public class SeraphBreakthroughShardRecipe extends SpecialCraftingRecipe {

    public SeraphBreakthroughShardRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getWidth() != 3 || input.getHeight() != 3) {
            return false;
        }
        ItemStack center = SeraphRecipeSupport.center(input);
        if (!SeraphRecipeSupport.isSeraph(center)) {
            return false;
        }
        ItemSeraphBase seraph = (ItemSeraphBase) center.getItem();
        return seraph.getSeraphRarity() == EnumSeraphRarity.SS
                && SeraphRecipeSupport.restIs(input, ModItems.SHARD_SS)
                && SeraphUtil.getBreakThrough(center) < SeraphUtil.getMaxBreakThrough(center);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack center = SeraphRecipeSupport.center(input);
        if (center.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = center.copy();
        SeraphUtil.addBreakThrough(result, 1);
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SERAPH_BREAKTHROUGH_SHARD;
    }
}
