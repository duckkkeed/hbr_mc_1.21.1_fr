package com.hbr_mc.entity;

import com.hbr_mc.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * Slasher（1.12.2 {@code EntitySlasher}）：1.5×3，20 生命 / 20 攻击 / 0.3 移速 / 32 索敌 / 50 护盾，
 * 六维 50（{@code set6Attr(50)}），额外掉诺瓦原水 B。
 */
public class EntitySlasher extends EntityCancer {

    public EntitySlasher(EntityType<? extends EntitySlasher> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSlasherAttributes() {
        return cancerAttributes(20.0, 20.0, 0.3, 32.0, 50.0, 50.0);
    }

    @Override
    protected float getInitShield() {
        return 50.0F;
    }

    @Override
    protected Item getExtraDrop() {
        return ModItems.BOOST_1_B;
    }
}
