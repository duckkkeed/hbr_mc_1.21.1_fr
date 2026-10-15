package com.hbr_mc.client.mixin;

import com.hbr_mc.client.tooltip.TooltipStyle;
import com.hbr_mc.client.tooltip.TooltipStyleManager;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * 修改物品提示框（Tooltip）的背景色与边框色。
 * <p>Modify the background and border colors of item tooltips.
 *
 * <p>1.21.1 中提示框方框由 {@link TooltipBackgroundRenderer#render} 绘制。该方法本身不直接调用 {@code DrawContext#fill}，而是把写死的颜色作为参数传给三个私有辅助方法，调用顺序固定：
 * <p>In 1.21.1, tooltip boxes are drawn by {@link TooltipBackgroundRenderer#render}. This method itself does not directly
 * call {@code DrawContext#fill}; instead, it passes hardcoded colors as arguments to three private helper methods in a fixed call order:
 *
 * <pre>
 * render(context, x, y, width, height, z):
 *   #0 renderHorizontalLine(..., z, 背景色 0xF0100010)   // 背景上边
 *   #0 renderHorizontalLine(..., z, background color 0xF0100010)   // top background edge
 *   #1 renderHorizontalLine(..., z, 背景色 0xF0100010)   // 背景下边
 *   #1 renderHorizontalLine(..., z, background color 0xF0100010)   // bottom background edge
 *   #2 renderRectangle     (..., z, 背景色 0xF0100010)   // 背景主体
 *   #2 renderRectangle     (..., z, background color 0xF0100010)   // main background body
 *   #3 renderVerticalLine  (..., z, 背景色 0xF0100010)   // 背景左边
 *   #3 renderVerticalLine  (..., z, background color 0xF0100010)   // left background edge
 *   #4 renderVerticalLine  (..., z, 背景色 0xF0100010)   // 背景右边
 *   #4 renderVerticalLine  (..., z, background color 0xF0100010)   // right background edge
 *   #5 renderBorder(..., z, 0x505000FF, 0x5028007F)      // 边框起始色 / 结束色
 *   #5 renderBorder(..., z, 0x505000FF, 0x5028007F)      // border start/end colors
 * </pre>
 *
 * <p>这里用 {@link ModifyArg} 直接替换这 6 个调用点上传入的颜色参数。选择 {@code @ModifyArg}而不是在辅助方法内部做 {@code @Redirect}，是因为注入点全部落在 {@code render} 方法体内，位置最稳定：辅助方法里的调用点会被其它提示框 mod 先改掉，而参数修改可以在同一处叠加。
 * <p>Here we use {@link ModifyArg} to directly replace the color arguments passed to these 6 call sites. We choose {@code @ModifyArg}
 * instead of doing {@code @Redirect} inside the helper methods, because all injection points are inside the {@code render} method body,
 * so the position is extremely stable: calls inside helper methods can be modified first by other tooltip mods, while argument patching can be stacked at one point.
 * <p>颜色按<b>原版常量</b>映射（而不是相信参数的语义），未识别的颜色原样透传，因此即便将来辅助方法参数顺序调整，也不会画出错误颜色。
 * <p>Colors are mapped according to the <b>vanilla constants</b> rather than trusting argument semantics; unrecognized colors are passed through unchanged,
 * so even if the helper method parameter order changes in the future, it still will not draw the wrong color.
 *
 * <p>所有注入都带 {@code require = 1}：一旦结构变化，Mixin 会在应用阶段立即报错，
 * All injections include {@code require = 1}: if the structure changes, the Mixin will fail immediately at application time,
 * 而不是静默失效。
 * instead of silently failing.
 *
 * <p><b>与其他提示框 mod 的兼容性</b>：若检测到 {@code customtooltips}（Custom Tooltip API）已安装，本 Mixin 会被 {@code HbrMixinPlugin} 主动禁用——它同样接管提示框背景渲染，两套逻辑同时存在会让外观互相覆盖、配置难以预期。此时请直接使用该 mod 的配置界面。
 * <p><b>Compatibility with other tooltip mods</b>: if {@code customtooltips} (Custom Tooltip API) is detected as installed,
 * this Mixin is disabled by {@code HbrMixinPlugin}—it also takes over tooltip background rendering,
 * and if both sets of logic are active, the appearance will overlap and settings will be difficult to predict. In that case, use the config UI of that mod directly.
 */
@Mixin(TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundMixin {

    /* 原版渲染时使用的三个颜色常量。 */
    /* The three color constants used by the vanilla renderer. */
    private static final int HBR_VANILLA_BACKGROUND = 0xF0100010;
    private static final int HBR_VANILLA_BORDER_START = 0x505000FF;
    private static final int HBR_VANILLA_BORDER_END = 0x5028007F;

    /*把原版的 3 个提示框颜色映射为当前配色（优先当前物品的专属配色），其它颜色原样返回。 */
    /* Map the 3 vanilla tooltip colors to the current style (preferring the current item's dedicated style); other colors are returned unchanged. */
    private static int hbr_mc$mapColor(int vanillaColor) {
        TooltipStyle style = TooltipStyleManager.resolveStyle();
        return switch (vanillaColor) {
            case HBR_VANILLA_BACKGROUND -> style.background();
            case HBR_VANILLA_BORDER_START -> style.borderTop();
            case HBR_VANILLA_BORDER_END ->
                    style.duplicateBorderGradient() ? style.borderBottom() : style.borderTop();
            default -> vanillaColor;
        };
    }

    // ================================================ 背景（0xF0100010）=====
    // ================================================ Background (0xF0100010) =====

    /** 背景上边：{@code renderHorizontalLine}（{@code render} 内第 0 次调用）。
    * <p>Top background edge: {@code renderHorizontalLine} (0th call inside {@code render}). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderHorizontalLine(Lnet/minecraft/client/gui/DrawContext;IIIII)V"),
            index = 5,
            require = 1
    )
    private static int hbr_mc$backgroundTop(int color) {
        return hbr_mc$mapColor(color);
    }

    /** 背景下边：{@code renderHorizontalLine}（{@code render} 内第 1 次调用）。
    * <p>Bottom background edge: {@code renderHorizontalLine} (1st call inside {@code render}). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderHorizontalLine(Lnet/minecraft/client/gui/DrawContext;IIIII)V"),
            index = 5,
            require = 1
    )
    private static int hbr_mc$backgroundBottom(int color) {
        return hbr_mc$mapColor(color);
    }

    /** 背景主体矩形：{@code renderRectangle}。
    * <p>Main background rectangle: {@code renderRectangle}. */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderRectangle(Lnet/minecraft/client/gui/DrawContext;IIIIII)V"),
            index = 6,
            require = 1
    )
    private static int hbr_mc$backgroundRectangle(int color) {
        return hbr_mc$mapColor(color);
    }

    /** 背景左侧竖条：单色 {@code renderVerticalLine}（{@code render} 内第 0 次调用）。
    * <p>Left background vertical bar: solid {@code renderVerticalLine} (0th call inside {@code render}). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderVerticalLine(Lnet/minecraft/client/gui/DrawContext;IIIII)V"),
            index = 5,
            require = 1
    )
    private static int hbr_mc$backgroundLeft(int color) {
        return hbr_mc$mapColor(color);
    }

    /** 背景右侧竖条：单色 {@code renderVerticalLine}（{@code render} 内第 1 次调用）。
    * <p>Right background vertical bar: solid {@code renderVerticalLine} (1st call inside {@code render}). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderVerticalLine(Lnet/minecraft/client/gui/DrawContext;IIIII)V"),
            index = 5,
            require = 1
    )
    private static int hbr_mc$backgroundRight(int color) {
        return hbr_mc$mapColor(color);
    }

    // ======================================================= 边框 =====
    // ======================================================= Border =====

    /** 边框起始色：{@code renderBorder} 的 {@code startColor}（左右竖条渐变起点 + 上横线）。
    * <p>Border start color: {@code startColor} of {@code renderBorder} (left/right vertical gradient start + top horizontal line). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderBorder(Lnet/minecraft/client/gui/DrawContext;IIIIIII)V"),
            index = 6,
            require = 1
    )
    private static int hbr_mc$borderStart(int color) {
        return hbr_mc$mapColor(color);
    }

    /** 边框结束色：{@code renderBorder} 的 {@code endColor}（左右竖条渐变终点 + 下横线）。
    * <p>Border end color: {@code endColor} of {@code renderBorder} (left/right vertical gradient end + bottom horizontal line). */
    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderBorder(Lnet/minecraft/client/gui/DrawContext;IIIIIII)V"),
            index = 7,
            require = 1
    )
    private static int hbr_mc$borderEnd(int color) {
        return hbr_mc$mapColor(color);
    }
}
