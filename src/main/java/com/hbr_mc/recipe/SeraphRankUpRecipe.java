package com.hbr_mc.recipe;

import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/**
 * A → SS 升阶：3×3 里中心放 A 级炽天使、周围 8 个 SS 万能碎片 → 对应的 SS 武器，保留成长数据。
 *
 * <p>对应 1.12.2 的 {@code SeraphRankUp}（原版会复制整份 NBT；这里复制等级/经验/强化/突破/损坏标记）。
 */
public class SeraphRankUpRecipe extends SpecialCraftingRecipe {

    public SeraphRankUpRecipe(CraftingRecipeCategory category) {
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
        return seraph.getSeraphRarity() == EnumSeraphRarity.A
                && rankUpTarget(center.getItem()) != null
                && SeraphRecipeSupport.restIs(input, ModItems.SHARD_SS);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack center = SeraphRecipeSupport.center(input);
        Item target = rankUpTarget(center.getItem());
        if (target == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(target);
        SeraphUtil.copyProgression(center, result);
        return result;
    }

    /** 1.12.2 为每对 A/SS 武器分别注册配方；这里只列出已存在的两组。 */
    private static Item rankUpTarget(Item item) {
        if (item == ModItems.BRAVE_BLUE) {
            return ModItems.BRAVE_BLUE_SS;
        }
        if (item == ModItems.RAPID_FIRE) {
            return ModItems.RAPID_FIRE_SS;
        }
        return null;
    }

    @Override
    public boolean fits(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SERAPH_RANK_UP;
    }
}
