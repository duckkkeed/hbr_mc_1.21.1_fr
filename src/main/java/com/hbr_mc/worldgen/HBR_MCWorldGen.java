package com.hbr_mc.worldgen;

import com.hbr_mc.HBR_MC;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * 水晶矿的世界生成注册。
 *
 * <p>设计参照 1.18 洞穴更新后的原版矿石（钻石/金/青金石等）：
 *
 * <ul>
 *   <li>📄 {@code configured_feature/crystal_ore.json} —— {@code minecraft:ore}，{@code size: 8}；
 *       同一个特性同时声明两个目标：石头系替换成 {@code crystal_ore}，深板岩系替换成
 *       {@code deepslate_crystal_ore}。岩石类型由原版方块标签决定，不再用自定义标签；</li>
 *   <li>📄 {@code placed_feature/crystal_ore.json} —— 浅层带：每区块 2 次，y ∈ [16, 128]（trapezoid）；</li>
 *   <li>📄 {@code placed_feature/crystal_ore_deep.json} —— 深层带：每区块 3 次，y ∈ [-64, 0]（trapezoid）；</li>
 *   <li>挂载：主世界所有生物群系，{@code UNDERGROUND_ORES} 阶段。</li>
 * </ul>
 *
 * <p>1.18 之后 y 轴扩展到 -64..320、深板岩取代 0 层以下的石头，因此“到地表高度”这种 1.12 写法
 * 已无意义；这里改用原版的高度分布（trapezoid 中间多、两端少）并分成上下两带，
 * 让浅层和深层都有稳定产量，同时保留“洞穴里能看到水晶”的观感
 * （{@code discard_chance_on_air_exposure = 0.0}，不做空气暴露剔除）。
 */
public final class HBR_MCWorldGen {

    private HBR_MCWorldGen() {}

    /** 浅层带（石头系）。对应 {@code data/hbr_mc/worldgen/placed_feature/crystal_ore.json}。 */
    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE = placedFeature("crystal_ore");

    /** 深层带（深板岩系）。对应 {@code data/hbr_mc/worldgen/placed_feature/crystal_ore_deep.json}。 */
    public static final RegistryKey<PlacedFeature> CRYSTAL_ORE_DEEP = placedFeature("crystal_ore_deep");

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
    }

    private static RegistryKey<PlacedFeature> placedFeature(String path) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, HBR_MC.id(path));
    }
}
