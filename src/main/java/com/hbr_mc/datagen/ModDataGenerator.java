package com.hbr_mc.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModModelProvider::new);
        pack.addProvider((output, registriesFuture) -> new ModLanguageProvider(output, registriesFuture));
        pack.addProvider((output, registriesFuture) -> new ModBlockTagProvider(output, registriesFuture));
        pack.addProvider((output, registriesFuture) -> new ModLootTableProvider(output, registriesFuture));
        pack.addProvider((output, registriesFuture) -> new ModRecipeProvider(output, registriesFuture));
    }
}
