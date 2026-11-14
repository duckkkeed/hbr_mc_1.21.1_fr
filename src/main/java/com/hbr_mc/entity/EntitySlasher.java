package com.hbr_mc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.world.World;

/** Slasher（1.12.2 {@code EntitySlasher}）：1.5×3，20 生命 / 20 攻击 / 0.3 移速 / 32 索敌 / 50 护盾。 */
public class EntitySlasher extends EntityCancer {

    public EntitySlasher(EntityType<? extends EntitySlasher> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSlasherAttributes() {
        return cancerAttributes(20.0, 20.0, 0.3, 32.0, 50.0);
    }

    @Override
    protected float getInitShield() {
        return 50.0F;
    }
}
