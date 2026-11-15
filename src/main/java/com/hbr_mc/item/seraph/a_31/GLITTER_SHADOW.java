package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * [A] GlitterShadow——镰刀，对敌方右键打出 4 段「STR 对 END」伤害。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemGlitterShadowA}：{@code GLITTER_SHADOW_A}
 * （450 / 120 / 5SP），四段各 0.25 倍（{@code CombatUtil.AttackGroup}，没有打盾加成）。
 */
public class GLITTER_SHADOW extends GLITTER_SHADOW_BASE {

    public GLITTER_SHADOW(Settings settings) {
        super(settings, EnumSeraphRarity.A);
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
            CombatConfig.SkillConf conf = CombatConfig.GLITTER_SHADOW_A;
            calcAtkBuffSkill(player);
            CombatUtil.attackGroup(player, target, conf.minPotency(),
                    new float[]{0.25F, 0.25F, 0.25F, 0.25F}, conf.cap());
            postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_USE);
        } else {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
        }
        return true;
    }
}
