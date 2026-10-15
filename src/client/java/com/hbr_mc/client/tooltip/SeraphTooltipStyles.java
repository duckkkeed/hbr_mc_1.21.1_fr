package com.hbr_mc.client.tooltip;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import net.minecraft.item.Item;

import java.util.EnumMap;
import java.util.Map;

/**
 * Seraph 系物品的提示框配色：按稀有度（A / S / SS）区分。
 *
 * <p>本类是 1.12.2 Forge 版
 * {@code RenderTooltipEvent.Color} 监听器（{@code onToolTipColor}）在新架构下的移植。
 * 旧的写法和对应关系如下：
 *
 * <pre>
 * 1.12.2 Forge                                 1.21.1 Fabric（本工程）
 * ----------------------------------------------------------------
 * @SubscribeEvent                              无需事件注册
 * RenderTooltipEvent.Color                     TooltipStyleManager.setStyle(item, style)
 * event.getStack().getItem()                   遍历 ItemSeraphBase.registeredItems()
 * item instanceof ItemSeraphBase               ItemSeraphBase#getSeraphRarity()
 * event.setBackground(argb)                    TooltipStyle#background
 * event.setBorderStart(argb)                   TooltipStyle#borderTop
 * event.setBorderEnd(argb)                     TooltipStyle#borderBottom
 * default: throw IllegalStateException         default: 显式用 A 级配色（枚举新增时不再崩溃）
 * </pre>
 *
 * <p>原版的颜色值（含 {@code 0xf0} 透明度）已 1:1 保留在 {@link #RARITY_STYLES} 中，想改直接改那里。
 * 未在本表中出现的稀有度请在 {@link #styleFor} 里补一个分支。
 *
 * <p>调用时机：客户端初始化时调用一次 {@link #register()} 即可；之后每次渲染提示框都会从
 * {@link TooltipStyleManager} 读取，改配置立即生效。
 */
public final class SeraphTooltipStyles {

    /** 所有 Seraph 物品共用的背景色 */
    public static final int SERAPH_BACKGROUND = 0xF0333333;

    /** 稀有度 → 提示框配色 */
    private static final Map<EnumSeraphRarity, TooltipStyle> RARITY_STYLES = new EnumMap<>(EnumSeraphRarity.class);

    static {
        // SS：白 → 金
        RARITY_STYLES.put(EnumSeraphRarity.SS,
                TooltipStyle.ofGradientBorder(SERAPH_BACKGROUND, 0xF0FFFFFF, 0xF0ECB630));
        // S：浅蓝 → 深蓝
        RARITY_STYLES.put(EnumSeraphRarity.S,
                TooltipStyle.ofGradientBorder(SERAPH_BACKGROUND, 0xF095CEE6, 0xF0282266));
        // A：鲑红 → 暗红
        RARITY_STYLES.put(EnumSeraphRarity.A,
                TooltipStyle.ofGradientBorder(SERAPH_BACKGROUND, 0xF0FB8978, 0xF06D2717));
    }

    private SeraphTooltipStyles() {
    }

    /**
     * 取某个稀有度对应的配色。
     *
     * <p>对应原代码里的 {@code switch (seraphBase.seraphRarity)}。原代码的
     * {@code default} 分支会 {@code throw new IllegalStateException(...)}——这里改为<b>回落到 A 级配色</b>：
     * 多一个稀有度只会配色不符合预期，而不会让游戏在渲染提示框时直接崩溃；
     * 同时打一条警告日志方便定位。
     */
    public static TooltipStyle styleFor(EnumSeraphRarity rarity) {
        TooltipStyle style = RARITY_STYLES.get(rarity);
        if (style == null) {
            HBR_MC.LOGGER.warn("稀有度 {} 还没有配置提示框配色，暂时使用 A 级配色；请在 {} 中补充",
                    rarity, SeraphTooltipStyles.class.getSimpleName());
            return RARITY_STYLES.get(EnumSeraphRarity.A);
        }
        return style;
    }

    /** 取某个 Seraph 物品当前的配色（依据它自己的稀有度）。 */
    public static TooltipStyle styleFor(ItemSeraphBase item) {
        return styleFor(item.getSeraphRarity());
    }

    /**
     * 按稀有度取配色，不做"未知稀有度回落"处理；没有配置时返回 {@code null}。
     * 主要给调试/测试用，例如把某件普通物品临时套用 SS 级配色。
     */
    public static TooltipStyle rarityStyle(EnumSeraphRarity rarity) {
        return RARITY_STYLES.get(rarity);
    }

    /**
     * 把全部 Seraph 物品按各自稀有度登记到 {@link TooltipStyleManager}。
     *
     * <p>因为 {@link ItemSeraphBase} 在构造时会自动登记自己，所以新增 Seraph 物品后
     * <b>这里不需要任何改动</b>。
     */
    public static void register() {
        for (ItemSeraphBase item : ItemSeraphBase.registeredItems()) {
            TooltipStyleManager.setStyle(item, styleFor(item));
        }
        HBR_MC.LOGGER.info("已为 {} 件 Seraph 物品按稀有度设置提示框配色", ItemSeraphBase.registeredItems().size());
    }

    /**
     * 给单件物品手动指定配色（对应原代码里对 {@code event.setBackground(...)} 的单独覆盖，
     * 例如给某一件 SS 物品换一套边框）。
     */
    public static void override(Item item, TooltipStyle style) {
        TooltipStyleManager.setStyle(item, style);
    }
}
