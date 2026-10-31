package com.hbr_mc.client;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.client.tooltip.ItemDescTooltips;
import com.hbr_mc.client.tooltip.SeraphTooltipStyles;
import com.hbr_mc.client.tooltip.TooltipStyleManager;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class HBR_MCClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Seraph 系物品按稀有度（A / S / SS）设置提示框配色，
        // 对应 1.12.2 Forge 版的 onToolTipColor(RenderTooltipEvent.Color)。
        SeraphTooltipStyles.register();

        // 通用物品说明行：语言文件里有 item.hbr_mc.<id>.desc 就会显示（对应 1.12.2 的 DescForNormal）。
        ItemDescTooltips.register();

        // 其它物品沿用原版提示框外观。
        // 若想给"所有物品"统一换配色，去掉下面这行注释并改成你的颜色：
        // TooltipStyleManager.setStyle(TooltipStyle.ofSolidBorder(0xCC0B1A2A, 0xFF35D6FF));

        registerDebugOverrides();

        HBR_MC.LOGGER.info("Hello Fabric client!");
    }

    /**
     * 开发环境调试开关（正式打包不生效）：
     *
     * <pre>
     * gradlew runClient -DhbrMc.testItemStyle=minecraft:stone    # 让石头套用 SS 级配色
     * gradlew runClient -DhbrMc.testItemStyle=minecraft:stone -DhbrMc.testRarity=A
     * </pre>
     */
    private static void registerDebugOverrides() {
        String testItem = System.getProperty("hbrMc.testItemStyle");
        if (testItem == null || !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }

        Identifier id = Identifier.tryParse(testItem);
        Item item = id == null ? null : Registries.ITEM.get(id);
        if (item == null) {
            HBR_MC.LOGGER.warn("[调试] 未找到物品 {}，已忽略 hbrMc.testItemStyle", testItem);
            return;
        }

        String rarityName = System.getProperty("hbrMc.testRarity", "SS");
        EnumSeraphRarity rarity;
        try {
            rarity = EnumSeraphRarity.valueOf(rarityName);
        } catch (IllegalArgumentException e) {
            HBR_MC.LOGGER.warn("[调试] 未知稀有度 {}，改用 SS", rarityName);
            rarity = EnumSeraphRarity.SS;
        }

        TooltipStyleManager.setStyle(item, SeraphTooltipStyles.styleFor(rarity));
        HBR_MC.LOGGER.info("[调试] 已为 {} 套用 {} 级提示框配色", testItem, rarity);
    }
}
