package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.block.ModBlocks;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 创造模式物品栏。
 *
 * <p>Seraph 武器不是"一个物品一个条目"，而是<b>一个物品三个条目</b>
 * （常规 / 满强化 / 满突破），对应 1.12.2 {@code ItemSeraphBase#getSubItems}：
 * 原版在创造栏里就是这样列出满级样品的，方便直接测试高等级数值。
 * 生成逻辑在 {@link ItemSeraphBase#creativeVariants(Item)}，这里只负责把结果塞进条目列表。
 */
public class ModItemGroups {

    public static final ItemGroup HBR_MC_GROUP_1 = Registry.register(Registries.ITEM_GROUP, Identifier.of(HBR_MC.MOD_ID, "hbr_mc"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.hbr_mc.tab_1"))
                    .icon(() -> new ItemStack(ModItems.BRAVE_BLUE))
                    .entries((displayContext, entries) -> {
                        addSeraphVariants(entries, ModItems.BRAVE_BLUE);
                        addSeraphVariants(entries, ModItems.BRAVE_BLUE_S);
                        addSeraphVariants(entries, ModItems.BRAVE_BLUE_SS);

                        addSeraphVariants(entries, ModItems.RAPID_FIRE);
                        addSeraphVariants(entries, ModItems.RAPID_FIRE_S);
                        addSeraphVariants(entries, ModItems.RAPID_FIRE_SS);

                        addSeraphVariants(entries, ModItems.CLAVIS);
                        addSeraphVariants(entries, ModItems.CLAVIS_S);
                        addSeraphVariants(entries, ModItems.CLAVIS_SS);

                        addSeraphVariants(entries, ModItems.SUPREME_EDGE);
                        addSeraphVariants(entries, ModItems.SUPREME_EDGE_S);
                        addSeraphVariants(entries, ModItems.SUPREME_EDGE_SS);

                        addSeraphVariants(entries, ModItems.GLITTER_SHADOW);
                        addSeraphVariants(entries, ModItems.GLITTER_SHADOW_S);
                        addSeraphVariants(entries, ModItems.GLITTER_SHADOW_SS);

                        addSeraphVariants(entries, ModItems.PHANTOM_WEAVER);
                        addSeraphVariants(entries, ModItems.PHANTOM_WEAVER_S);
                        addSeraphVariants(entries, ModItems.PHANTOM_WEAVER_SS);

                        addSeraphVariants(entries, ModItems.FATAL_NULL);
                        addSeraphVariants(entries, ModItems.FATAL_NULL_SS);

                        addSeraphVariants(entries, ModItems.KAZABANA);

                        entries.add(new ItemStack(ModItems.RAW_CRYSTAL));
                        entries.add(new ItemStack(ModItems.CRYSTAL_LOTTERY));
                        entries.add(new ItemStack(ModItems.LOTTERY));
                        entries.add(new ItemStack(ModBlocks.CRYSTAL_ORE));
                        entries.add(new ItemStack(ModBlocks.DEEPSLATE_CRYSTAL_ORE));
                        entries.add(new ItemStack(ModBlocks.CANCER_CARPET));
                        entries.add(new ItemStack(ModBlocks.RANDOM_DROP_BASIC));
                        entries.add(new ItemStack(ModBlocks.HEAL_STASH));
                        entries.add(new ItemStack(ModBlocks.HEAL_STASH_PERMA));

                        entries.add(new ItemStack(ModItems.GEM_R));
                        entries.add(new ItemStack(ModItems.GEM_Y));
                        entries.add(new ItemStack(ModItems.GEM_U));
                        entries.add(new ItemStack(ModItems.GEM_W));
                        entries.add(new ItemStack(ModItems.GEM_B));
                        entries.add(new ItemStack(ModItems.GEM_AMBER));

                        entries.add(new ItemStack(ModItems.BOOST_1_A));
                        entries.add(new ItemStack(ModItems.BOOST_1_B));
                        entries.add(new ItemStack(ModItems.BOOST_1_C));
                        entries.add(new ItemStack(ModItems.GEM_DROPLET));
                        entries.add(new ItemStack(ModItems.SHARD_SS));
                        entries.add(new ItemStack(ModItems.XP_ITEM_1));
                        entries.add(new ItemStack(ModItems.XP_ITEM_2));

                        entries.add(new ItemStack(ModItems.CANCER_SHELL));
                        entries.add(new ItemStack(ModItems.CANCER_HELMET));
                        entries.add(new ItemStack(ModItems.CANCER_CHESTPLATE));
                        entries.add(new ItemStack(ModItems.CANCER_LEGGINGS));
                        entries.add(new ItemStack(ModItems.CANCER_BOOTS));

                        entries.add(new ItemStack(ModItems.ONION));
                        entries.add(new ItemStack(ModItems.KNIFE_CUT_NOODLES));
                    })
                    .build());

    public static final ItemGroup HBR_MC_GROUP_2 = Registry.register(Registries.ITEM_GROUP, Identifier.of(HBR_MC.MOD_ID, "hbr_mc_2"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.hbr_mc.tab_2"))
                    .icon(() -> new ItemStack(ModItems.C_SLASHER_SPAWN_EGG))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.C_SLASHER_SPAWN_EGG));
                        entries.add(new ItemStack(ModItems.C_CREST_HOPPER_SPAWN_EGG));
                        entries.add(new ItemStack(ModItems.C_SMALL_HOPPER_SPAWN_EGG));
                        entries.add(new ItemStack(ModItems.C_DOLL_SPAWN_EGG));
                        entries.add(new ItemStack(ModItems.C_MARIONETTE_SPAWN_EGG));
                    })
                    .build());

    /** 把一个炽天使物品的三个变体（常规 / 满强化 / 满突破）加进创造栏。 */
    private static void addSeraphVariants(ItemGroup.Entries entries, Item item) {
        if (item instanceof ItemSeraphBase seraph) {
            List<ItemStack> variants = seraph.creativeVariants(item);
            for (ItemStack stack : variants) {
                entries.add(stack);
            }
        } else {
            entries.add(new ItemStack(item));
        }
    }

    public static void registerModItemGroups() {
        HBR_MC.LOGGER.info("Registering Item Groups");
    }

}
