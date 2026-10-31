package com.hbr_mc.datagen;

import com.hbr_mc.block.ModBlocks;
import com.hbr_mc.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.CRYSTAL_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.DEEPSLATE_CRYSTAL_ORE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.BRAVE_BLUE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRAVE_BLUE_S, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRAVE_BLUE_SS, Models.GENERATED);

        itemModelGenerator.register(ModItems.RAPID_FIRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAPID_FIRE_S, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAPID_FIRE_SS, Models.GENERATED);

        itemModelGenerator.register(ModItems.RAW_CRYSTAL, Models.GENERATED);
        itemModelGenerator.register(ModItems.CRYSTAL_LOTTERY, Models.GENERATED);
        itemModelGenerator.register(ModItems.ONION, Models.GENERATED);

        // 材料 / 消耗品（1.12.2 的宝石、强化材料、碎片、经验书、抽卡券）
        itemModelGenerator.register(ModItems.LOTTERY, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_R, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_Y, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_U, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_W, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_B, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_AMBER, Models.GENERATED);
        itemModelGenerator.register(ModItems.BOOST_1_A, Models.GENERATED);
        itemModelGenerator.register(ModItems.BOOST_1_B, Models.GENERATED);
        itemModelGenerator.register(ModItems.BOOST_1_C, Models.GENERATED);
        itemModelGenerator.register(ModItems.GEM_DROPLET, Models.GENERATED);
        itemModelGenerator.register(ModItems.SHARD_SS, Models.GENERATED);
        itemModelGenerator.register(ModItems.XP_ITEM_1, Models.GENERATED);
        itemModelGenerator.register(ModItems.XP_ITEM_2, Models.GENERATED);
    }
}
