package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup HBR_MC_GROUP_1 = Registry.register(Registries.ITEM_GROUP, Identifier.of(HBR_MC.MOD_ID, "hbr_mc"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.hbr_mc.tab_1"))
                    .icon(() -> new ItemStack(ModItems.BRAVE_BLUE))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.BRAVE_BLUE));
                        entries.add(new ItemStack(ModItems.BRAVE_BLUE_S));
                        entries.add(new ItemStack(ModItems.BRAVE_BLUE_SS));

                        entries.add(new ItemStack(ModItems.RAPID_FIRE));
                        entries.add(new ItemStack(ModItems.RAPID_FIRE_S));
                        entries.add(new ItemStack(ModItems.RAPID_FIRE_SS));

                        entries.add(new ItemStack(ModItems.RAW_CRYSTAL));
                        entries.add(new ItemStack(ModItems.CRYSTAL_LOTTERY));
                        entries.add(new ItemStack(ModBlocks.CRYSTAL_ORE));
                    })
                    .build());

    public static final ItemGroup HBR_MC_GROUP_2 = Registry.register(Registries.ITEM_GROUP, Identifier.of(HBR_MC.MOD_ID, "hbr_mc_2"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.hbr_mc.tab_2"))
                    .icon(() -> new ItemStack(ModItems.BRAVE_BLUE))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.BRAVE_BLUE));
                    })
                    .build());

    public static void registerModItemGroups() {
        HBR_MC.LOGGER.info("Registering Item Groups");
    }

}
