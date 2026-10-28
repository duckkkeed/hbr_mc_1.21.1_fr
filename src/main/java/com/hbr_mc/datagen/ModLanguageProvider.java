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
        translationBuilder.add(ModItems.BRAVE_BLUE, "Brave Blue (Attack or Music)");
        translationBuilder.add(ModItems.BRAVE_BLUE_S, "Brave Blue S");
        translationBuilder.add(ModItems.BRAVE_BLUE_SS, "Brave Blue SS");

        translationBuilder.add(ModItems.RAPID_FIRE, "Rapid Fire");
        translationBuilder.add(ModItems.RAPID_FIRE_S, "Rapid Fire S");
        translationBuilder.add(ModItems.RAPID_FIRE_SS, "Rapid Fire SS");

        translationBuilder.add(ModItems.RAW_CRYSTAL, "Raw Crystal");
        translationBuilder.add(ModItems.CRYSTAL_LOTTERY, "Crystal Lottery");
        translationBuilder.add(ModItems.ONION, "Onion");

        translationBuilder.add(ModBlocks.CRYSTAL_ORE, "Free Crystal Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_CRYSTAL_ORE, "Deepslate Free Crystal Ore");
        translationBuilder.add("itemGroup.hbr_mc.tab_1", "Heaven Burns Red");
        translationBuilder.add("itemGroup.hbr_mc.tab_2", "Heaven Burns Red Spawn");
    }
}
