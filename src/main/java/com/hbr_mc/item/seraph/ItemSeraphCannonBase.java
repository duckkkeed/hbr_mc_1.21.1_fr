package com.hbr_mc.item.seraph;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.entity.ModEntities;
import com.hbr_mc.entity.projectile.EntityHBRProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 炮/枪类炽天使的基类，移植自 1.12.2 的 {@code ItemSeraphCannonBase}。
 *
 * <h2>操作方式（与原版一致）</h2>
 * <ul>
 *   <li><b>右键开始蓄力</b>（{@link #use}）：只播"烈焰人发射"音效，不立刻开火；</li>
 *   <li><b>松手</b>（{@link #onStoppedUsing}）：若蓄力过（{@code remainingUseTicks > 0}）就打出 3 连射，
 *       冷却 10 tick；</li>
 *   <li>蓄力上限 3 秒（{@link #getMaxUseTime} = 60 tick）。</li>
 * </ul>
 *
 * <h2>弹道</h2>
 * 统一走 {@link EntityHBRProjectile}：沿视线方向、初速 {@code 1.8 + 0.2*i}、每 tick ×0.99。
 * {@link #spreadAttack} 是"多轮 × 扇面"的散射（RapidFire / Kazabana），{@link #volleyAttack} 是"同向多段"连射。
 */
public class ItemSeraphCannonBase extends ItemSeraphBase {

    /** 蓄力上限：3 秒（原版 {@code 3 * CommonDef.TICK_PER_SECOND}）。 */
    private static final int MAX_USE_TICKS = 3 * 20;

    /** 普通攻击冷却（原版 {@code setCooldown(this, 10)}）。 */
    private static final int NORMAL_ATTACK_COOLDOWN_TICKS = 10;

    public ItemSeraphCannonBase(Settings settings, EnumSeraphRarity rarity) {
        this(settings, rarity, EnumSeraphType.CANNON);
    }

    public ItemSeraphCannonBase(Settings settings, EnumSeraphRarity rarity, EnumSeraphType type) {
        super(settings, rarity, type);
    }

    /**
     * 散射（原版 {@code spreadAttack}）：{@code volley} 轮、每轮 {@code spread} 发、相邻 {@code perAngle} 度。
     *
     * <p>第 i 轮的水平速度乘以 {@code 1 + 0.4*i}（越靠后的轮次越"散"），
     * 与 1.12.2 的 {@code lookVec.y * (1 + 0.4 * i)} 一致。
     */
    public static void spreadAttack(World world, PlayerEntity player, int volley, int spread, float perAngle,
                                    float potency, float cap, CombatUtil.EnumAttrType type) {
        for (int i = 0; i < volley; i++) {
            Vec3d look = player.getRotationVec(1.0F);
            look = rotate(look, -perAngle * (spread / 2.0F - 0.5F));
            for (int r = 0; r < spread; r++) {
                EntityHBRProjectile bullet = new EntityHBRProjectile(ModEntities.HBR_PROJECTILE, world);
                bullet.shooter = player;
                bullet.minPotency = potency;
                bullet.cap = cap;
                bullet.attackType = type;
                bullet.setPos(player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z);
                Vec3d direction = new Vec3d(look.x, look.y * (1.0D + 0.4D * i), look.z);
                bullet.setVelocity(direction.normalize().multiply(1.8D + i * 0.2D));
                world.spawnEntity(bullet);
                look = rotate(look, perAngle);
            }
        }
    }

    /** 同向连射（原版 {@code volleyAttack} 的三个重载）。 */
    public static void volleyAttack(World world, LivingEntity shooter, int volley, CombatConfig.SkillConf conf) {
        volleyAttack(world, shooter, volley, conf.minPotency(), conf.cap(), CombatUtil.EnumAttrType.STANDARD);
    }

    public static void volleyAttack(World world, LivingEntity shooter, int volley, float minPotency, float cap) {
        volleyAttack(world, shooter, volley, minPotency, cap, CombatUtil.EnumAttrType.STANDARD);
    }

    public static void volleyAttack(World world, LivingEntity shooter, int volley, float minPotency, float cap,
                                    CombatUtil.EnumAttrType atkType) {
        for (int i = 0; i < volley; i++) {
            EntityHBRProjectile bullet = new EntityHBRProjectile(ModEntities.HBR_PROJECTILE, world);
            bullet.shooter = shooter;
            Vec3d look = shooter.getRotationVec(1.0F);
            bullet.minPotency = minPotency / volley;
            bullet.cap = cap;
            bullet.attackType = atkType;
            bullet.setPos(shooter.getX() + look.x, shooter.getEyeY() + look.y, shooter.getZ() + look.z);
            bullet.setVelocity(look.multiply(1.8D + i * 0.2D));
            world.spawnEntity(bullet);
        }
    }

    /** 右键：开始蓄力（原版 {@code onItemRightClick}）。 */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.5F, 0.4F / (user.getRandom().nextFloat() * 0.4F + 0.8F));
        return TypedActionResult.success(stack);
    }

    /**
     * 松手开火（原版 {@code onPlayerStoppedUsing}）。
     *
     * <p>与原版一致：<b>不</b>检查冷却，只要求"已经蓄力过（{@code remainingUseTicks > 0}）"
     * 且身上没有反作用力。
     */
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }
        if (remainingUseTicks <= 0 || !canUseSkills(player)) {
            return;
        }

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            volleyAttack(world, player, 3, CombatConfig.NORMAL_ATK_POWER, CombatConfig.NORMAL_ATK_CAP);
        }

        player.getItemCooldownManager().set(this, NORMAL_ATTACK_COOLDOWN_TICKS);
        player.swingHand(player.getActiveHand());
        player.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TICKS;
    }

    /** 绕 Y 轴旋转向量（原版 {@code rotate}）：{@code deg} 为角度。 */
    public static Vec3d rotate(Vec3d vec, float deg) {
        float rad = deg * 0.017453292F;
        return new Vec3d(vec.x * Math.cos(rad) - vec.z * Math.sin(rad), vec.y,
                vec.x * Math.sin(rad) + vec.z * Math.cos(rad));
    }
}
