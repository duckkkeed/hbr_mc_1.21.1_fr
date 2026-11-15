package com.hbr_mc.item.seraph;

import com.hbr_mc.attribute.ModAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 炽天使的「属性百分比」表与「强化成长」表，对应 1.12.2 的 {@code designs/SeraphAttrData}。
 *
 * <h2>两张表</h2>
 * <ol>
 *   <li>{@link #percentageBase}：<b>基础百分比加成</b>，按稀有度 + 职业给每个属性一个系数，
 *       作为 {@code getAttrValuePercent} 的起点（见 {@code ItemSeraphBase#getAttrValuePercent}）；</li>
 *   <li>{@link #boostSS}：<b>SS 强化的逐次成长</b>，把若干次强化按 8 / 10 / 20 次一组排成一条长列表，
 *       第 N 次强化就取这条列表的前 N 项累加。</li>
 * </ol>
 *
 * <h2>与 1.12.2 的差异（刻意保留原行为）</h2>
 * <ul>
 *   <li>原版 {@code getData} 里 <b>S 级不分职业</b>，一律返回 {@code fighterA}；
 *       这里用 {@link #percentageBase} 的 {@code S} 分支保留同一行为，并在注释里标注。</li>
 *   <li>原版 {@code boostSS} 每个职业最后一项是"全局"条目（注释写 {@code //global}），
 *       但它们实际上仍然加在某个具体属性上，因此这里按原表原样记录，不另作解释。</li>
 *   <li>原版用 {@code null} 属性表示"对所有属性都算"的条目；本移植保留 {@code null}，
 *       由 {@code ItemSeraphBase} 在遍历时判断。</li>
 * </ul>
 */
public final class SeraphAttrData {

    /** 稀有度 + 职业 → 每个属性的基础百分比。 */
    public static final Map<EnumSeraphRarity, Map<EnumSeraphClass, Map<RegistryEntry<EntityAttribute>, Float>>> percentageBase
            = new EnumMap<>(EnumSeraphRarity.class);

    /** 职业 → SS 强化成长长表（第 N 次强化取前 N 项）。 */
    public static final Map<EnumSeraphClass, List<SeraphBoostUnit>> boostSS
            = new EnumMap<>(EnumSeraphClass.class);

    static {
        initPercentageTables();
        boostSS.put(EnumSeraphClass.FIGHTER, buildFighterBoost());
        boostSS.put(EnumSeraphClass.RANGER, buildRangerBoost());
        boostSS.put(EnumSeraphClass.WIZARD, buildWizardBoost());
    }

    private SeraphAttrData() {
    }

    /**
     * 取基础百分比表，对应 1.12.2 的 {@code SeraphAttrData#getData(rarity, class)}。
     *
     * <p>原版对 S 级忽略职业，这里保留：查不到时回落到 A 级的 FIGHTER 表。
     */
    public static Map<RegistryEntry<EntityAttribute>, Float> getData(EnumSeraphRarity rarity, EnumSeraphClass seraphClass) {
        Map<EnumSeraphClass, Map<RegistryEntry<EntityAttribute>, Float>> byClass = percentageBase.get(rarity);
        if (byClass == null) {
            byClass = percentageBase.get(EnumSeraphRarity.A);
        }
        Map<RegistryEntry<EntityAttribute>, Float> data = byClass.get(seraphClass);
        if (data == null) {
            data = byClass.get(EnumSeraphClass.FIGHTER);
        }
        return data;
    }

    /** 取某次强化（第 {@code index} 次，0 起）的加成条目；越界返回 {@code null}。 */
    public static SeraphBoostUnit boostUnitAt(EnumSeraphClass seraphClass, int index) {
        List<SeraphBoostUnit> list = boostSS.get(seraphClass);
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    // ------------------------------------------------------------------ 初始化 --

    private static void initPercentageTables() {
        // 原版 initA(aBuff = 0.2)：A 级的基础百分比。
        // 注意原版写的是 0.30f - 0.2f 这种"先给总值再减 buff"的写法，这里直接写结果并保留来源注释。
        put(EnumSeraphRarity.A, EnumSeraphClass.FIGHTER, map(
                ModAttributes.STR, 0.10F,   // 0.30 - 0.20
                ModAttributes.DEX, 0.00F,   // 0.20 - 0.20
                ModAttributes.END, 0.10F,   // 0.30 - 0.20
                ModAttributes.MEN, 0.05F,   // 0.25 - 0.20
                ModAttributes.INT, 0.00F,   // 0.20 - 0.20
                ModAttributes.LUC, 0.05F)); // 0.25 - 0.20

        put(EnumSeraphRarity.A, EnumSeraphClass.RANGER, map(
                ModAttributes.STR, 0.00F,   // 0.20 - 0.20
                ModAttributes.DEX, 0.10F,   // 0.30 - 0.20
                ModAttributes.END, 0.05F,   // 0.25 - 0.20
                ModAttributes.MEN, 0.00F,   // 0.20 - 0.20
                ModAttributes.INT, 0.05F,   // 0.25 - 0.20
                ModAttributes.LUC, 0.10F)); // 0.30 - 0.20

        put(EnumSeraphRarity.A, EnumSeraphClass.WIZARD, map(
                ModAttributes.STR, 0.05F,   // 0.25 - 0.20
                ModAttributes.DEX, 0.05F,   // 0.25 - 0.20
                ModAttributes.END, 0.00F,   // 0.20 - 0.20
                ModAttributes.MEN, 0.10F,   // 0.30 - 0.20
                ModAttributes.INT, 0.10F,   // 0.30 - 0.20
                ModAttributes.LUC, 0.00F)); // 0.20 - 0.20

        // 原版 S 级不分职业，一律用 fighterA；这里三份都指向同一张表，保持可读性。
        Map<RegistryEntry<EntityAttribute>, Float> sTable = percentageBase.get(EnumSeraphRarity.A).get(EnumSeraphClass.FIGHTER);
        putTable(EnumSeraphRarity.S, sTable);

        put(EnumSeraphRarity.SS, EnumSeraphClass.WIZARD, map(
                ModAttributes.STR, 0.35F,
                ModAttributes.DEX, 0.35F,
                ModAttributes.END, 0.30F,   // 原注释：A + 0.1
                ModAttributes.MEN, 0.40F,
                ModAttributes.INT, 0.40F,
                ModAttributes.LUC, 0.30F)); // 原注释：A + 0.1

        put(EnumSeraphRarity.SS, EnumSeraphClass.FIGHTER, map(
                ModAttributes.STR, 0.40F,
                ModAttributes.DEX, 0.30F,
                ModAttributes.END, 0.40F,
                ModAttributes.MEN, 0.35F,
                ModAttributes.INT, 0.30F,
                ModAttributes.LUC, 0.35F));

        put(EnumSeraphRarity.SS, EnumSeraphClass.RANGER, map(
                ModAttributes.STR, 0.30F,
                ModAttributes.DEX, 0.40F,
                ModAttributes.END, 0.35F,
                ModAttributes.MEN, 0.30F,
                ModAttributes.INT, 0.35F,
                ModAttributes.LUC, 0.40F));
    }

    private static void put(EnumSeraphRarity rarity, EnumSeraphClass seraphClass,
                            Map<RegistryEntry<EntityAttribute>, Float> data) {
        percentageBase.computeIfAbsent(rarity, r -> new EnumMap<>(EnumSeraphClass.class)).put(seraphClass, data);
    }

    private static void putTable(EnumSeraphRarity rarity, Map<RegistryEntry<EntityAttribute>, Float> data) {
        for (EnumSeraphClass seraphClass : EnumSeraphClass.values()) {
            put(rarity, seraphClass, data);
        }
    }

    @SafeVarargs
    private static Map<RegistryEntry<EntityAttribute>, Float> map(Object... pairs) {
        Map<RegistryEntry<EntityAttribute>, Float> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            @SuppressWarnings("unchecked")
            RegistryEntry<EntityAttribute> attr = (RegistryEntry<EntityAttribute>) pairs[i];
            map.put(attr, (Float) pairs[i + 1]);
        }
        return map;
    }

    // -------------------------------------------------------------- 强化成长表 --

    private static void fixed(List<SeraphBoostUnit> list, RegistryEntry<EntityAttribute> attr, float value) {
        list.add(new SeraphBoostUnit(attr, value));
    }

    private static void percent(List<SeraphBoostUnit> list, RegistryEntry<EntityAttribute> attr, float value) {
        list.add(new SeraphBoostUnit(attr, value, EnumBoostType.PERCENT));
    }

    private static void global(List<SeraphBoostUnit> list, float value) {
        list.add(new SeraphBoostUnit(null, value));
    }

    /** 把一组的条目追加进长表，对应原版 {@code unitList} → {@code flatList} 的写法。 */
    private static void appendAll(List<SeraphBoostUnit> flat, List<SeraphBoostUnit> group) {
        flat.addAll(group);
    }

    /** SS · FIGHTER（原版 {@code SSBoostSTR}）：4 组，共 8 + 10 + 20 + 19 = 57 次强化。 */
    private static List<SeraphBoostUnit> buildFighterBoost() {
        List<SeraphBoostUnit> flat = new ArrayList<>();
        List<SeraphBoostUnit> g = new ArrayList<>();
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.END, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.STR, 0.1F);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.DEX, 2);
        percent(g, ModAttributes.END, 0.1F);
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.MEN, 2);
        global(g, 1);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.INT, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.STR, 1); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.MEN, 2);
        global(g, 1);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.INT, 2);
        percent(g, ModAttributes.END, 0.1F);
        fixed(g, ModAttributes.DEX, 2);
        global(g, 1);
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.MEN, 2);
        global(g, 1);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.INT, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.END, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        fixed(g, ModAttributes.MEN, 2);
        global(g, 1);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.STR, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.END, 2);
        fixed(g, ModAttributes.INT, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.DEX, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.END, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.STR, 0.1F);
        fixed(g, ModAttributes.LUC, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.STR, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);
        return flat;
    }

    /** SS · RANGER（原版 {@code SSBoostDEX}）：4 组，共 8 + 10 + 20 + 19 = 57 次强化。 */
    private static List<SeraphBoostUnit> buildRangerBoost() {
        List<SeraphBoostUnit> flat = new ArrayList<>();
        List<SeraphBoostUnit> g = new ArrayList<>();
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        fixed(g, ModAttributes.STR, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.END, 0.1F);
        fixed(g, ModAttributes.END, 2);
        global(g, 1);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.DEX, 1); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.END, 2);
        global(g, 1);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.END, 2);
        global(g, 1);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.STR, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        fixed(g, ModAttributes.LUC, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.END, 2);
        global(g, 1);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.DEX, 2);
        percent(g, ModAttributes.END, 0.1F);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.MEN, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.STR, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.DEX, 2);
        global(g, 1);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.INT, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.DEX, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);
        return flat;
    }

    /** SS · WIZARD（原版 {@code SSBoostWIS}）：4 组，共 8 + 10 + 20 + 19 = 57 次强化。 */
    private static List<SeraphBoostUnit> buildWizardBoost() {
        List<SeraphBoostUnit> flat = new ArrayList<>();
        List<SeraphBoostUnit> g = new ArrayList<>();
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        fixed(g, ModAttributes.STR, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.STR, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.END, 2);
        fixed(g, ModAttributes.INT, 1); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.MEN, 0.1F);
        fixed(g, ModAttributes.END, 2);
        global(g, 1);
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.LUC, 2);
        percent(g, ModAttributes.END, 0.1F);
        fixed(g, ModAttributes.END, 2);
        fixed(g, ModAttributes.MEN, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);

        g = new ArrayList<>();
        fixed(g, ModAttributes.INT, 2);
        fixed(g, ModAttributes.MEN, 2);
        percent(g, ModAttributes.DEX, 0.1F);
        fixed(g, ModAttributes.STR, 2);
        global(g, 1);
        fixed(g, ModAttributes.DEX, 2);
        fixed(g, ModAttributes.INT, 2);
        percent(g, ModAttributes.STR, 0.1F);
        fixed(g, ModAttributes.MEN, 2);
        fixed(g, ModAttributes.LUC, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.END, 2);
        percent(g, ModAttributes.LUC, 0.1F);
        fixed(g, ModAttributes.INT, 2);
        global(g, 1);
        fixed(g, ModAttributes.MEN, 2);
        fixed(g, ModAttributes.STR, 2);
        percent(g, ModAttributes.INT, 0.1F);
        fixed(g, ModAttributes.DEX, 2); // 原注释：hit combo +1%
        fixed(g, ModAttributes.INT, 2); // 原注释：hit combo +1%（global）
        appendAll(flat, g);
        return flat;
    }
}
