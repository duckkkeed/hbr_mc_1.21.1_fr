package com.hbr_mc.event;

import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 「伤害转耐久」：玩家主手持有 Seraph 物品时，受到的伤害不再扣血，而是转成该物品的耐久消耗。
 *
 * <p>移植自 1.12.2 版 {@code ItemSeraphBase#onHit(LivingHurtEvent)}（LOWEST 优先级）：
 *
 * <pre>{@code
 * ItemStack stack = entityLivingBase.getHeldItemMainhand();
 * if (stack.getItem() instanceof ItemSeraphBase) {
 *     float amount = event.getAmount();
 *     event.setAmount(0);                                   // 取消伤害
 *     stack.setItemDamage((int) (stack.getItemDamage() + amount));   // 转成耐久
 * }
 * }</pre>
 *
 * <p>1.21.1 没有 Forge 式可取消的 {@code LivingHurtEvent}，这里改用 Fabric API 的
 * {@link ServerLivingEntityEvents#ALLOW_DAMAGE}：
 *
 * <ul>
 *   <li>它从 {@code LivingEntity#damage} 里触发，位置在<b>无敌 / 客户端 / 死亡判定之后、护甲减伤之前</b>，
 *       所以创造模式玩家、免疫该伤害来源的玩家不会被误扣耐久；</li>
 *   <li>返回 {@code false} 即完全取消本次伤害（等价于原版的 {@code event.setAmount(0)}）；</li>
 *   <li>被吸收的耐久值 = <b>护甲减伤前的原始伤害</b>，与 1.12.2 的 {@code LivingHurtEvent} 时机一致。</li>
 * </ul>
 *
 * <p><b>与 1.12.2 的两点差异</b>：
 * <ol>
 *   <li>原版把伤害置 0 后流程仍会继续（仍可能有受击表现）；这里直接取消，因此没有受击动画 / 击退 / 护甲损耗；</li>
 *   <li>{@code LivingEntity#damage(...)} 的返回值会变成 {@code false}（表示"未造成伤害"）。</li>
 * </ol>
 *
 * <p><b>与 1.12.2 一致的两点行为</b>：
 * <ul>
 *   <li>只看<b>主手</b>（原版里"主副手都算"的写法是注释掉的）；</li>
 *   <li><b>不检查物品是否已损坏</b>——耐久见底后仍然继续吸收伤害。
 *       若想改成"损坏后不再吸收"，在 {@link #allowDamage} 里加一行
 *       {@code if (ItemSeraphBase.isBroken(stack)) return true;} 即可。</li>
 * </ul>
 */
public final class SeraphDamageAbsorb {

    private SeraphDamageAbsorb() {}

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(SeraphDamageAbsorb::allowDamage);
    }

    //耐久耗尽后仍能吸收伤害大概率是是bug， 但为了和1.12.2一致，暂时不改
    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        // 原版只处理玩家
        if (!(entity instanceof ServerPlayerEntity player)) return true;
        if (amount <= 0.0F) return true;

        // 原版只取主手物品
        // 1.12.2中原本想支持“副手持有 Seraph 也有 buff”，但发现副手 buff 不生效，若强行处理会导致伤害异常巨大，所以最终写死成了“只检查主手”。
        // 此处保留1.12.2的逻辑。
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof ItemSeraphBase)) return true;

//        if (ItemSeraphBase.isBroken(stack)) return true;

        return !absorb(stack, amount);

        // 抵消本次伤害
    }

    /**
     * 把伤害值加到耐久上，对应原版 {@code setItemDamage((int) (getItemDamage() + amount))}。
     *
     * <p>交给 {@link ItemSeraphBase#damageDurability(ItemStack, double)} 处理：
     * 与原版 {@code setDamage} 覆写一致——超过上限时夹到 {@code getMaxDamage()} 并打损坏标记，
     * 物品<b>不会因为耐久耗尽而消失</b>。
     *
     * @return 是否真的吸收了（没有耐久属性的物品不吸收）
     */
    private static boolean absorb(ItemStack stack, float amount) {
        if (stack.getMaxDamage() <= 0) return false;
        ItemSeraphBase.damageDurability(stack, amount);
        return true;
    }
}
