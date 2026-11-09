package com.hbr_mc.recipe;

import com.hbr_mc.HBR_MC;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * 炽天使成长相关的特殊合成配方注册（对应 1.12.2 {@code init/RegisterRecipes} 里的自定义配方）。
 *
 * <p>这些配方需要读取/改写输入物品的成长数据，无法用原版 JSON 配方表达，因此实现为
 * {@link SpecialCraftingRecipe}，并在 {@code data/hbr_mc/recipe/} 下各放一个只含 type/category 的 JSON。
 */
public final class ModRecipes {

    public static final RecipeSerializer<SeraphXpBookRecipe> SERAPH_XP_BOOK =
            register("seraph_xp_book", new SpecialRecipeSerializer<>(SeraphXpBookRecipe::new));

    public static final RecipeSerializer<SeraphBoostShellRecipe> SERAPH_BOOST_SHELL =
            register("seraph_boost_shell", new SpecialRecipeSerializer<>(SeraphBoostShellRecipe::new));

    public static final RecipeSerializer<SeraphBoostMaterialRecipe> SERAPH_BOOST_MATERIAL =
            register("seraph_boost_material", new SpecialRecipeSerializer<>(SeraphBoostMaterialRecipe::new));

    public static final RecipeSerializer<SeraphBreakthroughSameRecipe> SERAPH_BREAKTHROUGH_SAME =
            register("seraph_breakthrough_same", new SpecialRecipeSerializer<>(SeraphBreakthroughSameRecipe::new));

    public static final RecipeSerializer<SeraphBreakthroughShardRecipe> SERAPH_BREAKTHROUGH_SHARD =
            register("seraph_breakthrough_shard", new SpecialRecipeSerializer<>(SeraphBreakthroughShardRecipe::new));

    public static final RecipeSerializer<SeraphRankUpRecipe> SERAPH_RANK_UP =
            register("seraph_rank_up", new SpecialRecipeSerializer<>(SeraphRankUpRecipe::new));

    private ModRecipes() {
    }

    private static <T extends CraftingRecipe> RecipeSerializer<T> register(String id, RecipeSerializer<T> serializer) {
        Registry.register(Registries.RECIPE_SERIALIZER, HBR_MC.id(id), serializer);
        return serializer;
    }

    /** 在 {@code HBR_MC#onInitialize} 里调用一次，触发上面的静态注册。 */
    public static void register() {
    }
}
