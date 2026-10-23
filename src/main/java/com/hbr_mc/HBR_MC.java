package com.hbr_mc;

import com.hbr_mc.block.ModBlocks;
import com.hbr_mc.item.ModItemGroups;
import com.hbr_mc.item.ModItems;
import com.hbr_mc.command.CommandSeraphTP;
import com.hbr_mc.potion.ModPotion;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBR_MC implements ModInitializer {
	public static final String MOD_ID = "hbr_mc";

	// This logger is used to write text to the console and the log file.
	// 该 logger 用于向控制台和日志文件写入文本。
	// It is considered best practice to use your mod id as the logger's name.
	// 最佳实践是使用你的 mod id 作为 logger 的名称。
	// That way, it's clear which mod wrote info, warnings, and errors.
	// 这样可以明确哪些模组输出了信息、警告和错误。
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// 这段代码会在 Minecraft 进入可加载模组的状态时立即运行。
		// However, some things (like resources) may still be uninitialized.
		// 但某些内容（例如资源）可能仍未完成初始化。
		// Proceed with mild caution.
		// 请谨慎继续执行。
		// 自定义数据组件须在物品之前注册（Seraph 的"已损坏"标记）
		com.hbr_mc.component.ModComponents.register();
		ModItems.registerModItems();
		ModItemGroups.registerModItemGroups();
		ModBlocks.registerModBlocks();
		ModPotion.registerModPotions();
		// Register server commands
		// 注册服务器命令
		CommandSeraphTP.register();
		// 伤害转耐久：主手持有 Seraph 物品时，受到的伤害转为该物品的耐久消耗
		com.hbr_mc.event.SeraphDamageAbsorb.register();
		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
