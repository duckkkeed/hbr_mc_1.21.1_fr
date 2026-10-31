package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.hbr_mc.item.food.KnifeCutNoodles;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.SeraphItemSettings;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE_S;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE_SS;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE_S;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE_SS;

public class ModItems {

    // Seraph 系物品：统一继承 ItemSeraphBase，稀有度在各自类里给出（A / S / SS）；
    // Settings 统一走 SeraphItemSettings（不可堆叠 / 稀有度 / 按稀有度的耐久）。
    public static final Item BRAVE_BLUE = registerItem("brave_blue", new BRAVE_BLUE(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item BRAVE_BLUE_S = registerItem("brave_blue_s", new BRAVE_BLUE_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item BRAVE_BLUE_SS = registerItem("brave_blue_ss", new BRAVE_BLUE_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item RAPID_FIRE = registerItem("rapid_fire", new RAPID_FIRE(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item RAPID_FIRE_S = registerItem("rapid_fire_s", new RAPID_FIRE_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item RAPID_FIRE_SS = registerItem("rapid_fire_ss", new RAPID_FIRE_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item RAW_CRYSTAL = registerItem("raw_crystal", new Item(new Item.Settings()));
    public static final Item CRYSTAL_LOTTERY = registerItem("crystal_lottery", new Item(new Item.Settings()));
    public static final Item LOTTERY = registerItem("lottery", new Item(new Item.Settings()));

    public static final Item GEM_R = registerItem("gem_red", new Item(new Item.Settings()));
    public static final Item GEM_Y = registerItem("gem_yellow", new Item(new Item.Settings()));
    public static final Item GEM_U = registerItem("gem_blue", new Item(new Item.Settings()));
    public static final Item GEM_W = registerItem("gem_white", new Item(new Item.Settings()));
    public static final Item GEM_B = registerItem("gem_black", new Item(new Item.Settings()));
    public static final Item GEM_AMBER = registerItem("gem_amber", new Item(new Item.Settings()));

    public static final Item BOOST_1_A = registerItem("boost_1_a", new Item(new Item.Settings()));
    public static final Item BOOST_1_B = registerItem("boost_1_b", new Item(new Item.Settings()));
    public static final Item BOOST_1_C = registerItem("boost_1_c", new Item(new Item.Settings()));
    public static final Item GEM_DROPLET = registerItem("gem_droplet", new Item(new Item.Settings()));
    public static final Item SHARD_SS = registerItem("shard_ss", new Item(new Item.Settings()));
    public static final Item XP_ITEM_1 = registerItem("xp_item_1", new Item(new Item.Settings()));
    public static final Item XP_ITEM_2 = registerItem("xp_item_2", new Item(new Item.Settings()));

    public static final Item ONION = registerItem("onion", new Item(new Item.Settings().food(ModFoodComponents.ONION)));
    public static final Item KNIFE_CUT_NOODLES = registerItem("knife_cut_noodles", new KnifeCutNoodles(new Item.Settings().food(ModFoodComponents.KNIFE_CUT_NOODLES)));

    private static Item registerItem(String id, Item item)
    {
        return Registry.register(Registries.ITEM, Identifier.of(HBR_MC.MOD_ID, id), item);
    }

    public static void registerModItems()
    {

        HBR_MC.LOGGER.info("Registering Items");
    }
}
