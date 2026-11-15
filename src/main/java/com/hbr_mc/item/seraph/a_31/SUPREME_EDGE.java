package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphCannonBase;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * [A] SupremeEdge（Tsukasa Tojo）——「Serious or Stupid」。
 *
 * <p>对应 1.12.2 {@code items/seraph/a31/ItemSupremeEdge}：给 16 格内的玩家概率上"技能攻击力上升"
 * （{@code SUPREME_EDGE_6SP_BUFF}，成功率 = {@code MIN_POTENCY = 50/65 ≈ 0.77}）。
 *
 * <p>原版这里有一处<b>明显笔误</b>：{@code chance += (1-chance) * Math.min(0, INT/cap)}——
 * {@code Math.min(0, ...)} 恒不为正，所以 INT 越高成功率反而越低（且结果永远 ≤ 0.77）。
 * 本移植<b>照抄</b>该行为并在提示框/文档中标注，免得有人以为是移植错误。
 */
public class SUPREME_EDGE extends ItemSeraphCannonBase {

    private static final float BUFF_RANGE = 16.0F;

    public SUPREME_EDGE(Settings settings) {
        super(settings, EnumSeraphRarity.A, EnumSeraphType.GUN);
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
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.5F, 0.4F / (caster.getRandom().nextFloat() * 0.4F + 0.8F));

        CombatConfig.SkillConf conf = CombatConfig.SUPREME_EDGE_6SP_BUFF;
        float chance = conf.minPotency();
        // 原版笔误照抄：Math.min(0, ...) 只会让成功率变低
        chance += (1.0F - chance) * Math.min(0.0F, (ModAttributes.getFloat(caster, ModAttributes.INT) / conf.cap()));

        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class,
                Box.of(caster.getPos(), BUFF_RANGE * 2.0D, BUFF_RANGE * 2.0D, BUFF_RANGE * 2.0D),
                PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            if (player.getRandom().nextFloat() <= chance) {
                int level = EffectUtil.level(player, ModPotion.SKILL_ATK_UP);
                EffectUtil.apply(player, ModPotion.SKILL_ATK_UP, level, CombatConfig.BUFF_TIME_SECONDS);
            }
        }

        caster.swingHand(caster.getActiveHand());
        caster.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS,
                1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
    }
}
