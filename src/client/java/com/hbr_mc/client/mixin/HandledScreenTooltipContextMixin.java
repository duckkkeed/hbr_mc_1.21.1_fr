package com.hbr_mc.client.mixin;

import com.hbr_mc.client.tooltip.TooltipStyleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为「按物品自定义提示框配色」提供物品上下文（容器界面路径）。
 * <p>Provide item context for item-specific tooltip color customization (container UI path).
 *
 * <p>真正画方框的 {@code TooltipBackgroundRenderer.render(...)} 参数里没有 {@code ItemStack}，拿不到物品身份，所以要在更上层的绘制入口把物品记下来。物品提示框有两个带{@code ItemStack} 的入口，本 Mixin 负责容器界面这一个：HandledScreen#drawMouseoverTooltip(DrawContext, int mouseX, int mouseY)   // 背包 / 箱子 / 创造物品栏…
 * <pre>{@code
 * HandledScreen#drawMouseoverTooltip(DrawContext, int mouseX, int mouseY)   // 背包 / 箱子 / 创造物品栏…
 *     if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
 *         ItemStack itemStack = this.focusedSlot.getStack();
 *         context.drawTooltip(this.textRenderer, this.getTooltipFromItem(itemStack), itemStack.getTooltipData(), x, y);
 *     }
 * }</pre>
 * <p>The actual tooltip box renderer {@code TooltipBackgroundRenderer.render(...)} receives no {@code ItemStack},
 * so the item identity must be recorded at a higher-level drawing entry. Item tooltips have two entry points that carry
 * an {@code ItemStack}; this mixin handles the container UI one:
 * <pre>{@code
 * HandledScreen#drawMouseoverTooltip(DrawContext, int mouseX, int mouseY)   // inventory / chest / creative inventory…
 *     if (this.handler.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
 *         ItemStack itemStack = this.focusedSlot.getStack();
 *         context.drawTooltip(this.textRenderer, this.getTooltipFromItem(itemStack), itemStack.getTooltipData(), x, y);
 *     }
 * }</pre>
 *
 * <p>注意：{@code ItemStack} 在这里只是<b>局部变量</b>，并不是{@code DrawContext#drawTooltip} 的参数（那个方法根本没有物品参数），因此无法用{@code @ModifyArg} 改写参数，只能在方法体里读取。{@code focusedSlot} 通过 {@link Shadow}
 * 读取：鼠标未悬停在栏位上时为 {@code null}，空槽（如空物品栏格子）时栈为空，两种情况都不设置上下文，配色自动回落到全局配色。
 * <p>Note: here, {@code ItemStack} is only a <b>local variable</b>, not a parameter to
 * {@code DrawContext#drawTooltip} (that method has no item parameter at all), so it cannot be rewritten using
 * {@code @ModifyArg}; it must be read inside the method body. {@code focusedSlot} is read via {@link Shadow}:
 * when the mouse is not hovering a slot, it is {@code null}; when the slot is empty (for example, an empty inventory slot), the stack is empty.
 * In both cases, no context is set and the color falls back to the global style.
 *
 * <p>{@code DrawContext#drawTooltip} 会在同一调用栈内同步走到{@code TooltipBackgroundRenderer.render(...)}（渲染线程），因此上下文一定可见；{@code @At("RETURN")} 负责清理，异常路径由 Mixin 生成的 try/finally 兜底。
 * <p>{@code DrawContext#drawTooltip} runs through {@code TooltipBackgroundRenderer.render(...)} in the same call stack,
 * on the render thread, so the context is always visible here;
 * {@code @At("RETURN")} clears the state, and exception paths are protected by Mixin-generated try/finally blocks.
 *
 * <p>另一个入口 {@code DrawContext#drawItemTooltip} 由 {@link DrawContextItemTooltipMixin} 处理。
 * <p>The other entry point, {@code DrawContext#drawItemTooltip}, is handled by {@link DrawContextItemTooltipMixin}.
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenTooltipContextMixin<T extends ScreenHandler> {

    /**
     * {@code HandledScreen#focusedSlot}：当前鼠标悬停的物品栏位。
     * <p>{@code HandledScreen#focusedSlot}: the inventory slot currently hovered by the mouse.
     *
     * <p>{@link Shadow} 按<b>名称</b>在目标类里查找字段，因此字段名必须与目标字段完全一致（不能用 {@code hbr_mc$} 前缀，否则会报{@code @Shadow field ... was not located in the target class}）；Mixin 会把影子字段与本 Mixin 绑定，不会与其它 Mixin 的同名影子冲突。
     * <p>{@link Shadow} looks up fields by <b>name</b> in the target class, so the field name must exactly match the target field.
     * (You cannot use the {@code hbr_mc$} prefix, otherwise it will report
     * {@code @Shadow field ... was not located in the target class});
     * Mixin binds the shadow field to this mixin and does not conflict with shadow fields of the same name from other mixins.
     */
    @Shadow
    private Slot focusedSlot;

    /* 记录容器界面中当前悬停栏位上的物品，供提示框配色使用。 */
    /* Record the item in the currently hovered slot in the container UI for tooltip color styling. */
    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), require = 1)
    private void hbr_mc$captureFocusedItem(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack stack = this.focusedSlot == null ? ItemStack.EMPTY : this.focusedSlot.getStack();
        TooltipStyleManager.setCurrentItem(stack.isEmpty() ? null : stack.getItem());
    }

    /* 提示框绘制结束后立刻清理物品上下文，避免影响随后的非物品提示框。 */
    /* Clear the item context immediately after tooltip rendering ends to avoid affecting later non-item tooltips. */
    @Inject(method = "drawMouseoverTooltip", at = @At("RETURN"), require = 1)
    private void hbr_mc$clearItemContext(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipStyleManager.clearCurrentItem();
    }
}
