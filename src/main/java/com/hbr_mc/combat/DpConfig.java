package com.hbr_mc.combat;

/**
 * DP 系统的开关与数值，对应 1.12.2 {@code ModConfig.COMBAT} / {@code ModConfig.GUI_CONF} 里与 DP 有关的部分。
 *
 * <p>1.12.2 这些值来自 Forge 配置文件；本工程目前不做配置界面，先抽成常量并保留原版默认值，
 * 想改行为直接改这里（注释里写了影响面）。
 */
public final class DpConfig {

    private DpConfig() {
    }

    // ------------------------------------------------------------ 规则开关 --

    /**
     * 星癌体的「壳」是否严格免疫。
     *
     * <p>原版语义：{@code absorptionAmount > 0} 时，只有"能破壳"的伤害来源
     * （炽天使持有者、本 mod 的弹丸、虚空）才打得进去；其它来源完全免疫。
     * 设成 {@code false} 则退化为"原版吸收值"（任何伤害都能磨壳）。
     */
    public static final boolean STRICT_SHELL = true;

    /** 玩家侧 DP（主手炽天使的耐久吸收伤害）是否启用。 */
    public static final boolean PLAYER_DP_ENABLED = true;

    /**
     * 玩家侧 DP 是否过滤"环境伤害"。
     *
     * <p>原版 {@code ItemSeraphBase#onHit} <b>不</b>过滤来源，于是摔落、虚空、窒息都会被武器耐久吃掉
     * （拿着炽天使掉不进虚空）。这里保留原行为（{@code false}）；设成 {@code true} 时
     * 只吸收战斗伤害，环境伤害正常结算。
     */
    public static final boolean PLAYER_DP_FILTER_ENVIRONMENTAL = false;

    /**
     * 玩家普通近战是否改走六维公式（对应原版 {@code DesignPlayerDamageRework}）。
     *
     * <p>原版在"目标是玩家，或目标 {@code DP_MAX > 0}"时，取消原版近战伤害，
     * 改按武器类型分段调用 {@code CombatUtil.attackAsHBR}。设成 {@code false} 则保留原版近战。
     */
    public static final boolean PLAYER_MELEE_REWORK = true;

    /** 是否渲染头顶 DP 条（对应原版 {@code GUI_CONF.RENDER_DP}）。 */
    public static final boolean RENDER_BARS = true;

    // ------------------------------------------------------------ HUD 数值 --
    // 数值单位与 1.12.2 的 RenderDP 一致：先按 0.026666672 缩放，再用"像素"画四边形。

    /** 超过这个距离就不画 DP 条（原版 {@code MAX_RENDER_HUD_DISTANCE = 64}）。 */
    public static final float MAX_RENDER_DISTANCE = 64.0F;

    /** 条相对实体头顶的高度偏移（原版 {@code GUI_Y = 0.5}，以格为单位，加在缩放之前）。 */
    public static final float HEIGHT_OFFSET = 0.5F;

    /** 条的半宽（原版 {@code RENDER_HUD_SIZE = 20}）。 */
    public static final float HUD_SIZE = 20.0F;

    /** 世界坐标 → HUD 像素的缩放（原版 {@code scale = 0.026666672F}）。 */
    public static final float HUD_SCALE = 0.026666672F;

    /** 条高（像素）。 */
    public static final int BAR_HEIGHT = 2;

    /** 背景外扩（像素）。 */
    public static final int BAR_PADDING = 1;

    /** 背景色（ARGB 里的 RGB + 独立 alpha）。 */
    public static final int BAR_BACKGROUND_ALPHA = 64;

    /** 前景色：原版写死青色 {@code (0, 255, 255)}，alpha 127。 */
    public static final int BAR_COLOR_R = 0;
    public static final int BAR_COLOR_G = 255;
    public static final int BAR_COLOR_B = 255;
    public static final int BAR_ALPHA = 127;

    // ------------------------------------------------------------ 破壳反馈 --

    /** 破壳时施加的「反作用力」时长（tick），原版 {@code new PotionEffect(SELF_RECOIL, 20, 0)}。 */
    public static final int SHELL_BREAK_RECOIL_TICKS = 20;
}
