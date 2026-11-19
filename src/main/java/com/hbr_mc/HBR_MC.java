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
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// 自定义数据组件须在物品之前注册
		com.hbr_mc.component.ModComponents.register();
		// 六维属性必须尽早注册：属性容器在实体构造时就会追加它们（见 AttributeContainerMixin），
		// 而注册表在模组初始化结束后冻结，晚注册会直接崩。
		com.hbr_mc.attribute.ModAttributes.register();
		ModItems.registerModItems();
		com.hbr_mc.recipe.ModRecipes.register();
		ModItemGroups.registerModItemGroups();
		ModBlocks.registerModBlocks();
		com.hbr_mc.entity.ModEntities.register();
		ModPotion.registerModPotions();

		com.hbr_mc.worldgen.HBR_MCWorldGen.register();

		CommandSeraphTP.register();
		com.hbr_mc.command.CommandSeraph.register();

		com.hbr_mc.event.SeraphDamageAbsorb.register();

		com.hbr_mc.event.SeraphAttackRework.register();
		// 开发期自检（只有设置环境变量 HBR_MC_SELFTEST=true 时才生效，否则什么都不做）
		com.hbr_mc.debug.SeraphSelfTest.register();
		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
