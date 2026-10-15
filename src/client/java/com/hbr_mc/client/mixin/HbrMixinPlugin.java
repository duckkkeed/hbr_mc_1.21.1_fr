package com.hbr_mc.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin 配置插件：用来按运行环境决定某个 Mixin 是否加载。
 * <p>Mixin configuration plugin: used to decide whether a Mixin should be loaded based on the runtime environment.
 *
 * <p>目前只处理一个冲突：{@code customtooltips}（Custom Tooltip API）用自己的{@code TooltipBackgroundRendererMixin} 接管了同一批提示框背景/边框的配色逻辑。两套逻辑同时生效既互相覆盖又容易让人误以为配置无效，因此检测到它时直接不加载{@link TooltipBackgroundMixin}，提示框外观完全交给该 mod。
 * <p>Currently only one conflict is handled: {@code customtooltips} (Custom Tooltip API) uses its own
 * {@code TooltipBackgroundRendererMixin} to take over the same tooltip background/border color logic.
 * If both logic sets are active, they will overlap and make it easy to assume the configuration is ineffective; therefore,
 * when it is detected, {@link TooltipBackgroundMixin} is not loaded and the tooltip appearance is fully delegated to that mod.
 */
public class HbrMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("hbr_mc/mixin");

    /* 与 Custom Tooltip API 冲突的提示框相关 Mixin（一起跳过）。 */
    /* Tooltip-related Mixin classes that conflict with Custom Tooltip API and should be skipped together. */
    private static final Set<String> TOOLTIP_MIXINS = Set.of(
            "com.hbr_mc.client.mixin.TooltipBackgroundMixin",
            "com.hbr_mc.client.mixin.DrawContextItemTooltipMixin",
            "com.hbr_mc.client.mixin.HandledScreenTooltipContextMixin"
    );

    private static final String CONFLICTING_MOD_ID = "customtooltips";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (TOOLTIP_MIXINS.contains(mixinClassName)
                && FabricLoader.getInstance().isModLoaded(CONFLICTING_MOD_ID)) {
            LOGGER.warn("检测到 customtooltips(Custom Tooltip API)，已跳过 hbr_mc 的提示框 Mixin（{}）；"
                    + "提示框背景/边框请使用该 mod 的配置（此时 hbr_mc 的 TooltipStyleManager 不生效）。",
                    mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
