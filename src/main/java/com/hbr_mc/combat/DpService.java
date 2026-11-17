package com.hbr_mc.combat;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.entity.EntityCancer;
import com.hbr_mc.entity.projectile.EntityHBRProjectile;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * DP（护盾式耐久条）的统一读写入口，对应 1.12.2 的 {@code init/util/DShieldUtil}。
 *
 * <h2>DP 到底是什么（原版三层结构）</h2>
 * <table>
 *   <caption>DP 的载体</caption>
 *   <tr><th>对象</th><th>DP 当前值</th><th>DP 上限</th></tr>
 *   <tr><td>玩家（主手炽天使）</td>
 *       <td>{@code 武器剩余耐久 + absorptionAmount}</td>
 *       <td>{@code 武器 getMaxDamage()}</td></tr>
 *   <tr><td>玩家（空手/其它）</td><td>{@code absorptionAmount}</td><td>{@code DP_MAX} 属性</td></tr>
 *   <tr><td>星癌体（壳）</td><td>{@code absorptionAmount}</td><td>{@code DP_MAX} 属性（= 初始护盾）</td></tr>
 * </table>
 *
 * <p>原版 {@code DShieldUtil.getRemainDP(player)} 对持有炽天使的玩家返回
 * {@code maxDamage - damage + absorption}，所以"玩家 DP = 武器耐久"不是本移植发明的口径，
 * 而是原版就这么写；伤害转耐久的入口是 {@code ItemSeraphBase#onHit} →
 * 本工程的 {@code SeraphDamageAbsorb}。
 *
 * <h2>与 1.12.2 的差异</h2>
 * <ul>
 *   <li>原版用 {@code DP_LOSS_SYNC} 属性（{@code DP_MAX - absorption}）当"同步通道"给 HUD 读，
 *       且客户端/服务端对"没有武器的玩家"取值不一致（客户端读的是 loss、服务端读的是 absorption）。
 *       本工程把 {@code dp()} / {@code dpMax()} 作为唯一口径，HUD 直接调用它们；
 *       {@link #syncStatus} 仍会写 {@code DP_LOSS_SYNC} 以保持属性含义一致（兼容调试）。</li>
 *   <li>原版"武器耐久耗尽后仍然无限吸收"（{@code setDamage} 夹断后继续抵消伤害）。
 *       本工程在 {@code SeraphDamageAbsorb} 里把"武器已损坏"视为 <b>DP 已空</b>，
 *       溢出伤害会正常结算到血量——这是 DP 语义下的修正，见 {@code docs/dp-system.md}。</li>
 * </ul>
 */
public final class DpService {

    private DpService() {
    }

    // ---------------------------------------------------------------- 读取 --

    /** 该实体的 DP 上限；{@code 0} 表示没有 DP（HUD 不画条）。 */
    public static float dpMax(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        ItemStack weapon = weaponDpStack(entity);
        if (weapon != null && weapon.getMaxDamage() > 0) {
            return weapon.getMaxDamage();
        }
        // 星癌体的壳有专门的 tracked data（吸收值对怪物不同步，见 EntityCancer#getShellDp）
        if (entity instanceof EntityCancer cancer) {
            return cancer.getShellMax();
        }
        float fromAttribute = ModAttributes.getFloat(entity, ModAttributes.DP_MAX);
        if (fromAttribute > 0.0F) {
            return fromAttribute;
        }
        // 兜底：只有吸收值、没有 DP_MAX 的实体（例如被其它 mod 加了吸收值）
        float absorption = entity.getAbsorptionAmount();
        return absorption > 0.0F ? absorption : 0.0F;
    }

    /**
     * 该实体当前的 DP。
     *
     * <p>玩家 = 武器剩余耐久 + absorption（与原版 {@code getRemainDP} 一致）；
     * 其它 = absorption（原版怪物侧的 {@code DP_MAX - DP_LOSS_SYNC}，等价于吸收值）。
     */
    public static float dp(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        }
        ItemStack weapon = weaponDpStack(entity);
        if (weapon != null && weapon.getMaxDamage() > 0) {
            float remaining = Math.max(0, weapon.getMaxDamage() - weapon.getDamage());
            return remaining + Math.max(0.0F, entity.getAbsorptionAmount());
        }
        if (entity instanceof EntityCancer cancer) {
            return Math.min(cancer.getShellDp(), cancer.getShellMax());
        }
        float absorption = Math.max(0.0F, entity.getAbsorptionAmount());
        float max = dpMax(entity);
        return max > 0.0F ? Math.min(absorption, max) : absorption;
    }

    /** DP 是否已空（有上限且当前值为 0）。 */
    public static boolean isDepleted(LivingEntity entity) {
        float max = dpMax(entity);
        return max > 0.0F && dp(entity) <= 0.0F;
    }

    /** 该实体是否以"主手炽天使的耐久"作为 DP（对应原版 {@code getRemainDP} 的玩家分支）。 */
    public static boolean usesWeaponDp(LivingEntity entity) {
        return weaponDpStack(entity) != null;
    }

    // ---------------------------------------------------------------- 扣减 --

    /**
     * 扣 DP，返回<b>实际吸收掉的伤害量</b>（0 ~ amount）。
     *
     * <p>玩家：优先扣武器耐久（调用 {@link ItemSeraphBase#damageDurability}，
     * 因此耐久会夹到上限并打上 {@code seraph_broken} 标记，物品不会消失）；
     * 其它：扣 absorption。
     *
     * <p>注意：这个方法只负责"扣值"，不负责取消伤害。星癌体的壳由原版吸收值结算路径处理，
     * 玩家则由 {@code SeraphDamageAbsorb} 根据返回值决定是否抵消本次伤害。
     */
    public static float damageDp(LivingEntity entity, float amount) {
        if (entity == null || amount <= 0.0F || entity.getWorld().isClient()) {
            return 0.0F;
        }

        ItemStack weapon = weaponDpStack(entity);
        if (weapon != null && weapon.getMaxDamage() > 0) {
            int remaining = Math.max(0, weapon.getMaxDamage() - weapon.getDamage());
            float absorbed = Math.min(remaining, amount);
            if (absorbed > 0.0F) {
                ItemSeraphBase.damageDurability(weapon, absorbed);
                syncStatus(entity);
            }
            return absorbed;
        }

        float absorption = entity.getAbsorptionAmount();
        if (absorption <= 0.0F) {
            return 0.0F;
        }
        float absorbed = Math.min(absorption, amount);
        entity.setAbsorptionAmount(absorption - absorbed);
        if (entity instanceof EntityCancer cancer) {
            // 怪物的吸收值不会自动同步，这里同步写进 tracked 壳量
            cancer.setShellDp(entity.getAbsorptionAmount());
        }
        syncStatus(entity);
        return absorbed;
    }

    // -------------------------------------------------------------- 能否破壳 --

    /**
     * 该伤害来源能否打破 DP/壳，对应原版 {@code CombatUtil.canBreakShield}：
     * <ul>
     *   <li>虚空伤害；</li>
     *   <li>攻击者主手拿着未损坏的炽天使；</li>
     *   <li>本 mod 的弹丸。</li>
     * </ul>
     */
    public static boolean canBreak(DamageSource source) {
        if (source == null) {
            return false;
        }
        if (source.isOf(DamageTypes.OUT_OF_WORLD)) {
            return true;
        }
        if (source.getAttacker() instanceof LivingEntity attacker && SeraphUtil.isUsingSeraph(attacker)) {
            return true;
        }
        return source.getSource() instanceof EntityHBRProjectile;
    }

    // ---------------------------------------------------------------- 同步 --

    /**
     * 把 {@code DP_LOSS_SYNC}（已损失 DP）写回属性，保持与原版属性含义一致。
     *
     * <p>只在服务端调用；属性本身 {@code setTracked(false)}，所以这不会产生网络包，
     * HUD 用 {@link #dp} / {@link #dpMax} 直接取值（玩家耐久与 absorption 本来就会同步）。
     */
    public static void syncStatus(LivingEntity entity) {
        if (entity == null || entity.getWorld().isClient()) {
            return;
        }
        EntityAttributeInstance instance = entity.getAttributeInstance(ModAttributes.DP_LOSS_SYNC);
        if (instance != null) {
            instance.setBaseValue(Math.max(0.0D, dpMax(entity) - dp(entity)));
        }
    }

    /** 若实体主手拿着炽天使，返回那把武器；否则 {@code null}。 */
    private static ItemStack weaponDpStack(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return null;
        }
        ItemStack stack = player.getMainHandStack();
        return stack.getItem() instanceof ItemSeraphBase ? stack : null;
    }
}
