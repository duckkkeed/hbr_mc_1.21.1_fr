package com.hbr_mc.block;

import com.hbr_mc.HBR_MC;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;


public class ModBlocks {

    public static final Block CRYSTAL_ORE = register("crystal_ore", new ExperienceDroppingBlock(ConstantIntProvider.create(0),
            AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(15f, 15f).requiresTool()));

    public static final Block DEEPSLATE_CRYSTAL_ORE = register("deepslate_crystal_ore", new ExperienceDroppingBlock(ConstantIntProvider.create(0),
            AbstractBlock.Settings.copyShallow(CRYSTAL_ORE).mapColor(MapColor.DEEPSLATE_GRAY).strength(15f, 15f).requiresTool()));

    /**
     * 星癌体地毯，对应 1.12.2 的 {@code CANCER_FLOOR = new BlockCarpetBase("cancer_carpet", Material.IRON)
     * .setHardness(4f).setResistance(999f)}：硬度 4、爆炸抗性 999、铁灰色、1/16 高。
     */
    public static final Block CANCER_CARPET = register("cancer_carpet",
            new CancerCarpetBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .strength(4.0F, 999.0F)));

    /**
     * 随机补给箱，对应 1.12.2 的 {@code RANDOM_DROP_BASIC = new BlockCrate("random_1", Material.WOOD)}：
     * 硬度 0.1、木质音效，右键或打碎掉随机补给。
     */
    public static final Block RANDOM_DROP_BASIC = register("random_1",
            new RandomDropCrateBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.OAK_TAN)
                    .strength(0.1F)
                    .sounds(BlockSoundGroup.WOOD)));

    public static void registerBlocks(String id, Block block)
    {
        Item item = Registry.register(Registries.ITEM, Identifier.of(HBR_MC.MOD_ID, id), new BlockItem(block, new Item.Settings()));
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
    }

    public static Block register(String id, Block block) {
        registerBlocks(id, block);
        return Registry.register(Registries.BLOCK, Identifier.of(HBR_MC.MOD_ID, id), block);
    }

    public static void registerModBlocks()
    {
        HBR_MC.LOGGER.info("Registering Mod Blocks");
    }

}
