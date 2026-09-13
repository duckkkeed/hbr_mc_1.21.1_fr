package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item BRAVE_BLUE = registerItem("brave_blue", new Item(new Item.Settings()));
    public static final Item RAW_CRYSTAL = registerItem("raw_crystal", new Item(new Item.Settings()));

    private static Item registerItem(String id, Item item)
    {
        return Registry.register(Registries.ITEM, Identifier.of(HBR_MC.MOD_ID, id), item);
    }

    public static void registerModItems()
    {

        HBR_MC.LOGGER.info("Registering Items");
    }
}
