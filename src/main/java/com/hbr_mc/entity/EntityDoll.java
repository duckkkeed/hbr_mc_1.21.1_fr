package com.hbr_mc.entity;

import com.hbr_mc.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * Doll（1.12.2 {@code EntityDoll}）：0.8×1.4，5 生命 / 3 攻击 / 0.25 移速 / 32 索敌 / 10 护盾，
 * 六维 10（{@code set6Attr(10)}），等级 3，额外掉诺瓦原水 C。
 */
public class EntityDoll extends EntityCancer {

    public EntityDoll(EntityType<? extends EntityDoll> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createDollAttributes() {
        return cancerAttributes(5.0, 3.0, 0.25, 32.0, 10.0, 10.0);
    }

    @Override
    protected float getInitShield() {
        return 10.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    protected Item getExtraDrop() {
        return ModItems.BOOST_1_C;
    }
}
