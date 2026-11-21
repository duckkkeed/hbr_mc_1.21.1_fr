package com.hbr_mc.entity;

import com.hbr_mc.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * SmallHopper（1.12.2 {@code EntitySmallHopper}）：1.8×2，10 生命 / 3 攻击 / 0.3 移速 / 32 索敌 / 20 护盾，
 * 六维 20（{@code set6Attr(20)}），等级 3，额外掉诺瓦原水 A。
 */
public class EntitySmallHopper extends EntityCancer {

    public EntitySmallHopper(EntityType<? extends EntitySmallHopper> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSmallHopperAttributes() {
        return cancerAttributes(10.0, 3.0, 0.3, 32.0, 20.0, 20.0);
    }

    @Override
    protected float getInitShield() {
        return 20.0F;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    /**
     * 追击时不停跳跃，对应 1.12.2 {@code EntityCancer#updateAITasks} 里的：
     * {@code if (this instanceof EntitySmallHopper && onGround && getAttackTarget() != null) jump();}
     */
    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient() && isOnGround() && getTarget() != null) {
            jump();
        }
    }

    @Override
    protected Item getExtraDrop() {
        return ModItems.BOOST_1_A;
    }
}
