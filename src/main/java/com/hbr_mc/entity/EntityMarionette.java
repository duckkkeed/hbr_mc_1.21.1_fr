package com.hbr_mc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.world.World;

/** Marionette（1.12.2 {@code EntityMarionette}）：0.6×1.2，5 生命 / 3 攻击 / 0.15 移速 / 32 索敌 / 5 护盾。 */
public class EntityMarionette extends EntityCancer {

    public EntityMarionette(EntityType<? extends EntityMarionette> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createMarionetteAttributes() {
        return cancerAttributes(5.0, 3.0, 0.15, 32.0, 5.0);
    }

    @Override
    protected float getInitShield() {
        return 5.0F;
    }
}
