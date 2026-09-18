package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item BRAVE_BLUE = registerItem("brave_blue", new Item(new Item.Settings()));
    public static final Item BRAVE_BLUE_S = registerItem("brave_blue_s", new Item(new Item.Settings()));
    public static final Item BRAVE_BLUE_SS= registerItem("brave_blue_ss", new Item(new Item.Settings()));

    public static final Item RAPID_FIRE = registerItem("rapid_fire", new Item(new Item.Settings()));
    public static final Item RAPID_FIRE_S = registerItem("rapid_fire_s", new Item(new Item.Settings()));
    public static final Item RAPID_FIRE_SS = registerItem("rapid_fire_ss", new Item(new Item.Settings()));

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
