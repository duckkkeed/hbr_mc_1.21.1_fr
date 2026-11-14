package com.hbr_mc.entity;

import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

/**
 * 星癌体变体基类，对应 1.12.2 的 {@code entities/cancer/EntityCancer}。
 *
 * <p>五个变体（Slasher / CrestHopper / SmallHopper / Doll / Marionette）继承本类；
 * <b>本类本身不再注册实体</b>（旧的 {@code hbr_mc:cancer} 已移除）。共用逻辑：
 * <ul>
 *   <li>按子类的 {@link #getInitShield()} 写入「壳」护盾（用原版吸收值表示）；</li>
 *   <li>被玩家击杀时，给附近 32 格内手持炽天使的玩家加 {@link #getXpWorth()} 点武器经验；</li>
 *   <li>AI 暂未移植（{@link #initGoals()} 为空）。</li>
 * </ul>
 */
public abstract class EntityCancer extends HostileEntity {

    protected EntityCancer(EntityType<? extends EntityCancer> entityType, World world) {
        super(entityType, world);
    }

    /** 子类共用的属性构造器；护盾上限 = 该种类的初始护盾。 */
    protected static DefaultAttributeContainer.Builder cancerAttributes(
            double health, double attack, double speed, double follow, double shield) {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, health)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, attack)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, follow)
                // 1.21.1 的吸收值会被上限属性夹断，先把上限抬到护盾值才能设出来。
                .add(EntityAttributes.GENERIC_MAX_ABSORPTION, shield);
    }

    /** AI 暂不移植。 */
    @Override
    protected void initGoals() {
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        // 对应 1.12.2 的 getInitShield()：用吸收值当「壳」，破盾前不掉血。
        setAbsorptionAmount(getInitShield());
        return data;
    }

    /** 初始护盾（对应原版 {@code getInitShield()} / {@code setDPMax()}）。 */
    protected float getInitShield() {
        return 20.0F;
    }

    /** 等级（对应原版 {@code ICancer#getLevel()}），影响击杀经验。 */
    public int getLevel() {
        return 1;
    }

    /** 击杀奖励的武器经验，对应原版 {@code ICancer#getXPWorth()} = 等级 × 10。 */
    public int getXpWorth() {
        return getLevel() * 10;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!getWorld().isClient() && source.getAttacker() instanceof PlayerEntity) {
            // 原版逻辑：给 32 格内所有手持炽天使的玩家加经验（主手优先，其次副手）。
            for (PlayerEntity player : getWorld().getEntitiesByClass(
                    PlayerEntity.class, getBoundingBox().expand(32.0), player -> true)) {
                ItemStack stack = player.getMainHandStack();
                if (!SeraphUtil.isSeraph(stack)) {
                    stack = player.getOffHandStack();
                }
                if (SeraphUtil.isSeraph(stack)) {
                    SeraphUtil.addXP(stack, getXpWorth(), player);
                }
            }
        }
        super.onDeath(source);
    }
}
