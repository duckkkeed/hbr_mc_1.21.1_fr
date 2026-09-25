package com.hbr_mc.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class ModFoodComponents {
    public static final FoodComponent ONION = new FoodComponent.Builder().nutrition(2).saturationModifier(0.3f)
            .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,40),0.5f).build();
    public static final FoodComponent KNIFE_CUT_NOODLES = new FoodComponent.Builder().nutrition(7).saturationModifier(1.3f).build();
}
