package com.hbr_mc.client.tooltip;

import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 提示框配色的全局入口点。
 * <p>Global entry point for tooltip color styling.
 *
 * <p>在客户端初始化时（例如 {@code HBR_MCClient#onInitializeClient}）设置一次即可，也可以随时修改（例如做主题切换 / 配置界面实时预览）：
 * <p>You only need to set it once during client initialization (for example, in {@code HBR_MCClient#onInitializeClient}),
 * and it can also be changed at any time (for example, for theme switching or live preview in a config UI):
 *
 * <pre>{@code
 * // 所有物品：60% 不透明的深蓝黑背景 + 不透明的青色实线边框
 * // All items: 60% opaque dark blue-black background + opaque cyan solid border
 * TooltipStyleManager.setStyle(TooltipStyle.ofSolidBorder(0x99000B1E, 0xFF00E5FF));
 *
 * // 只给钻石单独配色（其余物品仍然使用上面的全局配色）
 * // Only give diamond a custom color (all other items still use the global style above)
 * TooltipStyleManager.setStyle(Items.DIAMOND, TooltipStyle.ofSolidBorder(0xCC001B33, 0xFF55FFFF));
 *
 * // 取消钻石的单独配色
 * // Remove diamond's custom color
 * TooltipStyleManager.clearStyle(Items.DIAMOND);
 * }</pre>
 *
 * <p>物品级配色优先于全局配色：命中就<b>完全</b>使用该物品的配色（不再与全局配色混合）。把全局配色设为 {@code null} 即可恢复原版外观，也可以直接用 {@link TooltipStyle#VANILLA}。
 * <p>Item-level colors take precedence over the global style: when matched, the item uses its own color completely instead of blending with the global style.
 * Set the global style to {@code null} to restore the vanilla appearance, or use {@link TooltipStyle#VANILLA} directly.
 */
public final class TooltipStyleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("hbr_mc/tooltip");

    /** 全局配色；{@code null} 表示使用原版外观。
    * <p>Global color style; {@code null} means use the vanilla appearance. */
    private static volatile TooltipStyle style = TooltipStyle.VANILLA;

    /**
     * 物品级配色。使用 {@link IdentityHashMap} 是因为 {@link Item} 是注册期单例对象，且原版 {@code Item} 未覆写 {@code equals/hashCode}，按对象身份比较最稳妥。
     * <p>Item-level color styles. {@link IdentityHashMap} is used because {@link Item} is a singleton object during registration,
     * and the vanilla {@code Item} does not override {@code equals/hashCode}; comparing by object identity is the most reliable approach.
     */
    private static final Map<Item, TooltipStyle> ITEM_STYLES = new IdentityHashMap<>();

    /**
     * 当前正在渲染的物品。由 {@code TooltipBackgroundMixin} 在绘制入口写入、出口清理，因此不会跨帧或跨物品残留。
     * <p>The item currently being rendered. It is written at the rendering entry by {@code TooltipBackgroundMixin} and cleared at exit,
     * so it does not leak across frames or items.
     */
    private static final ThreadLocal<Item> CURRENT_ITEM = new ThreadLocal<>();

    private TooltipStyleManager() {
    }

    // ------------------------------------------------------------ 全局配色 --
    // ------------------------------------------------------------ Global color styles --

    /** 全局配色（未命中物品级配色时使用），永不为 {@code null}。
    * <p>Global color style (used when no item-level style matches); it is never {@code null}. */
    public static @NotNull TooltipStyle getStyle() {
        TooltipStyle current = style;
        return current == null ? TooltipStyle.VANILLA : current;
    }

    /**
     * 设置全局配色。
     * <p>Set the global color style.
     * @param newStyle 新的配色；传 {@code null} 表示全局恢复原版外观
     * <p>newStyle The new style; passing {@code null} restores the vanilla look globally.
     */
    public static void setStyle(@Nullable TooltipStyle newStyle) {
        style = newStyle;
    }

    /* 全局恢复原版提示框外观（物品级配色不受影响）。 */
    /* Restore the vanilla tooltip appearance globally (item-level styles are unaffected). */
    public static void reset() {
        style = TooltipStyle.VANILLA;
    }

    // ---------------------------------------------------------- 物品级配色 --
    // ---------------------------------------------------------- Item-level color styles --

    /**
     * 给某个物品单独设置提示框配色，命中时优先于全局配色。
     * <p>Set a custom tooltip color for a specific item; when matched, it takes precedence over the global style.
     *
     * @param item     目标物品（例如 {@code Items.DIAMOND}）
     * <p>item     The target item (for example, {@code Items.DIAMOND})
     * @param newStyle 该物品的配色；传 {@code null} 等价于 {@link #clearStyle(Item)}（回落到全局配色）
     * <p>newStyle The item's color; passing {@code null} is equivalent to {@link #clearStyle(Item)} (falling back to the global style)
     */
    public static void setStyle(@NotNull Item item, @Nullable TooltipStyle newStyle) {
        if (newStyle == null) {
            ITEM_STYLES.remove(item);
        } else {
            ITEM_STYLES.put(item, newStyle);
        }
    }

    /* 取消某个物品的单独配色，回落到全局配色。 */
    /* Remove a custom color for a specific item and fall back to the global style. */
    public static void clearStyle(@NotNull Item item) {
        ITEM_STYLES.remove(item);
    }

    /* 取消所有物品级配色。 */
    /* Remove all item-level color styles. */
    public static void clearItemStyles() {
        ITEM_STYLES.clear();
    }

    /* 该物品是否设置了单独配色。 */
    /* Whether this item has a custom color style. */
    public static boolean hasItemStyle(@NotNull Item item) {
        return ITEM_STYLES.containsKey(item);
    }

    /** 该物品的单独配色，未设置时为 {@code null}。
     * The custom color for this item; {@code null} if not set. */
    public static @Nullable TooltipStyle getItemStyle(@NotNull Item item) {
        return ITEM_STYLES.get(item);
    }

    // ------------------------------------------------------------ 解析 / 上下文 --
    // ------------------------------------------------------------ Resolve / context --

    /**
     * 解析本次渲染应当使用的配色：优先取当前物品的专属配色，否则回落到全局配色。
     * <p>Resolve the color to use for current rendering: prefer the current item's dedicated style, otherwise fall back to the global style.
     *
     * <p>由 {@code TooltipBackgroundMixin} 在替换颜色时调用；不在物品提示框内渲染时，当前物品为空，等价于 {@link #getStyle()}。
     * <p>Called by {@code TooltipBackgroundMixin} when replacing colors; when not rendering within an item tooltip,
     * the current item is empty and this is equivalent to {@link #getStyle()}.
     */
    public static @NotNull TooltipStyle resolveStyle() {
        Item item = CURRENT_ITEM.get();
        if (item != null) {
            TooltipStyle itemStyle = ITEM_STYLES.get(item);
            if (itemStyle != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("物品 {} 使用专属提示框配色：{}", item, itemStyle);
                }
                return itemStyle;
            }
        }
        return getStyle();
    }

    /** 记录当前正在渲染的物品（由 {@code TooltipBackgroundMixin} 调用）。
    * <p>Record the item currently being rendered (called by {@code TooltipBackgroundMixin}). */
    public static void setCurrentItem(@Nullable Item item) {
        if (item == null) {
            CURRENT_ITEM.remove();
        } else {
            CURRENT_ITEM.set(item);
        }
    }

    /* 清理当前物品上下文（渲染结束必须调用）。 */
    /* Clear the current item context (must be called when rendering ends). */
    public static void clearCurrentItem() {
        CURRENT_ITEM.remove();
    }
}
