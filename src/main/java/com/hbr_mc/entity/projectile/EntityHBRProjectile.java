package com.hbr_mc.entity.projectile;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * 炽天使的通用弹丸，移植自 1.12.2 的 {@code entities/projectiles/EntityHBRProjectile}
 * （以及它的父类 {@code EntityIdlProjectile} 里与本 mod 相关的部分）。
 *
 * <h2>行为</h2>
 * <ul>
 *   <li>沿发射方向直线飞行，每 tick 速度 ×0.99（原版 {@code f = 0.99f}）；</li>
 *   <li>存在时间超过 {@link #TTL_TICKS} 自动消失（原版 {@code args.ttl}）；</li>
 *   <li>命中方块：播放"熔岩熄灭"音效并消失；</li>
 *   <li>命中生物：按 {@link #attackType} 走 {@link CombatUtil} 的同一套伤害公式；</li>
 *   <li>生成时快照 {@link ItemSeraphBase#currentBuffRateTotal}（原版同名字段 {@code atkUpRate}）。</li>
 * </ul>
 *
 * <h2>与 1.12.2 的差异</h2>
 * <ul>
 *   <li>原版继承 {@code EntityIdlProjectile}（自带 {@code ProjectileArgs} 的爆炸/燃烧/穿甲等),
 *       本工程只用到直线弹丸这一种形态，所以直接继承 {@code Entity} 并只保留用得到的部分；</li>
 *   <li>原版在客户端用 {@code EnumParticleTypes.FLAME}/{@code CLOUD} 画弹道，1.21.1 对应
 *       {@link ParticleTypes#FLAME} / {@link ParticleTypes#CLOUD}；</li>
 *   <li>原版靠 {@code ProjectileHelper.forwardsRaycast} 做碰撞；1.21.1 没有等价工具，
 *       这里用 {@code World#raycast} 处理方块、用射线-包围盒相交处理生物。</li>
 * </ul>
 */
public class EntityHBRProjectile extends Entity {

    /** 最长存活 tick 数。 */
    public static final int TTL_TICKS = 100;

    /** 判定生物时给包围盒留的余量（原版 {@code getCollisionBorderSize} 返回 1.0）。 */
    private static final double HIT_MARGIN = 0.3D;

    /** 发射者，伤害归属用。只在服务端有效（客户端不需要）。 */
    public LivingEntity shooter;

    /** 攻击属性类型，决定伤害公式。 */
    public CombatUtil.EnumAttrType attackType = CombatUtil.EnumAttrType.STANDARD;

    /** 技能威力与封顶，含义见 {@link CombatConfig.SkillConf}。 */
    public float minPotency = 100.0F;
    public float cap = 150.0F;

    /** 生成时的攻击力上升累计（原版同名字段；当前伤害公式并不直接使用，保留以便后续接入）。 */
    public float atkUpRate;

    private int ticksInAir;

    public EntityHBRProjectile(EntityType<? extends EntityHBRProjectile> entityType, World world) {
        super(entityType, world);
    }

    /**
     * 发射（对应原版 {@code shoot(...)} + {@code setPositionAndUpdate(...)}）。
     *
     * @param direction 归一化前的方向向量
     * @param speed     初速度
     */
    public void launch(LivingEntity owner, Vec3d direction, float speed) {
        this.shooter = owner;
        this.atkUpRate = ItemSeraphBase.currentBuffRateTotal;
        Vec3d normalized = direction.normalize();
        setPos(owner.getX() + normalized.x, owner.getEyeY() + normalized.y, owner.getZ() + normalized.z);
        setVelocity(normalized.multiply(speed));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            spawnTrail();
            return;
        }
        serverTick();
    }

    private void serverTick() {
        Vec3d motion = getVelocity();
        Vec3d from = getPos();
        Vec3d to = from.add(motion);

        BlockHitResult blockHit = getWorld().raycast(new RaycastContext(
                from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        Vec3d end = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getPos();

        LivingEntity target = findEntityHit(from, end);
        if (target != null) {
            impact(target);
            return;
        }
        if (blockHit.getType() != HitResult.Type.MISS) {
            getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_LAVA_EXTINGUISH,
                    SoundCategory.NEUTRAL, 1.0F, 1.0F);
            discard();
            return;
        }

        setPos(end.x, end.y, end.z);
        // 原版每 tick 速度 ×0.99（末段逐渐减速）
        setVelocity(motion.multiply(0.99D));
        if (++ticksInAir > TTL_TICKS) {
            discard();
        }
    }

    /** 找出射线路径上最近的、不是发射者的生物。 */
    private LivingEntity findEntityHit(Vec3d from, Vec3d to) {
        Box searchBox = new Box(from, to).expand(HIT_MARGIN + 1.0D);
        List<Entity> candidates = getWorld().getOtherEntities(this, searchBox,
                entity -> entity instanceof LivingEntity && entity != shooter && entity.isAlive());
        LivingEntity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (Entity candidate : candidates) {
            Optional<Vec3d> hit = candidate.getBoundingBox().expand(HIT_MARGIN).raycast(from, to);
            if (hit.isEmpty()) {
                continue;
            }
            double distanceSquared = hit.get().squaredDistanceTo(from);
            if (distanceSquared < nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearest = (LivingEntity) candidate;
            }
        }
        return nearest;
    }

    /**
     * 命中结算：与 1.12.2 {@code EntityHBRProjectile#onImpact} 一致——
     * 结算前把 {@code currentBuffRateTotal} 换成发射时的快照，结算后恢复。
     */
    private void impact(LivingEntity target) {
        if (shooter != null && target != shooter) {
            float bonusRate = CombatConfig.BONUS_DAMAGE_RATE;
            float lastRate = ItemSeraphBase.currentBuffRateTotal;
            ItemSeraphBase.currentBuffRateTotal = atkUpRate;
            switch (attackType) {
                case STR_FOCUS -> CombatUtil.HPAttack(this, shooter, target, minPotency, cap, bonusRate);
                case DEX_FOCUS -> CombatUtil.DPAttack(this, shooter, target, minPotency, cap, bonusRate);
                default -> CombatUtil.attackAsHBR(this, shooter, target, attackType,
                        CombatUtil.EnumDefType.END, minPotency, cap);
            }
            ItemSeraphBase.currentBuffRateTotal = lastRate;
        }
        getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_LAVA_EXTINGUISH,
                SoundCategory.NEUTRAL, 1.0F, 1.0F);
        discard();
    }

    /** 客户端弹道粒子（对应原版 {@code onImpact} 的 else 分支 + 每 tick 的拖尾）。 */
    private void spawnTrail() {
        Vec3d velocity = getVelocity();
        getWorld().addParticle(ParticleTypes.CLOUD, getX(), getY(), getZ(),
                velocity.x * 0.3D, velocity.y * 0.3D, velocity.z * 0.3D);
        getWorld().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(),
                velocity.x * 0.05D, velocity.y * 0.05D, velocity.z * 0.05D);
    }

    // ------------------------------------------------------------------ 其它 --

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        // 弹丸状态全部在服务端计算，不需要同步字段
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // 弹丸是短命实体，重进游戏后没有必要恢复
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    /** 不参与实体推挤（原版 {@code canBeCollidedWith} 返回 false）。 */
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        return false;
    }
}
