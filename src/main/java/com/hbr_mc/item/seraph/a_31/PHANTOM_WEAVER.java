package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * [A] PhantomWeaver（Tama Kunimi）——「Truth or Lies」，单手剑，治疗型。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemPhantomWeaver}：
 * 非潜行使用 / 对友方右键 → 群体修复（{@code PHANTOM_WEAVER_HEAL_A}：305 / 200，5 次，每级 +5）。
 */
public class PHANTOM_WEAVER extends ItemSeraphBase {

    private static final int HEAL_INDEX = 0;

    public PHANTOM_WEAVER(Settings settings) {
        super(settings, EnumSeraphRarity.A, EnumSeraphType.SWORD);
    }

    @Override
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        if (!canUseSkill(stack, HEAL_INDEX) || !canUseSkills(caster)) {
            return;
        }

        CombatConfig.SkillConf conf = CombatConfig.PHANTOM_WEAVER_HEAL_A;
        CombatUtil.areaHeal(world, caster, conf.minPotency(), conf.cap());
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK,
                SoundCategory.PLAYERS, 1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
        skillUseMark(stack, HEAL_INDEX);
    }

    @Override
    public boolean castSkillFriend(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        castSkillNonSneak(stack, world, player);
        return true;
    }

    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        if (slot == HEAL_INDEX) {
            CombatConfig.SkillConf conf = CombatConfig.PHANTOM_WEAVER_HEAL_A;
            return conf.uses() + Math.max(0, Math.min(getSkillLevel(stack, slot) - 1, conf.usesGrowth()));
        }
        return -1;
    }
}
