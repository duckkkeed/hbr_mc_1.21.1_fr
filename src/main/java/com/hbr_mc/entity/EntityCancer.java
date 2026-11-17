package com.hbr_mc.entity;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.DpConfig;
import com.hbr_mc.combat.DpService;
import com.hbr_mc.item.seraph.SeraphUtil;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

/**
 * 星癌体变体基类，对应 1.12.2 的 {@code entities/cancer/EntityCancer}（含 {@code EntityBase} 的 DP 部分）。
 *
 * <p>五个变体（Slasher / CrestHopper / SmallHopper / Doll / Marionette）继承本类；
 * <b>本类本身不再注册实体</b>（旧的 {@code hbr_mc:cancer} 已移除）。共用逻辑：
 * <ul>
 *   <li>按子类的 {@link #getInitShield()} 写入「壳」DP（用原版吸收值表示，上限镜像到 {@code DP_MAX} 属性）；</li>
 *   <li><b>壳未破时免疫</b>不能破壳的伤害（对应原版 {@code EntityBase#isEntityInvulnerable} +
 *       {@code CombatUtil.canBreakShield}），只有炽天使持有者、本 mod 弹丸、虚空能破壳；</li>
 *   <li>破壳时：施加「反作用力」20 tick + 玻璃破碎音 + 蓝色釉陶瓦碎块粒子（原版 {@code damageEntity} 的反馈）；</li>
 *   <li>被玩家击杀时，给附近 32 格内手持炽天使的玩家加 {@link #getXpWorth()} 点武器经验；</li>
 *   <li>AI 暂未移植（{@link #initGoals()} 为空）。</li>
 * </ul>
 *
 * <p>DP 的完整说明（玩家侧 / 怪物侧 / HUD / 同步）见 {@code docs/dp-system.md}。
 */
public abstract class EntityCancer extends HostileEntity {

    /**
     * 同步给客户端的「壳/DP 当前值」。
     *
     * <p><b>为什么不能直接用 absorptionAmount</b>：1.21.1 里只有 {@code PlayerEntity}
     * 把吸收值放进了 {@code DataTracker}（{@code PlayerEntity.ABSORPTION_AMOUNT}），
     * 怪物的 {@code absorptionAmount} 是纯服务端字段、<b>不会同步</b>。
     * 客户端读它永远是 0，于是"破壳贴图/HUD"之类的表现会一生成就误判成已破壳。
     * 因此这里自己加一个 tracked data，服务端与客户端都能读到真实壳量。
     */
    private static final TrackedData<Float> SHELL_DP =
            DataTracker.registerData(EntityCancer.class, TrackedDataHandlerRegistry.FLOAT);

    protected EntityCancer(EntityType<? extends EntityCancer> entityType, World world) {
        super(entityType, world);
        // 出生就带壳：initialize 在 /summon 时也会走，但构造器里先给一次更稳
        // （壳量同时写进 tracked data，客户端立刻可见）。
        if (!world.isClient()) {
            setDpMax(getInitShield());
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHELL_DP, getInitShield());
    }

    /** 客户端也能读的壳量（tracked data），对应原版 {@code DP_MAX - DP_LOSS_SYNC}。 */
    public float getShellDp() {
        return this.dataTracker.get(SHELL_DP);
    }

    /** 壳上限（= 该种类的初始护盾）。 */
    public float getShellMax() {
        return getInitShield();
    }

    /** 写壳量（服务端改动后调用；客户端也会收到同步）。 */
    public void setShellDp(float value) {
        this.dataTracker.set(SHELL_DP, Math.max(0.0F, Math.min(value, getShellMax())));
    }

    /**
     * 服务端每 tick 把 absorptionAmount 的变化回写到 tracked data。
     *
     * <p>吸收值可能在很多地方被改（原版伤害结算、`DpService.damageDp`、`setDpMax`），
     * 统一在这里兜底同步，避免漏掉某个入口导致客户端看到旧壳量。
     */
    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient() && getShellDp() != getAbsorptionAmount()) {
            setShellDp(getAbsorptionAmount());
        }
    }

    /** 子类共用的属性构造器；护盾上限 = 该种类的初始护盾，同时写入 DP 上限。 */
    protected static DefaultAttributeContainer.Builder cancerAttributes(
            double health, double attack, double speed, double follow, double shield) {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, health)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, attack)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, follow)
                // 1.21.1 的吸收值会被上限属性夹断，先把上限抬到护盾值才能设出来。
                .add(EntityAttributes.GENERIC_MAX_ABSORPTION, shield)
                // 原版 setDPMax 会把护盾同时写进 DP_MAX；这里从属性构造器就给上，客户端也能同步到。
                .add(ModAttributes.DP_MAX, shield);
    }

    /** AI 暂未移植。 */
    @Override
    protected void initGoals() {
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        // 对应 1.12.2 的 getInitShield() / setDPMax()：用吸收值当「壳」，破盾前不掉血。
        setDpMax(getInitShield());
        return data;
    }

    /** 初始护盾（对应原版 {@code getInitShield()} / {@code setDPMax()}）。 */
    protected float getInitShield() {
        return 20.0F;
    }

    /**
     * 设置 DP 上限，对应原版 {@code EntityBase#setDPMax(float)}：
     * {@code setAbsorptionAmount(value)} + 属性镜像 + 同步。
     *
     * <p>{@code setAbsorptionAmount} 是 {@code final} 且只写服务端字段，
     * 所以这里必须同时把值写进 tracked 的 {@link #SHELL_DP}，客户端才看得到。
     */
    public void setDpMax(float value) {
        setAbsorptionAmount(value);
        setShellDp(value);
        EntityAttributeInstance instance = getAttributeInstance(ModAttributes.DP_MAX);
        if (instance != null) {
            instance.setBaseValue(value);
        }
        DpService.syncStatus(this);
    }

    /** 等级（对应原版 {@code ICancer#getLevel()}），影响击杀经验。 */
    public int getLevel() {
        return 1;
    }

    /** 击杀奖励的武器经验，对应原版 {@code ICancer#getXPWorth()} = 等级 × 10。 */
    public int getXpWorth() {
        return getLevel() * 10;
    }

    // ------------------------------------------------------------------ DP --

    /**
     * 壳未破时，只有"能破壳"的来源才能造成伤害，对应原版
     * {@code EntityBase#isEntityInvulnerable} + {@code CombatUtil.canBreakShield}。
     *
     * <p>严格模式由 {@link DpConfig#STRICT_SHELL} 控制；关掉后退化为"任何伤害都能磨壳"。
     */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (DpConfig.STRICT_SHELL && getAbsorptionAmount() > 0.0F && !DpService.canBreak(source)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    /**
     * 伤害入口：补上原版 {@code EntityBase#attackEntityFrom} 的"打不动时播铁砧音"，
     * 以及 {@code damageEntity} 里"壳破时眩晕 + 特效"的反馈。
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient()) {
            return super.damage(source, amount);
        }

        // 壳还完整、且这个来源打不破：原版会播一个"当当"的音效并完全免疫
        if (DpConfig.STRICT_SHELL && getAbsorptionAmount() > 0.0F && !DpService.canBreak(source)) {
            getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE,
                    SoundCategory.HOSTILE, 1.0F, 1.0F);
            return false;
        }

        float absorptionBefore = getAbsorptionAmount();
        boolean damaged = super.damage(source, amount);
        if (damaged) {
            if (absorptionBefore > 0.0F && getAbsorptionAmount() <= 0.0F) {
                onShellBroken();
            }
            // 立刻回写 tracked 壳量：破壳的同一 tick 客户端就能换成破碎贴图
            setShellDp(getAbsorptionAmount());
            DpService.syncStatus(this);
        }
        return damaged;
    }

    /**
     * 破壳反馈，对应原版 {@code EntityCancer#damageEntity} 里护盾归零的那一段：
     * 眩晕（这里是「反作用力」20 tick）+ 玻璃破碎音 + 蓝色釉陶瓦碎块粒子。
     */
    protected void onShellBroken() {
        EffectUtil.applyTicks(this, ModPotion.SELF_RECOIL, 0, DpConfig.SHELL_BREAK_RECOIL_TICKS);
        getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK,
                SoundCategory.HOSTILE, 1.0F, 1.0F);
        getWorld().addParticle(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BLUE_GLAZED_TERRACOTTA.getDefaultState()),
                getX(), getY() + getHeight() * 0.5D, getZ(), 0.0D, 1.0D, 0.0D);
    }

    // -------------------------------------------------------------- 击杀奖励 --

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
