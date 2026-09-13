package com.hbr_mc.datagen;

import com.hbr_mc.block.ModBlocks;
import com.hbr_mc.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLanguageProvider extends FabricLanguageProvider {
    public ModLanguageProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModItems.BRAVE_BLUE, "BraveBlue(Attack or Music)");
        translationBuilder.add(ModItems.RAW_CRYSTAL, "Raw Crystal");
        translationBuilder.add(ModBlocks.CRYSTAL_ORE, "Free Crystal Ore");
        translationBuilder.add("itemGroup.hbr_mc.tab_1", "Heaven Burns Red");
        translationBuilder.add("itemGroup.hbr_mc.tab_2", "Heaven Burns Red Spawn");
    }
}
