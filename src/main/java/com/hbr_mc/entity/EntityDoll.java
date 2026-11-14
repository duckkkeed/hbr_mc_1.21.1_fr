package com.hbr_mc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.world.World;

/** Doll（1.12.2 {@code EntityDoll}）：0.8×1.4，5 生命 / 3 攻击 / 0.25 移速 / 32 索敌 / 10 护盾，3 级。 */
public class EntityDoll extends EntityCancer {

    public EntityDoll(EntityType<? extends EntityDoll> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createDollAttributes() {
        return cancerAttributes(5.0, 3.0, 0.25, 32.0, 10.0);
    }

    @Override
    protected float getInitShield() {
        return 10.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }
}
