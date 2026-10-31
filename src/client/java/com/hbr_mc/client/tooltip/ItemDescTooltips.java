package com.hbr_mc.client.tooltip;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

/**
 * 通用「物品说明行」，对应 1.12.2 的 {@code client/DescForNormal}。
 *
 * <p>规则与原版一致：取物品的翻译键（如 {@code item.hbr_mc.gem_red}）拼上 {@code .desc}，
 * 若当前语言里存在这个键，就把译文插到提示框第 2 行（物品名下面）；不存在则什么都不做。
 * 这样新增物品只要在语言文件里补一条 {@code item.hbr_mc.xxx.desc} 就有说明，不需要改代码。
 *
 * <p><b>为什么放在客户端</b>：说明是纯展示，且 {@link I18n} 是客户端类；
 * 1.12.2 也是 {@code @SideOnly(Side.CLIENT)} 的 tooltip 事件。
 */
public final class ItemDescTooltips {

    private static final String DESC_SUFFIX = ".desc";

    private ItemDescTooltips() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            String key = stack.getItem().getTranslationKey() + DESC_SUFFIX;
            if (!I18n.hasTranslation(key)) {
                return;
            }
            Text desc = Text.translatable(key);
            if (lines.size() > 1) {
                lines.add(1, desc);
            } else {
                lines.add(desc);
            }
        });
    }
}
