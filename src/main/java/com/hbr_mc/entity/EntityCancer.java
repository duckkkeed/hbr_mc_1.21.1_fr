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
 * 星癌体（Cancer），对应 1.12.2 的 {@code entities/cancer/EntityCancer}。
 *
 * <p>本次移植的是**服务端实体本体**：
 * <ul>
 *   <li>EntityType 注册（{@code hbr_mc:cancer}）、基础属性、0.6×1.8 碰撞箱；</li>
 *   <li>20 点「壳」护盾（用原版吸收值表示，先扣盾再扣血）；</li>
 *   <li>死亡掉落 1 个癌壳（战利品表 {@code hbr_mc:entities/cancer}）；</li>
 *   <li>被玩家击杀时，给附近 32 格内手持炽天使的玩家加 10 点武器经验（对应原版 {@code getXPWorth()}）。</li>
 * </ul>
 *
 * <p><b>未移植</b>：AI（{@link #initGoals()} 为空）、自然生成/刷怪蛋、客户端模型与渲染、
 * 六维属性与技能升级、护盾破碎的眩晕/粒子效果；另外 1.21.1 的 {@code canBreatheInWater} 是 final，
 * 原版「水下呼吸」这一点也没有复刻。
 */
public class EntityCancer extends HostileEntity {

    public EntityCancer(EntityType<? extends EntityCancer> entityType, World world) {
        super(entityType, world);
    }

    /** 通用小怪数值：20 生命、3 攻击、0.25 移速、16 索敌。 */
    public static DefaultAttributeContainer.Builder createCancerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0)
                // 1.21.1 的吸收值会被上限属性夹断，先把上限抬到 20 才能设出护盾。
                .add(EntityAttributes.GENERIC_MAX_ABSORPTION, 20.0);
    }

    /** AI 暂不移植。 */
    @Override
    protected void initGoals() {
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        // 对应 1.12.2 的 getInitShield() = 20：用吸收值当「壳」，破盾前不掉血。
        setAbsorptionAmount(20.0F);
        return data;
    }

    /** 击杀奖励的武器经验，对应原版 {@code ICancer#getXPWorth()}（等级 1 → 10）。 */
    public int getXpWorth() {
        return 10;
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
