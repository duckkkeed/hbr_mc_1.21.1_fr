package com.hbr_mc.worldgen;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.worldgen.feature.RandomDropCrateFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * 本 mod 的世界生成注册。
 *
 * <h2>水晶矿</h2>
 * <p>设计参照 1.18 洞穴更新后的原版矿石（钻石/金/青金石等）：
 *
 * <ul>
 *   <li>📄 {@code configured_feature/crystal_ore.json} —— {@code minecraft:ore}，{@code size: 7}；
 *       同一个特性同时声明两个目标：石头系替换成 {@code crystal_ore}，深板岩系替换成
 *       {@code deepslate_crystal_ore}。岩石类型由原版方块标签决定，不再用自定义标签；</li>
 *   <li>📄 {@code placed_feature/crystal_ore.json} —— 浅层带：每区块 2 次，y ∈ [0, 96]（trapezoid）；</li>
 *   <li>📄 {@code placed_feature/crystal_ore_deep.json} —— 深层带：每区块 1 次，y ∈ [-64, 0]（trapezoid）；</li>
 *   <li>挂载：主世界所有生物群系，{@code UNDERGROUND_ORES} 阶段。</li>
 * </ul>
 *
 * <h2>随机补给箱</h2>
 * <p>对应 1.12.2 的 {@code worldgen/ModGenDropsOverworld}：
 * <ul>
 *   <li>📄 {@code configured_feature/random_drop_basic.json} —— 自定义特性 {@code hbr_mc:random_drop_basic}，
 *       从世界顶部往下找第一个「顶面实心 + 上方空气」的位置并放置一个补给箱；</li>
 *   <li>📄 {@code placed_feature/random_drop_basic.json} —— {@code rarity_filter chance: 10} + {@code in_square}，
 *       即平均每 10 个区块 1 个（原版 {@code POT_DENSITY = 0.1}）；</li>
 *   <li>挂载：主世界所有生物群系，{@code TOP_LAYER_MODIFICATION} 阶段（在树木/植被之后，贴地表）。</li>
 * </ul>
 */
public final class HBR_MCWorldGen {

    private HBR_MCWorldGen() {}

    /** 浅层带（石头系）。对应 {@code data/hbr_mc/worldgen/placed_feature/crystal_ore.json}。 */
    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE = placedFeature("crystal_ore");

    /** 深层带（深板岩系）。对应 {@code data/hbr_mc/worldgen/placed_feature/crystal_ore_deep.json}。 */
    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE_DEEP = placedFeature("crystal_ore_deep");

    /**
     * 随机补给箱特性，注册为 {@code hbr_mc:random_drop_basic}，供 configured_feature JSON 引用。
     */
    public static final RandomDropCrateFeature RANDOM_DROP_BASIC_FEATURE = Registry.register(
            Registries.FEATURE,
            HBR_MC.id("random_drop_basic"),
            new RandomDropCrateFeature(DefaultFeatureConfig.CODEC)
    );

    /** 对应 {@code data/hbr_mc/worldgen/placed_feature/random_drop_basic.json}。 */
    public static final RegistryKey<PlacedFeature> RANDOM_DROP_BASIC = placedFeature("random_drop_basic");

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        // 主世界所有生物群系（对应 1.12.2 的 `world.provider.getDimensionType() == OVERWORLD`）
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                CRYSTAL_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                CRYSTAL_ORE_DEEP
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.TOP_LAYER_MODIFICATION,
                RANDOM_DROP_BASIC
        );
    }

    private static RegistryKey<PlacedFeature> placedFeature(String path) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, HBR_MC.id(path));
    }
}
