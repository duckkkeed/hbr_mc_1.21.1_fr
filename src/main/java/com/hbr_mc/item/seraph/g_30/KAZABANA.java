package com.hbr_mc.item.seraph.g_30;

import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphCannonBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

/**
 * [A] Kazabana（Hisame Ogasahara）——「仄かに揺らぐファイア」。
 *
 * <p>对应 1.12.2 {@code items/seraph/g30/ItemKazabana}：
 * 2 轮 × 10 发扇形散射（{@code KAZABANA_5SP_AOE}：37.5 / 120 / 5SP，DEX 侧重）。
 *
 * <p>另外保留了原版的一个小被动：当它放在<b>副手</b>、主手拿着任意剑时，
 * 每 tick 重置玩家的攻击冷却（{@code player.resetCooldown()}）。
 */
public class KAZABANA extends ItemSeraphCannonBase {

    public KAZABANA(Settings settings) {
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

        CombatConfig.SkillConf conf = CombatConfig.KAZABANA_5SP_AOE;
        int volley = 2;
        int spread = 10;
        float perAngle = 5.0F;
        float minPotency = conf.minPotency() / volley;
        spreadAttack(world, caster, volley, spread, perAngle, minPotency, conf.cap(),
                CombatUtil.EnumAttrType.DEX_FOCUS);

        caster.swingHand(caster.getActiveHand());
        caster.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                SoundCategory.PLAYERS, 1.0F, 1.5F);
        setCoolDown(caster, conf.cooldownTicks());
    }

    /**
     * 副手被动：主手是剑时持续重置攻击冷却（原版 {@code player.resetCooldown()}，
     * 1.21.1 对应 {@link PlayerEntity#resetLastAttackedTicks()}）。
     */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient || !(entity instanceof PlayerEntity player)) {
            return;
        }
        if (ItemStack.areEqual(player.getOffHandStack(), stack)
                && player.getMainHandStack().getItem() instanceof SwordItem) {
            player.resetLastAttackedTicks();
        }
    }
}
