package com.hbr_mc.combat;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 战斗公式与范围技能工具，移植自 1.12.2 的 {@code init/util/CombatUtil}。
 *
 * <h2>伤害公式（与原版逐条对应）</h2>
 * <pre>
 * 攻击侧 refAttr：
 *   STANDARD   = (STR + DEX) / 2
 *   STR_FOCUS  = (2*STR + DEX) / 3
 *   DEX_FOCUS  = (STR + 2*DEX) / 3
 *   INT_FOCUS  = INT
 * 防御侧 enemyAttr：
 *   MEN 偏好 = (2*MEN + END) / 3
 *   END 偏好 = (2*END + MEN) / 3
 * 暴击（幸运 LUC 决定）：先给敌方 enemyAttr - 50（不低于 0）
 *
 * refAttr &lt; enemyAttr/2            → 0.01（几乎无效）
 * enemyAttr/2 ≤ refAttr &lt; enemyAttr → lowDamage * 线性 0~1
 * enemyAttr ≤ refAttr &lt; enemyAttr+cap → lowDamage * (1 + 4 * (refAttr-enemyAttr)/cap)
 * refAttr ≥ enemyAttr + cap         → lowDamage * 5
 * 最后再乘 0.9~1.1 的随机浮动
 * </pre>
 *
 * <h2>与 1.12.2 的差异</h2>
 * <ul>
 *   <li>原版伤害走 {@code EntityLivingBase#attackEntityFrom}，这里走 {@code LivingEntity#damage}；
 *       两者都吃护甲/抗性，但 1.21.1 的伤害类型是 {@code DamageType} 注册表，
 *       所以用 {@code playerAttack} / {@code mobAttack} / {@code thrown} 这几种原版伤害来源，
 *       而非自定义伤害类型（与原版 {@code EntityUtil.attack} 的选择一致）。</li>
 *   <li>原版每次攻击前会把 {@code hurtResistantTime} 清零，让多段伤害都能打满；
 *       1.21.1 对应字段是 {@link Entity#timeUntilRegen}，同样清零。</li>
 *   <li>原版的 {@code Main.Log} 调试输出改成 {@code HBR_MC.LOGGER.debug}，默认不刷屏。</li>
 * </ul>
 */
public final class CombatUtil {

    /** 治疗上限（原版 {@code CombatUtil.DEFAULT_HEAL_CAP}）。 */
    public static final float DEFAULT_HEAL_CAP = 200.0F;

    /** 伤害倍率的上限段数（原版 {@code MAX_EXTRA = 4}，即最高 5 倍）。 */
    private static final float MAX_EXTRA = 4.0F;

    /** 是否处于"本 mod 的伤害结算"中（对应原版 {@code isHBRAttackProcess}）。 */
    private static boolean hbrAttackProcess;

    private CombatUtil() {
    }

    /** 攻击属性类型，对应原版 {@code CombatUtil.EnumAttrType}。 */
    public enum EnumAttrType {
        STANDARD,
        STR_FOCUS,
        DEX_FOCUS,
        INT_FOCUS,
    }

    /** 防御属性类型，对应原版 {@code CombatUtil.EnumDefType}。 */
    public enum EnumDefType {
        MEN,
        /** 原版注释：{@code //stamina?} */
        END,
    }

    public static boolean isHbrAttackProcess() {
        return hbrAttackProcess;
    }

    // ------------------------------------------------------------ 伤害来源 --

    /** 普通攻击的伤害来源（原版 {@code EntityUtil.attack(source)}）。 */
    public static DamageSource attackSource(LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            return player.getDamageSources().playerAttack(player);
        }
        return attacker.getDamageSources().mobAttack(attacker);
    }

    /** 投射物命中的伤害来源（原版 {@code EntityUtil.attack(source, projectile).setProjectile()}）。 */
    public static DamageSource projectileSource(Entity projectile, LivingEntity attacker) {
        return weaponSource(projectile, attacker);
    }

    /**
     * 1.21.1 里没有"间接伤害 + 记录发射者"的通用构造器，
     * 用 {@code DamageSources#thrown(projectile, owner)} 表达同一语义（弹射物 + 发射者）。
     */
    private static DamageSource weaponSource(Entity projectile, LivingEntity attacker) {
        if (attacker == null) {
            return projectile.getDamageSources().thrown(projectile, projectile);
        }
        return projectile.getDamageSources().thrown(projectile, attacker);
    }

    // ---------------------------------------------------------------- 治疗 --

    /**
     * 群体修复：给半径 32 内所有玩家的<b>手持炽天使</b>回耐久（原版 {@code areaHeal}）。
     *
     * <p>回血量按施术者 INT 在 [{@code minHeal}, {@code minHeal * 3}] 之间线性插值，
     * 封顶属性为 {@code cap}，与原版公式一致。
     */
    public static List<PlayerEntity> areaHeal(World world, PlayerEntity caster, float minHeal, float cap) {
        List<PlayerEntity> players = playersNear(world, caster, 32.0F);
        float maxHeal = minHeal * 3.0F;
        float intelligence = ModAttributes.getFloat(caster, ModAttributes.INT);
        float healAmount = Math.min(intelligence, cap) / cap * (maxHeal - minHeal) + minHeal;
        for (PlayerEntity player : players) {
            SeraphUtil.cureSeraph(SeraphUtil.getFirstSeraphNonBrokenInHand(player), healAmount);
        }
        return players;
    }

    /**
     * 群体复活：清除半径 32 内所有玩家手持炽天使的"损坏"标记（原版 {@code areaRevive}）。
     *
     * <p>原版会给被复活的玩家发一条 {@code msg.hbr_mc.apply_revive} 提示。
     */
    public static List<PlayerEntity> areaRevive(World world, PlayerEntity caster) {
        List<PlayerEntity> players = playersNear(world, caster, 32.0F);
        for (PlayerEntity player : players) {
            if (SeraphUtil.revive(SeraphUtil.getFirstSeraphInHand(player), 1.0D)) {
                player.sendMessage(net.minecraft.text.Text.translatable("msg.hbr_mc.apply_revive"), false);
            }
        }
        return players;
    }

    // ---------------------------------------------------------------- 攻击 --

    /**
     * 范围攻击（原版 {@code areaAttack}）：以"施术者 + 视线方向 × dist"为中心、半径 radius 的立方体。
     *
     * <p>原版没有排除施术者自己，只是中心点偏移 32 格后施术者恰好落在范围外；
     * 这里显式排除施术者，避免距离为 0 时把自己打死（属于对原实现的稳健化，不改变正常玩法）。
     */
    public static List<LivingEntity> areaAttack(World world, PlayerEntity caster, float dist, float radius,
                                                EnumAttrType atkType, float minPotency, float cap, float bonusRate) {
        Vec3d center = caster.getPos().add(caster.getRotationVec(1.0F).multiply(dist));
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class,
                Box.of(center, radius * 2.0D, radius * 2.0D, radius * 2.0D),
                entity -> entity.isAlive() && entity != caster);
        for (LivingEntity target : targets) {
            generalAttack(atkType, caster, minPotency, cap, bonusRate, target);
        }
        return targets;
    }

    /** 多段范围攻击（原版 {@code areaAttackGroup}）：每一段按比例调用一次 {@link #areaAttack}。 */
    public static void areaAttackGroup(float[] group, World world, PlayerEntity caster, float dist, float radius,
                                       EnumAttrType atkType, float minPotency, float cap, float bonusRate) {
        for (float ratio : group) {
            areaAttack(world, caster, dist, radius, atkType, minPotency * ratio, cap, bonusRate);
        }
    }

    /** 按攻击类型分发到三种属性公式（原版 {@code generalAttack}）。 */
    public static void generalAttack(EnumAttrType atkType, LivingEntity attacker, float minPotency, float cap,
                                     float bonusRate, LivingEntity target) {
        switch (atkType) {
            case STANDARD, INT_FOCUS -> attackAsHBR(attacker, target, atkType, EnumDefType.MEN, minPotency, cap);
            case STR_FOCUS -> HPAttack(attacker, target, minPotency, cap, bonusRate);
            case DEX_FOCUS -> DPAttack(attacker, target, minPotency, cap, bonusRate);
        }
    }

    /**
     * 多段近战（原版 {@code AttackGroup}）：每段按比例、以 STR 对 END 结算。
     *
     * <p>原版这里写死 {@code EnumAttrType.STR_FOCUS, EnumDefType.END}，并<b>忽略</b> bonusRate
     * （直接调 {@code attackAsHBR}），因此没有"打盾加成"。
     */
    public static void attackGroup(LivingEntity attacker, LivingEntity target, float minPotency, float[] group, float cap) {
        for (float ratio : group) {
            attackAsHBR(attacker, target, EnumAttrType.STR_FOCUS, EnumDefType.END, ratio * minPotency, cap);
        }
    }

    /** 多段 HP 攻击（原版 {@code HPAttackGroup}，带打盾加成）。 */
    public static void hpAttackGroup(LivingEntity attacker, LivingEntity target, float minPotency, float[] group,
                                     float cap, float bonusRate) {
        for (float ratio : group) {
            HPAttack(attacker, target, minPotency * ratio, cap, bonusRate);
        }
    }

    /** 以 STR 为主、对 END 结算（原版 {@code HPAttack}）：目标有 DP 时威力 × bonusRate。 */
    public static void HPAttack(LivingEntity attacker, LivingEntity target, float minPotency, float cap, float bonusRate) {
        HPAttack(null, attacker, target, minPotency, cap, bonusRate);
    }

    public static void HPAttack(Entity bullet, LivingEntity attacker, LivingEntity target, float minPotency,
                                float cap, float bonusRate) {
        // 原版用 getAbsorptionAmount() > 0；移植 DP 后统一按"目标还有 DP"判断，
        // 这样"以武器耐久为 DP 的玩家"也能吃到对 DP +30% 的加成（见 docs/dp-system.md）。
        boolean extra = DpService.dp(target) > 0.0F;
        attackAsHBR(bullet, attacker, target, EnumAttrType.STR_FOCUS, EnumDefType.END,
                extra ? minPotency * bonusRate : minPotency, cap);
    }

    /** 以 DEX 为主、对 END 结算（原版 {@code DPAttack}）：目标<b>没有</b> DP 时威力 × bonusRate。 */
    public static void DPAttack(LivingEntity attacker, LivingEntity target, float minPotency, float cap, float bonusRate) {
        DPAttack(null, attacker, target, minPotency, cap, bonusRate);
    }

    public static void DPAttack(Entity bullet, LivingEntity attacker, LivingEntity target, float minPotency,
                                float cap, float bonusRate) {
        boolean extra = DpService.dp(target) <= 0.0F;
        attackAsHBR(bullet, attacker, target, EnumAttrType.DEX_FOCUS, EnumDefType.END,
                extra ? minPotency * bonusRate : minPotency, cap);
    }

    /**
     * 该伤害来源能否打破 DP/壳，对应原版 {@code CombatUtil.canBreakShield}。
     *
     * <p>实现放在 {@link DpService#canBreak}（DP 规则集中在那一个类里），这里保留同名入口方便调用。
     */
    public static boolean canBreakShield(DamageSource source) {
        return DpService.canBreak(source);
    }

    /** 简化版：以 STANDARD / END 结算（原版 {@code attackAsHBR(attacker, target, cap, lowDamage)}）。 */
    public static void attackAsHBR(LivingEntity attacker, LivingEntity target, float lowDamage, float cap) {
        attackAsHBR(null, attacker, target, EnumAttrType.STANDARD, EnumDefType.END, lowDamage, cap);
    }

    public static void attackAsHBR(LivingEntity attacker, LivingEntity target, EnumAttrType atkType, EnumDefType defType,
                                   float lowDamage, float cap) {
        attackAsHBR(null, attacker, target, atkType, defType, lowDamage, cap);
    }

    /**
     * 核心伤害结算，逐行对应原版 {@code CombatUtil#attackAsHBR}。
     *
     * @param bullet 触发伤害的弹射物；{@code null} 表示近战/直接伤害
     */
    public static void attackAsHBR(Entity bullet, LivingEntity attacker, LivingEntity target, EnumAttrType atkType,
                                   EnumDefType defType, float lowDamage, float cap) {
        if (target == null || target.isDead() || target.getHealth() <= 0.0F) {
            return;
        }
        hbrAttackProcess = true;
        // 原版 hurtResistantTime = 0：让多段伤害都能打满
        target.timeUntilRegen = 0;

        double str = ModAttributes.get(attacker, ModAttributes.STR);
        double dex = ModAttributes.get(attacker, ModAttributes.DEX);
        double refAttr = switch (atkType) {
            case STANDARD -> (str + dex) / 2.0D;
            case STR_FOCUS -> (2.0D * str + dex) / 3.0D;
            case DEX_FOCUS -> (str + 2.0D * dex) / 3.0D;
            case INT_FOCUS -> ModAttributes.get(attacker, ModAttributes.INT);
        };

        double men = ModAttributes.get(target, ModAttributes.MEN);
        double end = ModAttributes.get(target, ModAttributes.END);
        double enemyAttr = switch (defType) {
            case MEN -> (men * 2.0D + end) / 3.0D;
            case END -> (end * 2.0D + men) / 3.0D;
        };

        boolean critical = checkCritical(attacker);
        if (critical) {
            enemyAttr = Math.max(0.0D, enemyAttr - 50.0D);
        }

        float damage;
        float halfEnemyAttr = (float) (enemyAttr / 2.0D);
        if (refAttr < halfEnemyAttr) {
            damage = 0.01F;
        } else if (refAttr < enemyAttr) {
            // 0 ~ LOW：线性缩放
            float ratio = (float) ((refAttr - halfEnemyAttr) / halfEnemyAttr);
            damage = lowDamage * ratio;
        } else if (refAttr < cap + enemyAttr) {
            // LOW ~ MAX：每多出 cap 点属性，伤害再加 MAX_EXTRA 倍
            float ratio = (float) ((refAttr - enemyAttr) / cap) * MAX_EXTRA + 1.0F;
            damage = lowDamage * ratio;
        } else {
            damage = lowDamage * (MAX_EXTRA + 1.0F);
        }
        damage *= target.getRandom().nextFloat() * 0.2F + 0.9F;

        DamageSource source = bullet != null ? weaponSource(bullet, attacker) : attackSource(attacker);
        target.damage(source, damage);
        hbrAttackProcess = false;
    }

    /**
     * 暴击判定（原版 {@code checkCritical}）：{@code chance = (1 - 1/(LUC+1)) * 0.1}。
     *
     * <p>原版注释留了一个 {@code //todo: check buff}，这里保持相同行为（只看幸运属性）。
     */
    public static boolean checkCritical(LivingEntity attacker) {
        float luck = ModAttributes.getFloat(attacker, ModAttributes.LUC);
        float chance = (1.0F - 1.0F / (luck + 1.0F)) * 0.1F;
        return attacker.getRandom().nextFloat() < chance;
    }

    // ---------------------------------------------------------------- 工具 --

    private static List<PlayerEntity> playersNear(World world, PlayerEntity caster, float range) {
        return world.getEntitiesByClass(PlayerEntity.class,
                Box.of(caster.getPos(), range * 2.0D, range * 2.0D, range * 2.0D),
                PlayerEntity::isAlive);
    }

    /** 把数值夹到 [0, 1]，对应原版 {@code CommonFunctions.clamp(v, 0, 1)}。 */
    public static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
