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
 * [A] BraveBlue（Ruka Kayamori）——「Attack or Music」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemBraveBlue}：对实体右键打一次 2 段近战。
 * 数值：{@code BRAVE_BLUE_A}（58.2 / 123 / 6SP），两段各 0.5 倍。
 */
public class BRAVE_BLUE extends ItemSeraphBase {

    public BRAVE_BLUE(Settings settings) {
        super(settings, EnumSeraphRarity.A, EnumSeraphType.DOUBLE_SWORD);
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
            CombatConfig.SkillConf conf = CombatConfig.BRAVE_BLUE_A;
            float bonusRate = CombatConfig.BONUS_DAMAGE_RATE;
            calcAtkBuffSkill(player);
            CombatUtil.hpAttackGroup(player, target, conf.minPotency(), new float[]{0.5F, 0.5F},
                    conf.cap(), bonusRate);
            postCastSkill(player, world, conf, SoundEvents.BLOCK_ANVIL_USE);
        } else {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
        }
        return true;
    }
}
