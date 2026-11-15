package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * [A] Clavis（Karen Asakura）——大剑，单体攻击 + 眩晕。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemClavis}：
 * 两段各 0.5 倍的单体伤害（{@code CLAVIS_A}：45 / 120 / 5SP），
 * 再按 {@code (INT + 2*LUC)/3} 与目标 MEN 的对比算眩晕概率（30% ~ 85%）。
 */
public class CLAVIS extends ItemSeraphBase {

    public CLAVIS(Settings settings) {
        super(settings, EnumSeraphRarity.A, EnumSeraphType.LARGE_SWORD);
    }

    @Override
    public int getMaxSkillSlot(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean castSkillEnemy(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        if (!canUseSkills(player)) {
            return false;
        }

        if (!world.isClient) {
            CombatConfig.SkillConf conf = CombatConfig.CLAVIS_A;
            calcAtkBuffSkill(player);
            // 原版 bonusRate 传 0：这种"标准攻击"不吃打盾加成
            CombatUtil.generalAttack(CombatUtil.EnumAttrType.STANDARD, player, conf.minPotency() * 0.5F,
                    conf.cap(), 0.0F, target);
            CombatUtil.generalAttack(CombatUtil.EnumAttrType.STANDARD, player, conf.minPotency() * 0.5F,
                    conf.cap(), 0.0F, target);

            if (player.getRandom().nextFloat() < stunChance(player, conf.cap())) {
                EffectUtil.applyTicks(target, ModPotion.STUNNED, 0, CombatConfig.STUN_TICK_PER_TURN);
            }

            postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_FALL);
        } else {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
        }
        return true;
    }

    /**
     * 眩晕概率（原版内联公式）：{@code clamp((pwr - 目标MEN)/cap, 0, 1)} 线性映射到 30%~85%。
     *
     * <p>原版这里读的是<b>施术者自己的</b> MEN 当作"敌方门槛"（写法可疑，但照抄），
     * 见 docs/seraph-port.md 的核对记录。
     */
    static float stunChance(PlayerEntity player, float cap) {
        float power = (ModAttributes.getFloat(player, ModAttributes.INT)
                + 2.0F * ModAttributes.getFloat(player, ModAttributes.LUC)) / 3.0F;
        float border = ModAttributes.getFloat(player, ModAttributes.MEN);
        float ratio = CombatUtil.clamp01((power - border) / cap);
        return 0.3F + 0.55F * ratio;
    }
}
