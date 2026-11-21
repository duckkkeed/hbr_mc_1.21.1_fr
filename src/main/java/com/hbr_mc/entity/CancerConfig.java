package com.hbr_mc.entity;

/**
 * 星癌体的生成难度配置，对应 1.12.2 的 {@code ModConfig.SPAWN_CONF}。
 *
 * <p>原版这两个值是 Forge 配置项（{@code config/hbr_mc.cfg}），可在游戏内改；本工程按
 * {@code DpConfig} / {@code CombatConfig} 的惯例抽成常量，想调直接改这里。
 */
public final class CancerConfig {

    private CancerConfig() {
    }

    /**
     * 雪地生物群系额外加的六维，原版 {@code SNOWY_EXTRA_DIFFICULTY = 180}。
     *
     * <p>注意这是个"大"数：普通变体六维只有 3~50，雪地加成后直接 183~230，
     * 掉壳数量与伤害减免都会明显上一个档次（原版就是这么设计的）。
     */
    public static final float SNOWY_EXTRA_DIFFICULTY = 180.0F;

    /** 沙漠生物群系额外加的六维，原版 {@code DESERT_EXTRA_DIFFICULTY = 100}。 */
    public static final float DESERT_EXTRA_DIFFICULTY = 100.0F;

    // ---------------------------------------------------------------- 自然生成 --

    /**
     * 是否启用自然生成，对应原版 {@code ModConfig.SPAWN_CONF.SPAWN}（默认开启）。
     *
     * <p>关掉后 {@link CancerSpawns} 不会注册生成项，但刷怪蛋/ {@code /summon} 仍然可用。
     */
    public static final boolean SPAWN_ENABLED = true;

    /**
     * 各变体的生成权重，原版 {@code SPAWN_MARIONETTE / SPAWN_DOLL / SPAWN_SMALL_HOPPER /
     * SPAWN_CREST_HOPPER / SPAWN_SLASHER} <b>全部是 30</b>。
     *
     * <p>注意：五个 30 加起来是 150，而原版一整套怪物权重合计约 100，所以星癌体在地表会非常密
     * （1.12.2 就是这个数值；而且原版没有光照检查，白天也刷）。嫌多就调小；填 0 表示该变体不自然生成。
     */
    public static final int SPAWN_WEIGHT_SLASHER = 30;
    public static final int SPAWN_WEIGHT_CREST_HOPPER = 30;
    public static final int SPAWN_WEIGHT_SMALL_HOPPER = 30;
    public static final int SPAWN_WEIGHT_DOLL = 30;
    public static final int SPAWN_WEIGHT_MARIONETTE = 30;

    /** 每次生成的数量范围，原版 {@code add(biome, weight, class, 1, 3)}。 */
    public static final int SPAWN_GROUP_MIN = 1;
    public static final int SPAWN_GROUP_MAX = 3;
}
