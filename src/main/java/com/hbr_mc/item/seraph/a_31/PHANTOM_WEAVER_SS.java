package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.attribute.ModAttributes;
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
 * [SS] PhantomWeaver——「気合一閃エンジェルセイラー」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemPhantomWeaverSS}：
 * <ul>
 *   <li>非潜行 / 对敌方右键 → 3 段范围攻击（{@code PHANTOM_WEAVER_7SP_AOE}），
 *       并按 LUC 有 30%~60% 概率让冷却少 3 秒；</li>
 *   <li>潜行使用 → 群体复活 + 治疗（{@code PHANTOM_WEAVER_13SP_REVIVAL}）。</li>
 * </ul>
 *
 * <p><b>原版的一处笔误照抄</b>：终极技的次数用的是
 * {@code ModConfig.COMBAT.RAPID_FIRE_11SP_AOE}（RapidFire 的配置）而不是 PhantomWeaver 自己的，
 * 两者的 {@code uses=2 / usesGrowth=2} 恰好相同，所以数值上没区别；这里保留原写法并注明。
 */
public class PHANTOM_WEAVER_SS extends ItemSeraphBase {

    public PHANTOM_WEAVER_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.SWORD);
    }

    @Override
    public boolean castSkillEnemy(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        castSkillNonSneak(stack, world, player);
        return true;
    }

    @Override
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        CombatConfig.SkillConf conf = CombatConfig.PHANTOM_WEAVER_7SP_AOE;
        calcAtkBuffSkill(caster);
        int hitCount = 3;
        for (int i = 0; i < hitCount; i++) {
            CombatUtil.areaAttack(world, caster, CombatConfig.AOE_DISTANCE, CombatConfig.AOE_RADIUS,
                    CombatUtil.EnumAttrType.STANDARD, conf.minPotency() / hitCount, conf.cap(), 1.0F);
        }
        postCastSkill(caster, world, conf, SoundEvents.BLOCK_ANVIL_USE);

        // 幸运越高，冷却越可能 -3 秒（原版 chance = 0.3 + 0.3 * clamp(LUC/cap, 0, 1)）
        float ratio = CombatUtil.clamp01(ModAttributes.getFloat(caster, ModAttributes.LUC) / conf.cap());
        int sp = conf.sp();
        if (caster.getRandom().nextFloat() < (0.3F + 0.3F * ratio)) {
            sp -= 3;
        }
        setCoolDown(caster, sp * 20);
    }

    @Override
    public void castSkillSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        if (!canUseSkill(stack, SLOT_ULTI)) {
            return;
        }
        CombatConfig.SkillConf conf = CombatConfig.PHANTOM_WEAVER_13SP_REVIVAL;
        CombatUtil.areaRevive(world, caster);
        CombatUtil.areaHeal(world, caster, conf.minPotency(), conf.cap());
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK,
                SoundCategory.PLAYERS, 1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
        skillUseMark(stack, SLOT_ULTI);
    }

    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        if (slot == SLOT_ULTI) {
            // 原版笔误：这里读的是 RAPID_FIRE_11SP_AOE（数值恰好相同），照抄
            CombatConfig.SkillConf conf = CombatConfig.RAPID_FIRE_11SP_AOE;
            return conf.uses() + Math.max(0, Math.min(getSkillLevel(stack, slot) - 1, conf.usesGrowth()));
        }
        return -1;
    }
}
