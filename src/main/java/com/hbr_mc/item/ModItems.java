package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.hbr_mc.entity.ModEntities;
import com.hbr_mc.item.food.KnifeCutNoodles;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.SeraphItemSettings;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE_S;
import com.hbr_mc.item.seraph.a_31.BRAVE_BLUE_SS;
import com.hbr_mc.item.seraph.a_31.CLAVIS;
import com.hbr_mc.item.seraph.a_31.CLAVIS_S;
import com.hbr_mc.item.seraph.a_31.CLAVIS_SS;
import com.hbr_mc.item.seraph.a_31.GLITTER_SHADOW;
import com.hbr_mc.item.seraph.a_31.GLITTER_SHADOW_S;
import com.hbr_mc.item.seraph.a_31.GLITTER_SHADOW_SS;
import com.hbr_mc.item.seraph.a_31.PHANTOM_WEAVER;
import com.hbr_mc.item.seraph.a_31.PHANTOM_WEAVER_S;
import com.hbr_mc.item.seraph.a_31.PHANTOM_WEAVER_SS;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE_S;
import com.hbr_mc.item.seraph.a_31.RAPID_FIRE_SS;
import com.hbr_mc.item.seraph.a_31.SUPREME_EDGE;
import com.hbr_mc.item.seraph.a_31.SUPREME_EDGE_S;
import com.hbr_mc.item.seraph.a_31.SUPREME_EDGE_SS;
import com.hbr_mc.item.seraph.b_31.FATAL_NULL;
import com.hbr_mc.item.seraph.b_31.FATAL_NULL_SS;
import com.hbr_mc.item.seraph.g_30.KAZABANA;

/**
 * 物品注册表。
 *
 * <h2>Seraph 系物品</h2>
 * 全部继承 {@code ItemSeraphBase}，稀有度在各自类里给出（A / S / SS）；
 * {@code Settings} 统一走 {@link SeraphItemSettings}（不可堆叠 / 稀有度 / 按稀有度的耐久）。
 *
 * <p>与 1.12.2 的 {@code RegisterItem} 对照：
 * <table>
 *   <caption>武器族</caption>
 *   <tr><th>族</th><th>A</th><th>S</th><th>SS</th><th>1.12.2 类</th></tr>
 *   <tr><td>BraveBlue</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 有技能</td><td>ItemBraveBlue / ItemSeraphForNPC / ItemBraveBlueSS</td></tr>
 *   <tr><td>RapidFire</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 有技能</td><td>ItemRapidFire / ItemWIPRanged / ItemRapidFireSS</td></tr>
 *   <tr><td>Clavis</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 有技能</td><td>ItemClavis / ItemSeraphForNPC / ItemClavisSS</td></tr>
 *   <tr><td>GlitterShadow</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 有技能</td><td>ItemGlitterShadowA / ItemSeraphForNPC / ItemGlitterShadowSS</td></tr>
 *   <tr><td>PhantomWeaver</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 有技能</td><td>ItemPhantomWeaver / ItemSeraphForNPC / ItemPhantomWeaverSS</td></tr>
 *   <tr><td>SupremeEdge</td><td>✔ 有技能</td><td>✔ 占位</td><td>✔ 占位</td><td>ItemSupremeEdge / ItemWIPRanged</td></tr>
 *   <tr><td>FatalNull</td><td>✔ 有技能</td><td>—</td><td>✔ 占位</td><td>ItemFatalNull / ItemWIPRanged</td></tr>
 *   <tr><td>Kazabana</td><td>✔ 有技能</td><td>—</td><td>—</td><td>ItemKazabana</td></tr>
 * </table>
 *
 * <p><b>注册 id 的差异</b>：1.12.2 里 SupremeEdge 的 id 与贴图都拼成了 {@code superme_edge}（少一个 r）；
 * 本工程统一改成正确的 {@code supreme_edge}，并把贴图一并改名。
 */
public class ModItems {

    // ---------------------------------------------------------------- 炽天使 --

    public static final Item BRAVE_BLUE = registerItem("brave_blue",
            new BRAVE_BLUE(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item BRAVE_BLUE_S = registerItem("brave_blue_s",
            new BRAVE_BLUE_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item BRAVE_BLUE_SS = registerItem("brave_blue_ss",
            new BRAVE_BLUE_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item RAPID_FIRE = registerItem("rapid_fire",
            new RAPID_FIRE(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item RAPID_FIRE_S = registerItem("rapid_fire_s",
            new RAPID_FIRE_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item RAPID_FIRE_SS = registerItem("rapid_fire_ss",
            new RAPID_FIRE_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item CLAVIS = registerItem("clavis",
            new CLAVIS(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item CLAVIS_S = registerItem("clavis_s",
            new CLAVIS_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item CLAVIS_SS = registerItem("clavis_ss",
            new CLAVIS_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item SUPREME_EDGE = registerItem("supreme_edge",
            new SUPREME_EDGE(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item SUPREME_EDGE_S = registerItem("supreme_edge_s",
            new SUPREME_EDGE_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item SUPREME_EDGE_SS = registerItem("supreme_edge_ss",
            new SUPREME_EDGE_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item GLITTER_SHADOW = registerItem("glitter_shadow",
            new GLITTER_SHADOW(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item GLITTER_SHADOW_S = registerItem("glitter_shadow_s",
            new GLITTER_SHADOW_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item GLITTER_SHADOW_SS = registerItem("glitter_shadow_ss",
            new GLITTER_SHADOW_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item PHANTOM_WEAVER = registerItem("phantom_weaver",
            new PHANTOM_WEAVER(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item PHANTOM_WEAVER_S = registerItem("phantom_weaver_s",
            new PHANTOM_WEAVER_S(SeraphItemSettings.of(EnumSeraphRarity.S)));
    public static final Item PHANTOM_WEAVER_SS = registerItem("phantom_weaver_ss",
            new PHANTOM_WEAVER_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item FATAL_NULL = registerItem("fatal_null",
            new FATAL_NULL(SeraphItemSettings.of(EnumSeraphRarity.A)));
    public static final Item FATAL_NULL_SS = registerItem("fatal_null_ss",
            new FATAL_NULL_SS(SeraphItemSettings.of(EnumSeraphRarity.SS)));

    public static final Item KAZABANA = registerItem("kazabana",
            new KAZABANA(SeraphItemSettings.of(EnumSeraphRarity.A)));

    // ------------------------------------------------------------------ 材料 --

    public static final Item RAW_CRYSTAL = registerItem("raw_crystal", new Item(new Item.Settings()));
    public static final Item CRYSTAL_LOTTERY = registerItem("crystal_lottery", new Item(new Item.Settings()));
    public static final Item LOTTERY = registerItem("lottery", new LotteryItem(new Item.Settings()));

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
    public static final Item XP_ITEM_1 = registerItem("xp_item_1", new XpBookItem(new Item.Settings(), 500));
    public static final Item XP_ITEM_2 = registerItem("xp_item_2", new XpBookItem(new Item.Settings(), 2000));

    public static final Item CANCER_SHELL = registerItem("cancer_shell", new Item(new Item.Settings()));

    // ------------------------------------------------------------ 刷怪蛋 --
    // 1.12.2 没有刷怪蛋（只有测试用的 ItemSpawnTestCancer / accurate_spawner），
    // 这里按 1.21.1 的规范补上：SpawnEggItem + 原生 template_spawn_egg 模型，
    // 颜色（主色/斑点色）由 SpawnEggItem 提供，客户端 ItemColors 会自动对
    // 所有 SpawnEggItem 注册 tint（见 MinecraftClient 里对 SpawnEggItem.getAll() 的遍历）。
    public static final Item C_SLASHER_SPAWN_EGG = registerItem("c_slasher_spawn_egg",
            new SpawnEggItem(ModEntities.C_SLASHER, 0x8C2F39, 0xE8C7C7, new Item.Settings()));
    public static final Item C_CREST_HOPPER_SPAWN_EGG = registerItem("c_crest_hopper_spawn_egg",
            new SpawnEggItem(ModEntities.C_CREST_HOPPER, 0x2F5D8C, 0xC7D8E8, new Item.Settings()));
    public static final Item C_SMALL_HOPPER_SPAWN_EGG = registerItem("c_small_hopper_spawn_egg",
            new SpawnEggItem(ModEntities.C_SMALL_HOPPER, 0x3F7A3A, 0xC9E8B0, new Item.Settings()));
    public static final Item C_DOLL_SPAWN_EGG = registerItem("c_doll_spawn_egg",
            new SpawnEggItem(ModEntities.C_DOLL, 0xB06A8C, 0xF0DCE6, new Item.Settings()));
    public static final Item C_MARIONETTE_SPAWN_EGG = registerItem("c_marionette_spawn_egg",
            new SpawnEggItem(ModEntities.C_MARIONETTE, 0x5D3F8C, 0xE0D2F0, new Item.Settings()));

    // 星癌体套装（1.12.2 的 ItemArmorCancer）：材质见 ModArmorMaterials。
    public static final Item CANCER_HELMET = registerArmor("cancer_helmet", ArmorItem.Type.HELMET);
    public static final Item CANCER_CHESTPLATE = registerArmor("cancer_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final Item CANCER_LEGGINGS = registerArmor("cancer_leggings", ArmorItem.Type.LEGGINGS);
    public static final Item CANCER_BOOTS = registerArmor("cancer_boots", ArmorItem.Type.BOOTS);

    public static final Item ONION = registerItem("onion", new Item(new Item.Settings().food(ModFoodComponents.ONION)));
    public static final Item KNIFE_CUT_NOODLES = registerItem("knife_cut_noodles", new KnifeCutNoodles(new Item.Settings().food(ModFoodComponents.KNIFE_CUT_NOODLES)));

    private static Item registerItem(String id, Item item)
    {
        return Registry.register(Registries.ITEM, Identifier.of(HBR_MC.MOD_ID, id), item);
    }

    /** 按部位注册盔甲，耐久沿用 1.12.2 的材质系数 9999。 */
    private static Item registerArmor(String id, ArmorItem.Type type) {
        return registerItem(id, new ArmorItem(ModArmorMaterials.cancer(), type,
                new Item.Settings().maxDamage(type.getMaxDamage(ModArmorMaterials.CANCER_DURABILITY))));
    }

    public static void registerModItems()
    {

        HBR_MC.LOGGER.info("Registering Items");
    }
}
