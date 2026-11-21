package com.hbr_mc.entity;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.combat.DpConfig;
import com.hbr_mc.combat.DpService;
import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.SeraphUtil;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

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

    /**
     * 雪地/沙漠额外六维的 modifier id，对应原版 {@code EntityCancer#biomeDiff} 那个固定 UUID。
     *
     * <p>用<b>持久</b> modifier 表示：{@code LivingEntity} 会把 {@code attributes} 写进实体 NBT
     * （{@code writeCustomDataToNbt}），持久 modifier 跟着一起存/读，所以加成不会随区块重载丢失
     * （等价于原版 {@code new AttributeModifier(...).setSaved(true)}）。
     */
    private static final Identifier BIOME_BOOST_ID = HBR_MC.id("cancer_biome_boost");

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

    /**
     * 子类共用的属性构造器；护盾上限 = 该种类的初始护盾，同时写入 DP 上限。
     *
     * @param six 六维（STR/DEX/END/MEN/INT/LUC 全部取同一个值），对应原版 {@code set6Attr(value)}
     */
    protected static DefaultAttributeContainer.Builder cancerAttributes(
            double health, double attack, double speed, double follow, double shield, double six) {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, health)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, attack)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, follow)
                // 1.21.1 的吸收值会被上限属性夹断，先把上限抬到护盾值才能设出来。
                .add(EntityAttributes.GENERIC_MAX_ABSORPTION, shield)
                // 原版 setDPMax 会把护盾同时写进 DP_MAX；这里从属性构造器就给上，客户端也能同步到。
                .add(ModAttributes.DP_MAX, shield)
                // 六维：原版 set6Attr(value) 把六项设成同一个值。
                // 默认容器里的基值只有在 AttributeContainerMixin 不抢占 custom 时才会生效，见该 mixin。
                .add(ModAttributes.STR, six)
                .add(ModAttributes.DEX, six)
                .add(ModAttributes.END, six)
                .add(ModAttributes.MEN, six)
                .add(ModAttributes.INT, six)
                .add(ModAttributes.LUC, six);
    }

    /**
     * AI，逐条对应 1.12.2 的 {@code EntityWalkerCancerBase#initEntityAI}：
     *
     * <table>
     *   <caption>目标对照</caption>
     *   <tr><th>1.12.2</th><th>1.21.1</th><th>优先级</th></tr>
     *   <tr><td>{@code EntityAIAttackMelee(this, 1.0, false)}</td><td>{@link MeleeAttackGoal}</td><td>2</td></tr>
     *   <tr><td>{@code EntityAIWanderAvoidWater(this, 0.3)}</td><td>{@link WanderAroundFarGoal}</td><td>7</td></tr>
     *   <tr><td>{@code EntityAIWatchClosest(Player, 8)}</td><td>{@link LookAtEntityGoal}</td><td>8</td></tr>
     *   <tr><td>{@code EntityAILookIdle}</td><td>{@link LookAroundGoal}</td><td>8</td></tr>
     *   <tr><td>{@code EntityAIHurtByTarget(this, true, EntityCancer.class)}</td>
     *       <td>{@link RevengeGoal} + {@code setGroupRevenge()}</td><td>1</td></tr>
     *   <tr><td>{@code EntityAINearestAttackableTarget(Player, true)}</td>
     *       <td>{@link ActiveTargetGoal}(Player, checkVisibility=true)</td><td>2</td></tr>
     *   <tr><td>{@code EntityAINearestAttackableTarget(Villager, false)}</td>
     *       <td>{@link ActiveTargetGoal}(Villager, checkVisibility=false)</td><td>3</td></tr>
     * </table>
     *
     * <p>原版把这段放在 {@code EntityWalkerCancerBase}；本工程五个变体都直接继承 {@code EntityCancer}，
     * 所以统一放在这里（五个变体都是"walker"）。
     */
    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.3D));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));

        // 原版 EntityAIHurtByTarget(this, true, EntityCancer.class)：被打就反击，并招呼同类
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, VillagerEntity.class, false));
    }

    /**
     * 近战结算，对应 1.12.2 的 {@code EntityBase#attackEntityAsMob} →
     * {@code standardAttack} → {@code CombatUtil.attackAsHBR(this, target, 150, damage)}。
     *
     * <p>原版怪物近战走的是六维公式（攻击方 {@code (STR+DEX)/2} 对防御方 END 偏好），
     * 而不是原版 {@code ATTACK_DAMAGE} 直接扣血；1.21.1 默认的 {@code MobEntity#tryAttack}
     * 只做"攻击力 + 附魔 - 护甲"，所以这里整段替换。
     */
    @Override
    public boolean tryAttack(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return super.tryAttack(target);
        }
        float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damage <= 0.0F) {
            return false;
        }
        // 原版 standardAttack 的 cap 固定 150（注意 1.21.1 的形参顺序是 lowDamage, cap）
        CombatUtil.attackAsHBR(this, living, damage, 150.0F);
        return true;
    }

    /**
     * 自然生成的落点校验，对应 1.12.2 {@code EntityCancer#getCanSpawnHere}：
     * {@code super.getCanSpawnHere() && world.canSeeSky(pos.up())}。
     *
     * <p><b>为什么这里不查光照/难度</b>：1.12.2 的 {@code EntityCancer} 继承的是
     * {@code EntityBase → EntityCreature}，<b>不是 {@code EntityMob}</b>（只是 {@code implements IMob}，
     * 那是个标记接口）。而"怪物只在暗处生成"的 {@code EntityMob#getCanSpawnHere}
     * （{@code difficulty != PEACEFUL && isValidLightLevel()}）只写在 {@code EntityMob} 里，
     * 所以星癌体从来没有这条限制：<b>白天只要头顶见天就会刷</b>。
     *
     * <p>因此 1.21.1 这边也只用 {@link MobEntity#canMobSpawn}（检查脚下方块
     * {@code allowsSpawning}，不涉及光照与难度）；和平难度的拦截在刷怪器层面
     * （{@code SpawnHelper} 的 {@code spawnMonsters} 标志），与 1.12.2 的
     * {@code WorldProvider#setAllowedSpawnTypes} 等价。
     *
     * <p>天空可见只对<b>自然生成</b>生效，刷怪蛋 / 命令不受影响。
     *
     * <p><b>注意还有第二道光照关</b>：{@code SpawnHelper} 之后还会调用
     * {@code PathAwareEntity#canSpawn(WorldAccess, SpawnReason)}，它要求
     * {@code getPathfindingFavor(...) >= 0}；{@code HostileEntity} 把这个值实现成
     * {@code 0.5 - 亮度}（趋暗），白天会直接判 false。所以这里必须同时把
     * {@link #getPathfindingFavor(BlockPos, WorldView)} 覆写成 0（见下），否则白天仍然刷不出来。
     */
    public static boolean canSpawn(EntityType<? extends EntityCancer> type, ServerWorldAccess world,
                                   SpawnReason spawnReason, BlockPos pos, Random random) {
        if (spawnReason == SpawnReason.NATURAL && !world.isSkyVisible(pos.up())) {
            return false;
        }
        return MobEntity.canMobSpawn(type, world, spawnReason, pos, random);
    }

    /**
     * 覆写成 0，对应 1.12.2 {@code EntityCreature#getBlockPathWeight}（返回 {@code 0.0F}）。
     *
     * <p>1.21.1 的 {@code HostileEntity} 把"趋暗"写进了这个方法（{@code 0.5 - 亮度}），
     * 而 {@code PathAwareEntity#canSpawn} 又用它当自然生成的第二道门槛 —— 不覆写的话
     * 白天亮度 1.0 会得到 {@code -0.5}，生成被拦下（1.12.2 没有这条，因为
     * {@code EntityMob} 的重写未被继承）。
     */
    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0F;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        // 对应 1.12.2 的 getInitShield() / setDPMax()：用吸收值当「壳」，破盾前不掉血。
        setDpMax(getInitShield());
        // 对应 1.12.2 onInitialSpawn 里的雪地/沙漠额外难度。
        applyBiomeBoost(world);
        return data;
    }

    /** 初始护盾（对应原版 {@code getInitShield()} / {@code setDPMax()}）。 */
    protected float getInitShield() {
        return 20.0F;
    }

    // ------------------------------------------------------------------ 六维 --

    /**
     * 雪地 / 沙漠额外六维，对应原版 {@code EntityCancer#onInitialSpawn} 里
     * {@code if (SNOWY) boost6Attr(SNOWY_EXTRA_DIFFICULTY); else if (SANDY) boost6Attr(DESERT_EXTRA_DIFFICULTY);}。
     *
     * <p>1.12.2 用 Forge 的 {@code BiomeDictionary.Type.SNOWY / SANDY}；1.21.1 没有等价字典，这里用：
     * <ul>
     *   <li>雪地：{@link Biome#isCold(BlockPos)}（原版语义即 {@code temperature < 0.15}）；</li>
     *   <li>沙漠：{@code IS_BADLANDS} 标签，或基础温度 {@code >= 2.0}（沙漠/恶地的温度）。</li>
     * </ul>
     */
    private void applyBiomeBoost(WorldView world) {
        float boost = biomeBoostFor(world, getBlockPos());
        if (boost <= 0.0F) {
            return;
        }
        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.SIX) {
            EntityAttributeInstance instance = getAttributeInstance(attribute);
            if (instance == null) {
                continue;
            }
            instance.removeModifier(BIOME_BOOST_ID);
            instance.addPersistentModifier(new EntityAttributeModifier(
                    BIOME_BOOST_ID, boost, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** 该位置该加多少六维；0 表示普通群系。 */
    private static float biomeBoostFor(WorldView world, BlockPos pos) {
        RegistryEntry<Biome> biome = world.getBiome(pos);
        if (biome.value().isCold(pos)) {
            return CancerConfig.SNOWY_EXTRA_DIFFICULTY;
        }
        if (biome.isIn(BiomeTags.IS_BADLANDS) || biome.value().getTemperature() >= 2.0F) {
            return CancerConfig.DESERT_EXTRA_DIFFICULTY;
        }
        return 0.0F;
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

    // ---------------------------------------------------------------- 战利品 --

    /**
     * 空战利品表：星癌体的掉落是"按 STR 属性算概率"的，数据包战利品表读不到实体属性，
     * 因此这里返回 {@link LootTables#EMPTY}，全部改由 {@link #dropLoot} 用代码生成
     * （对应原版 {@code dropLoot} → {@code handleCancerDrop} → {@code dropByAttribute}）。
     *
     * <p>{@code MobEntity#getLootTable()} 是 {@code final}，所以覆盖它调用的
     * {@code getLootTableId()}（{@code /summon} 之类走 {@code DeathLootTable} NBT 的路径不受影响）。
     */
    @Override
    protected RegistryKey<LootTable> getLootTableId() {
        return LootTables.EMPTY;
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        // 默认是空表，什么都不会掉；但若实体身上被写了 DeathLootTable NBT（/summon 可指定），
        // super 仍会照常结算那张表，所以这里保留调用。
        super.dropLoot(damageSource, causedByPlayer);
        handleCancerDrop();
    }

    /** 对应原版 {@code EntityCancer#handleCancerDrop}：先掉癌壳，再掉变体各自的材料。 */
    protected void handleCancerDrop() {
        dropByAttribute(ModItems.CANCER_SHELL, 1);
        Item extra = getExtraDrop();
        if (extra != null) {
            dropByAttribute(extra, 1);
        }
    }

    /**
     * 各变体额外掉的材料，对应原版在 {@code handleCancerDrop} 覆写里调用的
     * {@code dropByAttribute(BOOST_1_X, 1)}；没有额外掉落的变体返回 {@code null}。
     */
    protected Item getExtraDrop() {
        return null;
    }

    /** 对应原版 {@code EntityCancer#dropByAttribute}：掉率 = {@code STR / 100}。 */
    protected void dropByAttribute(Item item, int sizePerDrop) {
        float chance = ModAttributes.getFloat(this, ModAttributes.STR) / 100.0F;
        advancedDrop(chance, item, sizePerDrop);
    }

    /**
     * 对应原版 {@code EntityCancer#advancedDrop}：整数部分必掉，余数按概率掉。
     *
     * <p><b>与原版的差异</b>：原版把"整数部分"攒在 {@code sizeTotal} 里，只有余数判定成功时
     * 才一起 {@code dropItem}；判定失败时整数部分会凭空消失（STR ≥ 100，即雪地/沙漠加成后才会触发，
     * 疑似 bug）。这里按意图修正为整数部分必掉。
     */
    protected void advancedDrop(float dropChance, Item item, int sizePerDrop) {
        int guaranteed = (int) dropChance;
        if (guaranteed > 0) {
            dropItems(item, guaranteed * sizePerDrop);
        }
        if (getRandom().nextFloat() < dropChance - guaranteed) {
            dropItems(item, sizePerDrop);
        }
    }

    /** 按最大堆叠数拆成若干 {@link ItemStack} 掉出（原版 {@code dropItem} 的 while 拆分）。 */
    private void dropItems(Item item, int total) {
        int maxStack = item.getMaxCount();
        while (total > 0) {
            int size = Math.min(total, maxStack);
            dropStack(new ItemStack(item, size));
            total -= size;
        }
    }

    // -------------------------------------------------------------- 击杀奖励 --

    @Override
    public void onDeath(DamageSource source) {
        if (!getWorld().isClient() && source.getAttacker() instanceof PlayerEntity) {
            // 原版逻辑：给 32 格内所有手持炽天使的玩家加经验（主手优先，其次副手）。
            // 经验 = 等级 × 10 + 5 × STR（原版 dropLoot 里的 getXPWorth() + 5 * STR）。
            int xp = getXpWorth() + (int) (5.0F * ModAttributes.getFloat(this, ModAttributes.STR));
            for (PlayerEntity player : getWorld().getEntitiesByClass(
                    PlayerEntity.class, getBoundingBox().expand(32.0), player -> true)) {
                ItemStack stack = player.getMainHandStack();
                if (!SeraphUtil.isSeraph(stack)) {
                    stack = player.getOffHandStack();
                }
                if (SeraphUtil.isSeraph(stack)) {
                    SeraphUtil.addXP(stack, xp, player);
                }
            }
        }
        super.onDeath(source);
    }
}
