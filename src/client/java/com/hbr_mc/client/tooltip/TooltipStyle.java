package com.hbr_mc.client.tooltip;

/**
 * 物品提示框（Tooltip）的配色方案。
 * <p>Tooltip color scheme for item tooltips.
 *
 * <p>所有颜色均为 ARGB 格式，可写成十六进制字面量，例如：
 * <p>All colors use the ARGB format and can be written as hexadecimal literals, for example:
 * <ul>
 *     <li>{@code 0xF0100010} —— 半透明深紫黑（原版背景色）</li>
 *     <li>{@code 0xF0100010} — Semi-transparent dark purple-black (vanilla background color)</li>
 *     <li>{@code 0x80000000} —— 50% 透明度的纯黑背景</li>
 *     <li>{@code 0x80000000} — Pure black background at 50% opacity</li>
 *     <li>{@code 0xFF1E1E1E} —— 完全不透明的深灰色背景</li>
 *     <li>{@code 0xFF1E1E1E} — Fully opaque dark gray background</li>
 *     <li>{@code 0xFF00AAFF} —— 不透明的青色边框</li>
 *     <li>{@code 0xFF00AAFF} — Opaque cyan border</li>
 * </ul>
 * 即：{@code 0xAARRGGBB}，AA = 透明度（00 全透明，FF 不透明）。
 * <p>In other words: {@code 0xAARRGGBB}, where AA = opacity (00 fully transparent, FF opaque).
 *
 * @param background          提示框内部填充色（含透明度）
 * <p>background          Tooltip inner fill color (including opacity)
 * @param borderTop           边框上边 / 左右两侧渐变起点的颜色
 * <p>borderTop           Top border / gradient start color on the left and right vertical borders
 * @param borderBottom        边框下边 / 左右两侧渐变终点的颜色
 * <p>borderBottom        Bottom border / gradient end color on the left and right vertical borders
 * @param duplicateBorderGradient 为 {@code true} 时左右竖边框按上→下做渐变；为 {@code false} 时使用纯 {@code borderTop} 颜色（更"硬"的实线边框）
 * <p>duplicateBorderGradient Whether the left and right vertical borders are gradient from top to bottom when {@code true};
 *                                when {@code false}, the solid {@code borderTop} color is used (a harsher solid border)
 */
public record TooltipStyle(int background, int borderTop, int borderBottom, boolean duplicateBorderGradient) {

    /* 原版 1.21.1 的配色：背景 0xF0100010，边框 0x505000FF → 0x5028007F。 */
    /* Vanilla 1.21.1 palette: background 0xF0100010, border 0x505000FF → 0x5028007F. */
    public static final TooltipStyle VANILLA = new TooltipStyle(0xF0100010, 0x505000FF, 0x5028007F, true);

    /* 单一颜色边框：只关心一个边框颜色时用它构造。 */
    /* Single-color border: use this constructor when only a single border color matters. */
    public static TooltipStyle ofSolidBorder(int background, int border) {
        return new TooltipStyle(background, border, border, false);
    }

    /** 上下渐变边框：左右竖边从上到下由 {@code borderTop} 渐变到 {@code borderBottom}。
    * <p>Top-to-bottom gradient border: the left and right vertical edges transition from {@code borderTop} to {@code borderBottom} from top to bottom. */
    public static TooltipStyle ofGradientBorder(int background, int borderTop, int borderBottom) {
        return new TooltipStyle(background, borderTop, borderBottom, true);
    }

    /* 只替换背景色，边框保持原版。 */
    /* Replace only the background color while keeping the original border style. */
    public static TooltipStyle withBackground(int background) {
        return new TooltipStyle(background, VANILLA.borderTop(), VANILLA.borderBottom(), true);
    }

    public TooltipStyle withBackgroundColor(int background) {
        return new TooltipStyle(background, this.borderTop, this.borderBottom, this.duplicateBorderGradient);
    }

    public TooltipStyle withBorderColor(int border) {
        return new TooltipStyle(this.background, border, border, false);
    }

    public TooltipStyle withBorderGradient(int borderTop, int borderBottom) {
        return new TooltipStyle(this.background, borderTop, borderBottom, true);
    }
}
