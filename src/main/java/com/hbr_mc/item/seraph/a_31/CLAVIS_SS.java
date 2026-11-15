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

import java.util.List;

/**
 * [SS] Clavis（Karen Asakura）——「Ikki Burst Strike」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemClavisSS}：
 * <ul>
 *   <li>普通释放 / 对敌方右键（非潜行）→ 范围伤害 + 眩晕（{@code CLAVIS_SS_10SP_STUN}）；</li>
 *   <li>对敌方右键且<b>潜行</b> → 单体终极技（{@code CLAVIS_SS_14SP_ULTI}），额外 50%+50% 的眩晕概率。</li>
 * </ul>
 */
public class CLAVIS_SS extends ItemSeraphBase {

    public CLAVIS_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.LARGE_SWORD);
    }

    @Override
    public int getMaxSkillSlot(ItemStack stack) {
        return 2;
    }

    @Override
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity player) {
        if (!canUseSkills(player) || world.isClient) {
            return;
        }
        areaCast(world, player);
    }

    @Override
    public boolean castSkillEnemy(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        if (!canUseSkills(player)) {
            return false;
        }
        if (world.isClient) {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
            return true;
        }

        if (player.isSneaking()) {
            if (!canUseSkill(stack, SLOT_ULTI)) {
                return false;
            }
            CombatConfig.SkillConf conf = CombatConfig.CLAVIS_SS_14SP_ULTI;
            calcAtkBuffSkill(player);
            CombatUtil.generalAttack(CombatUtil.EnumAttrType.STANDARD, player, conf.minPotency(), conf.cap(),
                    0.0F, target);
            if (player.getRandom().nextFloat() < debuffChance(player, conf.cap(), 0.5F, 0.5F)) {
                EffectUtil.applyTicks(target, ModPotion.STUNNED, 0, CombatConfig.STUN_TICK_PER_TURN);
            }
            skillUseMark(stack, SLOT_ULTI);
            postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_FALL);
        } else {
            areaCast(world, player);
        }
        return true;
    }

    /** 范围伤害 + 概率眩晕（原版 {@code castSkillNonSneak} 与 {@code castSkillEnemy} 的公共部分）。 */
    private void areaCast(World world, PlayerEntity player) {
        CombatConfig.SkillConf conf = CombatConfig.CLAVIS_SS_10SP_STUN;
        calcAtkBuffSkill(player);
        List<LivingEntity> targets = CombatUtil.areaAttack(world, player,
                CombatConfig.AOE_DISTANCE, CombatConfig.AOE_RADIUS, CombatUtil.EnumAttrType.STANDARD,
                conf.minPotency(), conf.cap(), 0.0F);
        for (LivingEntity target : targets) {
            if (player.getRandom().nextFloat() < debuffChance(player, conf.cap(), 0.3F, 0.55F)) {
                EffectUtil.applyTicks(target, ModPotion.STUNNED, 0, CombatConfig.STUN_TICK_PER_TURN);
            }
        }
        postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_FALL);
    }

    /**
     * 眩晕概率（原版 {@code getDebuffChance}）：{@code x + x1 * clamp((pwr - MEN)/cap, 0, 1)}，
     * 其中 {@code pwr = (INT + 2*LUC)/3}。与 {@link CLAVIS} 一样，原版拿施术者自己的 MEN 当门槛。
     */
    private static float debuffChance(PlayerEntity player, float cap, float base, float growth) {
        float power = (ModAttributes.getFloat(player, ModAttributes.INT)
                + 2.0F * ModAttributes.getFloat(player, ModAttributes.LUC)) / 3.0F;
        float border = ModAttributes.getFloat(player, ModAttributes.MEN);
        float ratio = CombatUtil.clamp01((power - border) / cap);
        return base + growth * ratio;
    }

    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        if (slot == SLOT_ULTI) {
            CombatConfig.SkillConf conf = CombatConfig.CLAVIS_SS_14SP_ULTI;
            return conf.uses() + Math.max(0, Math.min(getSkillLevel(stack, slot) - 1, conf.usesGrowth()));
        }
        return -1;
    }
}
