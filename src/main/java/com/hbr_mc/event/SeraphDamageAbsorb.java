package com.hbr_mc.event;

import com.hbr_mc.combat.DpConfig;
import com.hbr_mc.combat.DpService;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * 玩家侧 DP：主手持有 Seraph 物品时，受到的伤害先扣这把武器的耐久（DP），
 * DP 吃满后才轮到血量。
 *
 * <p>移植自 1.12.2 版 {@code ItemSeraphBase#onHit(LivingHurtEvent)}（LOWEST 优先级）：
 *
 * <pre>{@code
 * ItemStack stack = entityLivingBase.getHeldItemMainhand();
 * if (stack.getItem() instanceof ItemSeraphBase) {
 *     float amount = event.getAmount();
 *     event.setAmount(0);                                            // 抵消伤害
 *     stack.setItemDamage((int) (stack.getItemDamage() + amount));    // 转成耐久
 * }
 * }</pre>
 *
 * <h2>1.21.1 的实现方式</h2>
 * 1.21.1 没有 Forge 式可取消的 {@code LivingHurtEvent}，这里用 Fabric 的
 * {@link ServerLivingEntityEvents#ALLOW_DAMAGE}：返回 {@code false} 即取消本次伤害。
 * 它从 {@code LivingEntity#damage} 里触发，位置在<b>无敌 / 客户端 / 死亡判定之后、护甲减伤之前</b>，
 * 所以创造模式玩家、免疫该来源的玩家不会被误扣耐久；被吸收的量也是<b>护甲前</b>的原始伤害，
 * 与 1.12.2 的 {@code LivingHurtEvent} 时机一致。
 *
 * <h2>DP 语义（本工程对原版的修正）</h2>
 * <ul>
 *   <li><b>只吸主手</b>（原版里"主副手都算"的写法是注释掉的）；</li>
 *   <li>伤害按"武器剩余耐久"截断：耐久只够吸一部分时，<b>溢出部分</b>会被排到下一次服务端任务里
 *       重新结算（此时武器已被标记 {@code seraph_broken}，本监听器会放行），所以 DP 破掉后会正常掉血。
 *       原版是"耐久夹到上限 + 继续无限吸收"，属于 bug，见 {@code docs/dp-system.md}；</li>
 *   <li>武器已损坏（{@code seraph_broken}）时直接放行，不再吸收；</li>
 *   <li>默认<b>不过滤</b>伤害来源（摔落/虚空/窒息也会被耐久吃掉），与原版一致；
 *       想过滤可打开 {@link DpConfig#PLAYER_DP_FILTER_ENVIRONMENTAL}。</li>
 * </ul>
 *
 * <h2>与 1.12.2 的差异</h2>
 * <ol>
 *   <li>原版把伤害置 0 后流程仍会继续（仍可能有受击表现）；这里直接取消，因此没有受击动画 / 击退 /
 *       护甲损耗，{@code damage()} 返回 {@code false}（表示"未造成伤害"）；</li>
 *   <li>DP 见底后不再无限吸收（见上）。</li>
 * </ol>
 *
 * <p>实测踩到的三个坑（与 DP 无关，但测这个功能一定会遇到）：进存档 60 tick 无敌、和平难度、
 * 创造模式无敌——三者都会让 {@code damage()} 在进入 {@code LivingEntity#damage} 之前就返回，
 * 于是本监听器根本不触发。详见 {@code docs/seraph-port.md} §1.6。
 */
public final class SeraphDamageAbsorb {

    private SeraphDamageAbsorb() {
    }

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(SeraphDamageAbsorb::allowDamage);
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!DpConfig.PLAYER_DP_ENABLED) {
            return true;
        }
        // 原版只处理玩家
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }
        if (amount <= 0.0F) {
            return true;
        }
        // 可选：过滤环境伤害（原版不过滤）
        if (DpConfig.PLAYER_DP_FILTER_ENVIRONMENTAL && !(source.getAttacker() instanceof LivingEntity)) {
            return true;
        }

        // 原版只取主手物品（副手的 buff 在 1.12.2 里就不生效，故写死主手）
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof ItemSeraphBase)) {
            return true;
        }
        // DP 已空：放行，让伤害正常结算到血量
        if (ItemSeraphBase.isBroken(stack)) {
            return true;
        }

        float absorbed = DpService.damageDp(player, amount);
        if (absorbed <= 0.0F) {
            // 武器没有耐久可扣（或已经被夹到上限）：不拦，正常结算
            return true;
        }

        float overflow = amount - absorbed;
        if (overflow > 1.0E-4F) {
            // 耐久只够吸一部分：把溢出量排到本次事件之后再结算。
            // 此刻武器已被标记损坏，所以下一次进入本方法时会直接放行，不会递归。
            scheduleOverflowDamage(player, source, overflow);
        }
        // 抵消本次伤害（DP 已经吃掉了 absorbed）
        return false;
    }

    /**
     * 延迟结算"DP 吸收不下的那部分伤害"。
     *
     * <p>用 {@code server.execute} 而不是当场递归调 {@code damage}，是为了避免在
     * {@code ALLOW_DAMAGE} 回调里重入 {@code LivingEntity#damage}（原版事件也不允许这么做）。
     */
    private static void scheduleOverflowDamage(ServerPlayerEntity player, DamageSource source, float overflow) {
        ServerWorld world = player.getServerWorld();
        world.getServer().execute(() -> {
            if (!player.isAlive() || player.isRemoved()) {
                return;
            }
            player.damage(source, overflow);
        });
    }
}
