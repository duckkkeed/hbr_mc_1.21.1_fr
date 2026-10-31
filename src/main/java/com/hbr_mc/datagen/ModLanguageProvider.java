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

        translationBuilder.add(ModItems.LOTTERY, "Lottery Ticket");
        translationBuilder.add(ModItems.GEM_R, "Ruby");
        translationBuilder.add(ModItems.GEM_Y, "Topaz");
        translationBuilder.add(ModItems.GEM_U, "Sapphire");
        translationBuilder.add(ModItems.GEM_W, "Pearl");
        translationBuilder.add(ModItems.GEM_B, "Onyx");
        translationBuilder.add(ModItems.GEM_AMBER, "Jade Amber");
        translationBuilder.add(ModItems.BOOST_1_A, "Boost Material (Slash)");
        translationBuilder.add(ModItems.BOOST_1_B, "Boost Material (Shoot)");
        translationBuilder.add(ModItems.BOOST_1_C, "Boost Material (Smash)");
        translationBuilder.add(ModItems.GEM_DROPLET, "Sol Droplet");
        translationBuilder.add(ModItems.SHARD_SS, "SS Universal Shard");
        translationBuilder.add(ModItems.XP_ITEM_1, "Exp Book");
        translationBuilder.add(ModItems.XP_ITEM_2, "Exp Book II");

        // 物品说明行：客户端 ItemDescTooltips 读取 "<翻译键>.desc"，有译文才显示。
        translationBuilder.add("item.hbr_mc.lottery.desc", "Consume to get a random Seraph.");
        translationBuilder.add("item.hbr_mc.boost_1_a.desc", "Craft with a Seraph to boost it. Only works from the 9th boost onward.");
        translationBuilder.add("item.hbr_mc.boost_1_b.desc", "Craft with a Seraph to boost it. Only works from the 9th boost onward.");
        translationBuilder.add("item.hbr_mc.boost_1_c.desc", "Craft with a Seraph to boost it. Only works from the 9th boost onward.");
        translationBuilder.add("item.hbr_mc.xp_item_1.desc", "Craft with a Seraph to give it a little XP.");
        translationBuilder.add("item.hbr_mc.xp_item_2.desc", "Craft with a Seraph to give it a good amount of XP.");
        translationBuilder.add("item.hbr_mc.shard_ss.desc", "Shard + A-rank Seraph = SS, stats preserved.");
        translationBuilder.add("item.hbr_mc.crystal_lottery.desc", "Combine 3x2 to get a lottery ticket.");

        translationBuilder.add(ModBlocks.CRYSTAL_ORE, "Free Crystal Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_CRYSTAL_ORE, "Deepslate Free Crystal Ore");
        translationBuilder.add("itemGroup.hbr_mc.tab_1", "Heaven Burns Red");
        translationBuilder.add("itemGroup.hbr_mc.tab_2", "Heaven Burns Red Spawn");
    }
}
