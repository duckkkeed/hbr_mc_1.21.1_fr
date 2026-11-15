package com.hbr_mc.item.seraph.b_31;

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
 * [A] FatalNull（Seika Higuchi）——31B 组的炮。
 *
 * <p>对应 1.12.2 {@code items/seraph/b31/ItemFatalNull}，原版注释写明
 * 「copied from ItemSupremeEdge」：效果与 {@code SUPREME_EDGE} 完全相同——
 * 给 16 格内玩家概率上"技能攻击力上升"，成功率同样带着 {@code Math.min(0, ...)} 的笔误。
 *
 * <p>为了与原版一致，这里也照抄该公式（包括那次笔误），而不是"顺手修正"。
 */
public class FATAL_NULL extends ItemSeraphCannonBase {

    private static final float BUFF_RANGE = 16.0F;

    public FATAL_NULL(Settings settings) {
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
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.5F, 0.4F / (caster.getRandom().nextFloat() * 0.4F + 0.8F));

        CombatConfig.SkillConf conf = CombatConfig.SUPREME_EDGE_6SP_BUFF;
        float chance = conf.minPotency();
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
