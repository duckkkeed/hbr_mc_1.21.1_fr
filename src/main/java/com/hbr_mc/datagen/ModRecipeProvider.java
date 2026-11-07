package com.hbr_mc.datagen;

import com.hbr_mc.block.ModBlocks;
import com.hbr_mc.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // 星癌体地毯：2 个癌壳（横排）→ 64 张，对应 1.12.2 的 assets/hbr_mc/recipes/cancer_floor.json。
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.CANCER_CARPET, 64)
                .pattern("II")
                .input('I', ModItems.CANCER_SHELL)
                .criterion(hasItem(ModItems.CANCER_SHELL), conditionsFromItem(ModItems.CANCER_SHELL))
                .offerTo(exporter);

        // 抽卡券：6 个免费石英（3x2）→ 1 张，对应 1.12.2 的 assets/hbr_mc/recipes/lottery.json。
        // 原版 Seraph 物品的合成配方已移除（现在没有直接合成 Seraph 的配方）。
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LOTTERY, 1)
                .pattern("II")
                .pattern("II")
                .pattern("II")
                .input('I', ModItems.CRYSTAL_LOTTERY)
                .criterion(hasItem(ModItems.CRYSTAL_LOTTERY), conditionsFromItem(ModItems.CRYSTAL_LOTTERY))
                .offerTo(exporter);
    }
}
