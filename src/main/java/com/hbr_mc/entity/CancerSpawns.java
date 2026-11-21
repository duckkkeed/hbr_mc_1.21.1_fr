package com.hbr_mc.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;

/**
 * 星癌体的自然生成，对应 1.12.2 的 {@code init/RegisterSpawn}。
 *
 * <h2>1.12.2 的做法</h2>
 * 遍历所有生物群系，往 {@code EnumCreatureType.MONSTER} 的生成表里给五个变体各加一条：
 * 权重 30、每次 1~3 只（{@code RegisterSpawn#add(biome, weight, class, 1, 3)}）。
 * 落点由 {@code EntityCancer#getCanSpawnHere} 把关：<b>只要求头顶见天</b>——原版星癌体继承的是
 * {@code EntityCreature} 而不是 {@code EntityMob}，所以"怪物只在暗处生成"的光照检查从未生效，
 * 白天同样会刷（详见 {@link EntityCancer#canSpawn} 的注释）。
 *
 * <h2>1.21.1 的对应</h2>
 * <ul>
 *   <li>{@link SpawnRestriction#register}：落点规则（贴地 + 高度图 + {@link EntityCancer#canSpawn}）；</li>
 *   <li>{@link BiomeModifications#addSpawn}：把生成项挂到主世界所有生物群系
 *       （原版遍历的是全部维度，这里按需求收紧到主世界——下界顶上有基岩、末地见天，
 *       真按原版加会在末地刷出一堆）。</li>
 * </ul>
 *
 * <p>权重等数值在 {@link CancerConfig}；{@code SPAWN_ENABLED = false} 可整体关闭，
 * 刷怪蛋与 {@code /summon} 不受影响。
 */
public final class CancerSpawns {

    private CancerSpawns() {
    }

    /** 在 {@code ModEntities#register} 之后调用一次。 */
    public static void register() {
        if (!CancerConfig.SPAWN_ENABLED) {
            return;
        }
        // 与原版相同的注册顺序（对生成权重没有影响，只是便于对照）
        register(ModEntities.C_MARIONETTE, CancerConfig.SPAWN_WEIGHT_MARIONETTE);
        register(ModEntities.C_DOLL, CancerConfig.SPAWN_WEIGHT_DOLL);
        register(ModEntities.C_SMALL_HOPPER, CancerConfig.SPAWN_WEIGHT_SMALL_HOPPER);
        register(ModEntities.C_CREST_HOPPER, CancerConfig.SPAWN_WEIGHT_CREST_HOPPER);
        register(ModEntities.C_SLASHER, CancerConfig.SPAWN_WEIGHT_SLASHER);
    }

    private static <T extends EntityCancer> void register(EntityType<T> type, int weight) {
        SpawnRestriction.register(type, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityCancer::canSpawn);
        if (weight > 0) {
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, type, weight,
                    CancerConfig.SPAWN_GROUP_MIN, CancerConfig.SPAWN_GROUP_MAX);
        }
    }
}
