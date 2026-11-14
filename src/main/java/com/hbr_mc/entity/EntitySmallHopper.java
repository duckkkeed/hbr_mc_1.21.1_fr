package com.hbr_mc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.world.World;

/** SmallHopper（1.12.2 {@code EntitySmallHopper}）：1.8×2，10 生命 / 3 攻击 / 0.3 移速 / 32 索敌 / 20 护盾，3 级。 */
public class EntitySmallHopper extends EntityCancer {

    public EntitySmallHopper(EntityType<? extends EntitySmallHopper> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSmallHopperAttributes() {
        return cancerAttributes(10.0, 3.0, 0.3, 32.0, 20.0);
    }

    @Override
    protected float getInitShield() {
        return 20.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }
}
