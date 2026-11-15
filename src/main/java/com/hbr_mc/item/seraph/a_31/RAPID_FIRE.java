package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphCannonBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

/**
 * [A] RapidFire（Yuki Izumi）——「Attack or March」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemRapidFire}：潜行/非潜行都打出
 * 「3 轮 × 10 发、每发间隔 5°」的扇形散射（{@code RAPID_FIRE_4SP_AOE}，DEX 侧重）。
 */
public class RAPID_FIRE extends ItemSeraphCannonBase {

    public RAPID_FIRE(Settings settings) {
        super(settings, EnumSeraphRarity.A, EnumSeraphType.CANNON);
    }

    @Override
    public int getMaxSkillSlot(ItemStack stack) {
        return 1;
    }

    @Override
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity player) {
        castSkillSneak(stack, world, player);
    }

    @Override
    public void castSkillSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        // 原版这里对 canUseSkill 的检查是注释掉的：RapidFire A 的散射没有次数限制
        CombatConfig.SkillConf conf = CombatConfig.RAPID_FIRE_4SP_AOE;
        int volley = 3;
        int spread = 10;
        float perAngle = 5.0F;
        float minPotency = conf.minPotency() / volley;
        calcAtkBuffSkill(caster);
        spreadAttack(world, caster, volley, spread, perAngle, minPotency, conf.cap(),
                CombatUtil.EnumAttrType.DEX_FOCUS);

        caster.swingHand(caster.getActiveHand());
        caster.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
    }
}
