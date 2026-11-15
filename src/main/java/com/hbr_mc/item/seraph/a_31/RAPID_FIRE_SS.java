package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphCannonBase;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.List;

/**
 * [SS] RapidFire（Yuki Izumi）——「终焉 Spitfire（終いのSpitfire）」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemRapidFireSS}：
 * <ul>
 *   <li>潜行/非潜行右键键（{@code castSkillNonSneak}）→ 3 段连射（{@code RAPID_FIRE_7SP_ST}）；</li>
 *   <li>对敌方右键且<b>潜行</b> → 终极技「流星」（{@code RAPID_FIRE_11SP_AOE}）：
 *       范围内全部目标吃伤害，并在每个目标头顶落一道<b>无伤害的</b>闪电；
 *       施术者随后获得「反作用力」（{@link ModPotion#SELF_RECOIL}，2 秒内不能用技能）。</li>
 * </ul>
 */
public class RAPID_FIRE_SS extends ItemSeraphCannonBase {

    public RAPID_FIRE_SS(Settings settings) {
        super(settings, EnumSeraphRarity.SS, EnumSeraphType.CANNON);
    }

    @Override
    public int getMaxSkillSlot(ItemStack stack) {
        return 2;
    }

    @Override
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        CombatConfig.SkillConf conf = CombatConfig.RAPID_FIRE_7SP_ST;
        calcAtkBuffSkill(caster);
        volleyAttack(world, caster, 3, conf);
        postCastSkill(caster, world, conf, SoundEvents.ENTITY_BLAZE_SHOOT);
    }

    @Override
    public void castSkillSneak(ItemStack stack, World world, PlayerEntity caster) {
        if (world.isClient) {
            return;
        }
        if (!canUseSkill(stack, SLOT_ULTI)) {
            return;
        }

        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.5F, 0.4F / (caster.getRandom().nextFloat() * 0.4F + 0.8F));

        CombatConfig.SkillConf conf = CombatConfig.RAPID_FIRE_11SP_AOE;
        calcAtkBuffSkill(caster);
        List<LivingEntity> targets = CombatUtil.areaAttack(world, caster,
                CombatConfig.AOE_DISTANCE, CombatConfig.AOE_RADIUS, CombatUtil.EnumAttrType.DEX_FOCUS,
                conf.minPotency(), conf.cap(), CombatConfig.BONUS_DAMAGE_RATE);

        for (LivingEntity target : targets) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(target.getPos());
                // 原版 new EntityLightningBolt(world, x, y, z, true)：只做特效、不造成伤害
                lightning.setCosmetic(true);
                world.spawnEntity(lightning);
            }
        }

        EffectUtil.apply(caster, ModPotion.SELF_RECOIL, 0, 2.0F);
        skillUseMark(stack, SLOT_ULTI);
        postCastSkill(caster, world, conf, SoundEvents.ENTITY_GENERIC_EXPLODE.value());
    }

    /**
     * 只有终极技槽有位次数上限（{@code uses=2}，每级 +2）；
     * 主攻击没有次数限制，返回 -1 表示"不限制槽位"。
     */
    @Override
    public int getSkillLimit(ItemStack stack, int slot) {
        if (slot == SLOT_ULTI) {
            CombatConfig.SkillConf conf = CombatConfig.RAPID_FIRE_11SP_AOE;
            return conf.uses() + Math.max(0, Math.min(getSkillLevel(stack, slot) - 1, conf.usesGrowth()));
        }
        return -1;
    }
}
