package com.hbr_mc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.world.World;

/** CrestHopper（1.12.2 {@code EntityCrestHopper}）：2×4，继承 SmallHopper 的数值，护盾 40、3 级。 */
public class EntityCrestHopper extends EntitySmallHopper {

    public EntityCrestHopper(EntityType<? extends EntityCrestHopper> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createCrestHopperAttributes() {
        return cancerAttributes(10.0, 3.0, 0.3, 32.0, 40.0);
    }

    @Override
    protected float getInitShield() {
        return 40.0F;
    }
}
