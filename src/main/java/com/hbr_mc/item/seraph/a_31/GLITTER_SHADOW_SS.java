package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * [SS] GlitterShadow——「I will crush you like a bug!」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemGlitterShadowSS}：
 * <ul>
 *   <li>对敌方右键（非潜行）→ 3 段范围攻击（{@code GLITTER_SHADOW_SS}：591 / 126 / 7SP，各 0.33）；</li>
 *   <li>对敌方右键且<b>潜行</b> → 10 段终极技（{@code GLITTER_SHADOW_SS_ULTI}：2178 / 144 / 11SP），
 *       之后获得反作用力（2 秒内不能用技能）。</li>
 * </ul>
 */
public class GLITTER_SHADOW_SS extends GLITTER_SHADOW_BASE {

    public GLITTER_SHADOW_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS);
    }

    @Override
    public int getMaxSkillSlot(ItemStack stack) {
        return 2;
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
            CombatConfig.SkillConf conf = CombatConfig.GLITTER_SHADOW_SS_ULTI;
            calcAtkBuffSkill(player);
            CombatUtil.attackGroup(player, target, conf.minPotency(),
                    new float[]{0.1F, 0.1F, 0.1F, 0.1F, 0.1F, 0.1F, 0.1F, 0.1F, 0.1F, 0.1F}, conf.cap());
            postCastSkill(player, world, conf, SoundEvents.BLOCK_GLASS_BREAK);
            EffectUtil.apply(player, ModPotion.SELF_RECOIL, 0, 2.0F);
            skillUseMark(stack, SLOT_ULTI);
        } else {
            CombatConfig.SkillConf conf = CombatConfig.GLITTER_SHADOW_SS;
            calcAtkBuffSkill(player);
            CombatUtil.areaAttackGroup(new float[]{0.33F, 0.33F, 0.33F}, world, player,
                    CombatConfig.AOE_DISTANCE, CombatConfig.AOE_RADIUS,
                    CombatUtil.EnumAttrType.STANDARD, conf.minPotency(), conf.cap(), 0.0F);
            postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_USE);
        }
        return true;
    }

    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        if (slot == SLOT_ULTI) {
            CombatConfig.SkillConf conf = CombatConfig.GLITTER_SHADOW_SS_ULTI;
            return conf.uses() + Math.max(0, Math.min(getSkillLevel(stack, slot) - 1, conf.usesGrowth()));
        }
        return -1;
    }
}
