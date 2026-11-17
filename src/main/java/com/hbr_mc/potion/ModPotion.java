package com.hbr_mc.potion;

import com.hbr_mc.HBR_MC;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * 本 mod 的状态效果，对应 1.12.2 的 {@code init/RegisterEffects}。
 *
 * <table>
 *   <caption>效果对照</caption>
 *   <tr><th>1.12.2</th><th>本工程</th><th>作用</th></tr>
 *   <tr><td>{@code SKILL_ATK_UP_LESSER} / {@code _UP} / {@code _GREATER}</td>
 *       <td>{@link #SKILL_ATK_UP_LESSER} / {@link #SKILL_ATK_UP} / {@link #SKILL_ATK_UP_GREATER}</td>
 *       <td>纯标记：技能结算前按层数消耗，每层给技能 +50% / +65% / +90% 威力（见 {@code ItemSeraphBase#calcAtkBuffSkill}）</td></tr>
 *   <tr><td>{@code SELF_RECOIL}</td><td>{@link #SELF_RECOIL}</td><td>反作用力：移速与攻速大幅下降，且期间不能用技能</td></tr>
 *   <tr><td>{@code STUNNED}</td><td>{@link #STUNNED}</td><td>眩晕：移速与攻速归零</td></tr>
 * </table>
 *
 * <h2>实现差异</h2>
 * <ul>
 *   <li>1.12.2 的 {@code SKILL_ATK_UP_*} 是"无害药水 + 等级当层数"，本身没有任何属性修饰；
 *       这里同样不加修饰，只作为层数载体。</li>
 *   <li>SELF_RECOIL / STUNNED 在原版是 {@code registerPotionAttributeModifier(..., -1, 2)}，
 *       其中 {@code operation = 2} 是"乘以 (1 + value)"，即 -100%。
 *       1.21.1 对应 {@link EntityAttributeModifier.Operation#ADD_MULTIPLIED_TOTAL}。</li>
 *   <li><b>颜色必须照抄原版构造参数</b>：1.21.1 的 {@code StatusEffect(category, color)} 会拿这个
 *       RGB 生成状态粒子（{@code ENTITY_EFFECT}），填错就会看到"反作用力粒子是红的"这种偏差。
 *       原版：self_recoil / skill_atk_up_* = {@code 0x6ABECE}，stunned = {@code 0xE0E69E}。</li>
 *   <li>原版的图标来自 {@code textures/misc/potions.png} 的 3 个 18×18 图标（索引 0/1/2）；
 *       1.21.1 要求把图标放到 {@code assets/hbr_mc/textures/mob_effect/<效果id>.png}，
 *       本工程已按原索引裁好放入。</li>
 * </ul>
 */
public final class ModPotion {

    /** 技能攻击力上升（小），对应原版 {@code skill_atk_up_1}。 */
    public static final StatusEffect SKILL_ATK_UP_LESSER = register("skill_atk_up_1",
            new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x6ABECE) {
                @Override
                public boolean canApplyUpdateEffect(int duration, int amplifier) {
                    return false;
                }
            });

    /** 技能攻击力上升（中），对应原版 {@code skill_atk_up_2}。 */
    public static final StatusEffect SKILL_ATK_UP = register("skill_atk_up_2",
            new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x6ABECE) {
                @Override
                public boolean canApplyUpdateEffect(int duration, int amplifier) {
                    return false;
                }
            });

    /** 技能攻击力上升（大），对应原版 {@code skill_atk_up_3}。 */
    public static final StatusEffect SKILL_ATK_UP_GREATER = register("skill_atk_up_3",
            new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x6ABECE) {
                @Override
                public boolean canApplyUpdateEffect(int duration, int amplifier) {
                    return false;
                }
            });

    /**
     * 反作用力：移速、攻速 -100%，并且期间无法使用炽天使技能（{@code canUseSkills}）。
     *
     * <p>颜色取原版构造参数里的 {@code liquidColor = 0x6ABECE}（青绿）：1.21.1 的
     * {@link StatusEffect} 用这个颜色作为<b>状态粒子颜色</b>，所以数值必须和原版一致，
     * 否则身上的粒子会变成别的颜色（例如写成 0xFF6B6B 时是红色）。
     *
     * <p>原版把两个属性修饰都用同一个 UUID {@code f3a462f9-...}；1.21.1 改用 {@link net.minecraft.util.Identifier}，
     * 两个修饰各自需要不同的 id（同一个 id 会互相覆盖）。
     */
    public static final StatusEffect SELF_RECOIL = register("self_recoil", new StatusEffect(StatusEffectCategory.HARMFUL, 0x6ABECE) {
        {
            addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, HBR_MC.id("self_recoil_movement_speed"),
                    -1.0D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, HBR_MC.id("self_recoil_attack_speed"),
                    -1.0D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return false;
        }
    });

    /** 眩晕：移速、攻速 -100%（对应原版 {@code stunned}，1.12.2 里由 {@code EntityAIStunned} 使用）。 */
    public static final StatusEffect STUNNED = register("stunned", new StatusEffect(StatusEffectCategory.HARMFUL, 0xE0E69E) {
        {
            addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, HBR_MC.id("stunned_movement_speed"),
                    -1.0D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, HBR_MC.id("stunned_attack_speed"),
                    -1.0D, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            return false;
        }
    });

    private ModPotion() {
    }

    private static StatusEffect register(String id, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, HBR_MC.id(id), effect);
    }

    public static void registerModPotions() {
        HBR_MC.LOGGER.info("Registering status effects");
    }
}
