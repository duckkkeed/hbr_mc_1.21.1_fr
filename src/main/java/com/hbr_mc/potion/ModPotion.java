package com.hbr_mc.potion;

import com.hbr_mc.HBR_MC;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModPotion {
    public static final StatusEffect SELF_RECOIL = register("self_recoil", new StatusEffect(StatusEffectCategory.HARMFUL, 0xFF6B6B) {
        {
            addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, HBR_MC.id("self_recoil_movement_speed"), -0.9, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, HBR_MC.id("self_recoil_attack_speed"), -0.9, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return false;
        }
    });

    private static StatusEffect register(String id, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, HBR_MC.id(id), effect);
    }

    public static void registerModPotions() {
        HBR_MC.LOGGER.info("Registering status effects");
    }
}
