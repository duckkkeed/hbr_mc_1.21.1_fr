package com.hbr_mc.combat;

/**
 * 战斗数值表，对应 1.12.2 的 {@code init/ModConfig} 里的 {@code CombatConf} / {@code SkillConf}。
 *
 * <h2>与 1.12.2 的对应</h2>
 * 原版这些值是 Forge 配置文件（{@code config/hbr_mc.cfg}），可由玩家修改。本工程目前<b>不做配置文件</b>，
 * 直接把原版默认值抽成常量：数量少、改动少，而且每个值都有注释说明来源。
 * 若以后要做配置界面，把这些常量换成配置读取即可，调用点不用动。
 *
 * <table>
 *   <caption>字段对照</caption>
 *   <tr><th>1.12.2</th><th>本工程</th><th>含义</th></tr>
 *   <tr><td>{@code NORMAL_ATK_POWER} / {@code NORMAL_ATK_CAP}</td><td>{@link #NORMAL_ATK_POWER} / {@link #NORMAL_ATK_CAP}</td><td>普通攻击（右键蓄力）的威力与上限</td></tr>
 *   <tr><td>{@code BONUS_DAMAGE_RATE}</td><td>{@link #BONUS_DAMAGE_RATE}</td><td>打护盾（吸收伤害）时的额外倍率</td></tr>
 *   <tr><td>{@code AOE_DISTANCE} / {@code AOE_RADIUS}</td><td>{@link #AOE_DISTANCE} / {@link #AOE_RADIUS}</td><td>范围技能的中心偏移与半径</td></tr>
 *   <tr><td>{@code STUN_TICK_PER_TURN}</td><td>{@link #STUN_TICK_PER_TURN}</td><td>眩晕持续时间（tick）</td></tr>
 *   <tr><td>{@code BUFF_TIME}（单位：秒）</td><td>{@link #BUFF_TIME_SECONDS}</td><td>攻击力上升效果的持续时间</td></tr>
 * </table>
 */
public final class CombatConfig {

    private CombatConfig() {
    }

    // ---------------------------------------------------------------- 通用 --

    public static final float NORMAL_ATK_POWER = 10.0F;
    public static final float NORMAL_ATK_CAP = 150.0F;

    /** 原版 {@code SKILL_UP_CHANCE}，技能等级提升概率（目前只保留数值，技能升级入口尚未接入）。 */
    public static final float SKILL_UP_CHANCE = 0.01F;

    public static final float BONUS_DAMAGE_RATE = 1.3F;
    public static final float AOE_DISTANCE = 32.0F;
    public static final float AOE_RADIUS = 32.0F;
    public static final int STUN_TICK_PER_TURN = 50;

    /**
     * BUFF 持续时间。
     *
     * <p>原版字段叫 {@code BUFF_TIME = 20 * 60}，注释与用法都是"秒"（{@code ApplyBuff} 内部再乘 20 换成 tick），
     * 所以 1200 秒 = 20 分钟。这里保留同一语义，变量名带上了单位避免误用。
     */
    public static final float BUFF_TIME_SECONDS = 20.0F * 60.0F;

    // ------------------------------------------------------------ 具体技能 --
    // 数值全部照抄 1.12.2 ModConfig.CombatConf 的默认值。

    /** BraveBlue（A）「Attack or Music」。 */
    public static final SkillConf BRAVE_BLUE_A = new SkillConf(58.2F, 123.0F, 6, 1.5F);
    /** BraveBlue SS 的群体修复。 */
    public static final SkillConf BRAVE_BLUE_HEAL = new SkillConf(305.0F, 200.0F, 5);
    /** BraveBlue SS 的终极技（原文：12SP, 138 cap, 1602-8010 potency）。 */
    public static final SkillConf BRAVE_BLUE_ULTI = new SkillConf(160.2F, 138.0F, 12, 2.75F);

    /** RapidFire（A）的散射。 */
    public static final SkillConf RAPID_FIRE_4SP_AOE = new SkillConf(29.0F, 117.0F, 4, 0.8F);
    /** RapidFire SS 的连射（ブレイクカノン，3 hit）。 */
    public static final SkillConf RAPID_FIRE_7SP_ST = new SkillConf(73.0F, 126.0F, 7, 1.75F);
    /** RapidFire SS 的终极技（流星，1 hit，2 次使用）。 */
    public static final SkillConf RAPID_FIRE_11SP_AOE = new SkillConf(167.0F, 144.0F, 11, 2.6F).withUses(2, 2);

    /** SupremeEdge / FatalNull 的群体攻击力上升。 */
    public static final SkillConf SUPREME_EDGE_6SP_BUFF = new SkillConf(50 / 65F, 208.0F, 6);

    public static final SkillConf CLAVIS_A = new SkillConf(45.0F, 120.0F, 5, 1.25F);
    public static final SkillConf CLAVIS_SS_10SP_STUN = new SkillConf(105.0F, 135.0F, 10, 2.0F);
    public static final SkillConf CLAVIS_SS_14SP_ULTI = new SkillConf(250.0F, 147.0F, 14, 3.5F).withUses(2, 2);

    public static final SkillConf GLITTER_SHADOW_A = new SkillConf(450.0F, 120.0F, 5, 5.0F);
    public static final SkillConf GLITTER_SHADOW_SS = new SkillConf(591.0F, 126.0F, 7, 5.6F);
    public static final SkillConf GLITTER_SHADOW_SS_ULTI = new SkillConf(2178.0F, 144.0F, 11, 13.0F).withUses(2, 2);

    public static final SkillConf PHANTOM_WEAVER_HEAL_A = new SkillConf(305.0F, 200.0F, 5).withUses(5, 5);
    public static final SkillConf PHANTOM_WEAVER_7SP_AOE = new SkillConf(59.1F, 126.0F, 7, 1.4F);
    public static final SkillConf PHANTOM_WEAVER_13SP_REVIVAL = new SkillConf(1169.0F, 248.0F, 13).withUses(2, 2);

    public static final SkillConf KAZABANA_5SP_AOE = new SkillConf(37.5F, 120.0F, 5, 1.0F);

    /**
     * 一个技能的数值，对应 1.12.2 的 {@code ModConfig.SkillConf}。
     *
     * @param minPotency     技能威力（命中时作为"最低伤害"参与属性换算）
     * @param cap            属性换算的封顶值
     * @param sp             冷却秒数（原版 {@code SP}）
     * @param destructionRate 原版 {@code DR}（破坏率），技能数值里保留，暂未参与计算
     * @param uses           每场战斗可用次数；{@code -1} 表示不限
     * @param usesGrowth     每提升 1 级技能等级增加的使用次数
     */
    public record SkillConf(float minPotency, float cap, int sp, float destructionRate, int uses, int usesGrowth) {

        public SkillConf(float minPotency, float cap, int sp, float destructionRate) {
            this(minPotency, cap, sp, destructionRate, -1, 0);
        }

        public SkillConf(float minPotency, float cap, int sp) {
            this(minPotency, cap, sp, 1.0F, -1, 0);
        }

        /** 对应原版可链式调用的 {@code setUses(uses, usesGrowth)}。 */
        public SkillConf withUses(int uses, int usesGrowth) {
            return new SkillConf(minPotency, cap, sp, destructionRate, uses, usesGrowth);
        }

        /** 冷却 tick 数（原版 {@code CommonDef.TICK_PER_SECOND * COMBAT.SP}）。 */
        public int cooldownTicks() {
            return 20 * sp;
        }
    }
}
