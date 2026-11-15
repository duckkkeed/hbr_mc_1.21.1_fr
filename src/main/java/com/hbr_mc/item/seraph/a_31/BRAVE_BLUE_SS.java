package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * [SS] BraveBlue（Ruka Kayamori）——「电路的爆裂闪光」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemBraveBlueSS}：
 * <ul>
 *   <li>潜行右键 / 对友方右键 → 群体修复（{@code BRAVE_BLUE_HEAL}，按 INT 计算回复量）；</li>
 *   <li>对敌方右键 → 8 段终极技（{@code BRAVE_BLUE_ULTI}，各段比例见原版数组）。</li>
 * </ul>
 *
 * <p>次数：修复槽 0 = {@code 5 + clamp(等级-1, 0, 5)}，终极槽 1 = {@code 2 + clamp(等级-1, 0, 2)}。
 */
public class BRAVE_BLUE_SS extends ItemSeraphBase {

    private static final int HEAL_INDEX = 0;

    public BRAVE_BLUE_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.DOUBLE_SWORD);
    }

    /** 修复：清除范围内玩家手持炽天使的耐久损伤（原版 {@code castSkillSneak}）。 */
    @Override
    public void castSkillSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        if (!canUseSkill(stack, HEAL_INDEX) || !canUseSkills(caster)) {
            return;
        }

        CombatConfig.SkillConf conf = CombatConfig.BRAVE_BLUE_HEAL;
        CombatUtil.areaHeal(world, caster, conf.minPotency(), conf.cap());
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK,
                net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
        skillUseMark(stack, HEAL_INDEX);
    }

    /** 对友方右键 = 原地放修复（原版 {@code castSkillFriend}）。 */
    @Override
    public boolean castSkillFriend(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        castSkillSneak(stack, world, player);
        return true;
    }

    @Override
    public boolean castSkillEnemy(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        if (!canUseSkill(stack, SLOT_ULTI) || !canUseSkills(player)) {
            return false;
        }

        if (!world.isClient) {
            CombatConfig.SkillConf conf = CombatConfig.BRAVE_BLUE_ULTI;
            float bonusRate = CombatConfig.BONUS_DAMAGE_RATE;
            calcAtkBuffSkill(player);
            CombatUtil.hpAttackGroup(player, target, conf.minPotency(),
                    new float[]{0.15F, 0.05F, 0.05F, 0.05F, 0.05F, 0.05F, 0.05F, 0.5F},
                    conf.cap(), bonusRate);
            postCastSkill(player, world, conf, SoundEvents.ENTITY_GENERIC_EXPLODE.value());
            skillUseMark(stack, SLOT_ULTI);
        } else {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
        }
        return true;
    }

    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        return switch (slot) {
            case HEAL_INDEX -> 5 + clamp(getSkillLevel(stack, slot) - 1, 0, 5);
            case SLOT_ULTI -> 2 + clamp(getSkillLevel(stack, slot) - 1, 0, 2);
            default -> -1;
        };
    }

    /** 原版在这里 {@code throw}；本移植统一返回 -1，避免越界槽位让游戏崩溃。 */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
