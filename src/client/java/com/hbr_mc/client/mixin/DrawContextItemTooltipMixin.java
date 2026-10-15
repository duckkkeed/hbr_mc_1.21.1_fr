package com.hbr_mc.client.mixin;

import com.hbr_mc.client.tooltip.TooltipStyleManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为「按物品自定义提示框配色」提供物品上下文（直接调用路径）。
 * <p> item context for item-specific tooltip color customization (direct call path).
 * <p>{@code DrawContext#drawItemTooltip(TextRenderer, ItemStack, int x, int y)} 是另一个带{@code ItemStack} 的提示框绘制入口，用于商人交易格子等位置。它在方法内部同步走到{@code TooltipBackgroundRenderer.render(...)}，所以这里用 HEAD / RETURN 成对注入，记录并在结束后清理物品身份。
 * <p>{@code DrawContext#drawItemTooltip(TextRenderer, ItemStack, int x, int y)} is another tooltip rendering entry that carries an
 * {@code ItemStack}, used for merchant trade slots and similar positions. It flows internally to
 * {@code TooltipBackgroundRenderer.render(...)} within the method, so this mixin uses a HEAD/RETURN pair injection,
 * record the item and clear its identity when finished.
 * <p>容器界面（背包、箱子……）的路径见 {@link HandledScreenTooltipContextMixin}。
 * <p>The container UI path (inventory, chests, etc.) is handled by {@link HandledScreenTooltipContextMixin}.
 */
@Mixin(DrawContext.class)
public abstract class DrawContextItemTooltipMixin {

    /* 记录本次即将绘制的物品。 */
    /* Record the item being drawn this time. */
    @Inject(method = "drawItemTooltip", at = @At("HEAD"), require = 1)
    private void hbr_mc$captureItem(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipStyleManager.setCurrentItem(stack.isEmpty() ? null : stack.getItem());
    }

    /* 绘制结束后清理，回落到全局配色。 */
    /* Clear after rendering ends and fall back to the global style. */
    @Inject(method = "drawItemTooltip", at = @At("RETURN"), require = 1)
    private void hbr_mc$clearItem(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipStyleManager.clearCurrentItem();
    }
}
