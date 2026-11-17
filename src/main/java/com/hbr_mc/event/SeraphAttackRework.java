package com.hbr_mc.event;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.combat.DpConfig;
import com.hbr_mc.combat.DpService;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.ItemSeraphCannonBase;
import com.hbr_mc.item.seraph.SeraphUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * 玩家普通近战改走六维公式，移植自 1.12.2 的 {@code events/DesignPlayerDamageRework}。
 *
 * <h2>原版逻辑</h2>
 * 在 {@code LivingHurtEvent} 里，如果伤害来源是玩家、玩家主手拿着能用的炽天使，并且
 * <b>目标是玩家 或 目标的 {@code DP_MAX > 0}</b>，就取消这次原版伤害，改为：
 * <ul>
 *   <li>炮类武器（{@code ItemSeraphCannonBase}）：直接不结算（取消后什么都不做）；</li>
 *   <li>其它武器：按 {@code type.hitCount} 分段调用 {@code CombatUtil.attackAsHBR}，
 *       每段威力 = {@code NORMAL_ATK_POWER / hitCount}。</li>
 * </ul>
 *
 * <h2>1.21.1 的对应写法</h2>
 * 1.21.1 没有"出站伤害可取消"的事件，改用 Fabric 的 {@link AttackEntityCallback}
 * （玩家攻击实体之前触发）：返回 {@link ActionResult#FAIL} 会取消原版攻击，
 * 我们自己在取消前把 HBR 伤害打完。
 *
 * <p>取消原版攻击会顺带失去"挥击消耗 + 击退"，所以这里手动补上：
 * {@code addExhaustion(0.1)} 与 {@code takeKnockback(0.4, ...)}，手感与原版接近。
 *
 * <p>开关：{@link DpConfig#PLAYER_MELEE_REWORK}。
 */
public final class SeraphAttackRework {

    private SeraphAttackRework() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register(SeraphAttackRework::onAttack);
    }

    private static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity,
                                         EntityHitResult hitResult) {
        if (world.isClient() || !DpConfig.PLAYER_MELEE_REWORK) {
            return ActionResult.PASS;
        }
        if (!(entity instanceof LivingEntity target)) {
            return ActionResult.PASS;
        }
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof ItemSeraphBase seraph)) {
            return ActionResult.PASS;
        }
        // 原版 canAttackWithMainHandSeraph：主手炽天使且未损坏
        if (!SeraphUtil.canAttackWithMainHandSeraph(player)) {
            return ActionResult.PASS;
        }
        // 原版触发条件：目标是玩家，或者目标有 DP（星癌体的壳）
        boolean dpTarget = target instanceof PlayerEntity || DpService.dpMax(target) > 0.0F;
        if (!dpTarget) {
            return ActionResult.PASS;
        }

        if (seraph instanceof ItemSeraphCannonBase) {
            // 原版对炮类武器也是"取消但不打"（炮的伤害走右键技能，不走近战）
            return ActionResult.FAIL;
        }

        int hits = seraph.getSeraphType() == null ? 1 : Math.max(1, seraph.getSeraphType().hitCount);
        float perHit = CombatConfig.NORMAL_ATK_POWER / (float) hits;
        for (int i = 0; i < hits; i++) {
            CombatUtil.attackAsHBR(player, target, perHit, CombatConfig.NORMAL_ATK_CAP);
        }

        // 补偿被取消掉的挥击消耗与击退
        player.addExhaustion(0.1F);
        double knockbackX = MathHelper.sin(player.getYaw() * 0.017453292F);
        double knockbackZ = -MathHelper.cos(player.getYaw() * 0.017453292F);
        target.takeKnockback(0.4D, knockbackX, knockbackZ);

        return ActionResult.FAIL;
    }
}
