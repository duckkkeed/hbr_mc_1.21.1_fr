package com.hbr_mc.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * 状态效果的读写工具，对应 1.12.2 {@code EntityUtil} 里的
 * {@code ApplyBuff} / {@code getBuffLevelIDL} / {@code TryRemoveGivenBuff}。
 *
 * <h2>两套参数类型</h2>
 * 1.21.1 里状态效果有两种持有形式：
 * <ul>
 *   <li>{@link StatusEffect}：本 mod 注册后的对象本身（{@link ModPotion#SELF_RECOIL}）；</li>
 *   <li>{@link RegistryEntry}{@code <StatusEffect>}：原版常量（{@code StatusEffects.SPEED} 等）与
 *       {@code LivingEntity} 的查询接口要求的形态。</li>
 * </ul>
 * 每个方法都提供两种重载，行为一致；传 {@link StatusEffect} 时内部会查一次注册表。
 *
 * <h2>等级语义（容易踩坑）</h2>
 * 原版这两套编号是错开的：
 * <ul>
 *   <li>{@code ApplyBuff(entity, potion, level, seconds)} 的 {@code level} 是<b>amplifier</b>（0 起），
 *       所以 {@code level = 0} 就是"效果 I"；</li>
 *   <li>{@code getBuffLevelIDL} 返回的是 {@code amplifier + 1}（1 起），
 *       所以"没有该效果"时返回 0，有"效果 I"时返回 1。</li>
 * </ul>
 * 本类把两者分别命名为 {@code amplifier} 与 {@code level}（= amplifier + 1），调用点按原版语义一一对应。
 */
public final class EffectUtil {

    private EffectUtil() {
    }

    // ------------------------------------------------------------ 查询/移除 --

    /**
     * 读取效果等级（1 起），对应原版 {@code EntityUtil.getBuffLevelIDL}：
     * 没有该效果、或剩余时间为 0 时返回 0。
     */
    public static int level(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        if (entity == null || effect == null) {
            return 0;
        }
        StatusEffectInstance instance = entity.getStatusEffect(effect);
        if (instance == null || instance.getDuration() <= 0) {
            return 0;
        }
        return instance.getAmplifier() + 1;
    }

    public static int level(LivingEntity entity, StatusEffect effect) {
        return level(entity, entry(effect));
    }

    /** 是否有该效果（不看等级），对应原版 {@code canUseSkills} 用的"身上有没有 SELF_RECOIL"。 */
    public static boolean has(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        return entity != null && effect != null && entity.hasStatusEffect(effect);
    }

    public static boolean has(LivingEntity entity, StatusEffect effect) {
        return has(entity, entry(effect));
    }

    /** 移除效果，对应原版 {@code EntityUtil.TryRemoveGivenBuff}。 */
    public static void remove(LivingEntity entity, RegistryEntry<StatusEffect> effect) {
        if (entity != null && effect != null) {
            entity.removeStatusEffect(effect);
        }
    }

    public static void remove(LivingEntity entity, StatusEffect effect) {
        remove(entity, entry(effect));
    }

    // ---------------------------------------------------------------- 施加 --

    /**
     * 施加状态效果，对应原版 {@code EntityUtil.ApplyBuff(entity, potion, level, seconds)}。
     *
     * @param amplifier 效果等级（0 起；0 = 效果 I）
     * @param seconds   持续秒数（原版把 {@code BUFF_TIME} 当秒用，内部再乘 20）
     */
    public static boolean apply(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier, float seconds) {
        return applyTicks(entity, effect, amplifier, (int) (seconds * 20.0F));
    }

    public static boolean apply(LivingEntity entity, StatusEffect effect, int amplifier, float seconds) {
        return apply(entity, entry(effect), amplifier, seconds);
    }

    /** 同 {@link #apply}，但持续时间直接给 tick（原版 {@code PotionEffect(potion, ticks, amp)} 的写法）。 */
    public static boolean applyTicks(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier, int ticks) {
        if (entity == null || effect == null || amplifier < 0) {
            return false;
        }
        return entity.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier));
    }

    public static boolean applyTicks(LivingEntity entity, StatusEffect effect, int amplifier, int ticks) {
        return applyTicks(entity, entry(effect), amplifier, ticks);
    }

    /**
     * 施加"环境效果"：不显示粒子、不显示图标（对应原版
     * {@code new PotionEffect(potion, ticks, amp, true, false)}，GlitterShadow 手持时用它刷 buff）。
     */
    public static boolean applySilent(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier, int ticks) {
        if (entity == null || effect == null || amplifier < 0) {
            return false;
        }
        return entity.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
    }

    public static boolean applySilent(LivingEntity entity, StatusEffect effect, int amplifier, int ticks) {
        return applySilent(entity, entry(effect), amplifier, ticks);
    }

    /** 把本 mod 注册的效果对象换成注册表条目；未注册时返回 {@code null}。 */
    private static RegistryEntry<StatusEffect> entry(StatusEffect effect) {
        return effect == null ? null : Registries.STATUS_EFFECT.getEntry(effect);
    }
}
