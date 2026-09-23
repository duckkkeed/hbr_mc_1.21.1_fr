package com.hbr_mc.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class ModFoodComponents {
    public static final FoodComponent ONION = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f)
            .build();
}
