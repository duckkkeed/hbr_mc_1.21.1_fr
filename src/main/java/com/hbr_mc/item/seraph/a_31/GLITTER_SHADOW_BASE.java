package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.EnumSeraphType;
import com.hbr_mc.item.seraph.ItemSeraphBlaster;
import com.hbr_mc.potion.EffectUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * GlitterShadow 系（镰刀）的公共基类，移植自 1.12.2 {@code items/seraph/a31/ItemGlitterShadowBase}。
 *
 * <p>除了"挖得快"（{@link ItemSeraphBlaster}）以外，它还有一个被动：
 * <b>拿在手上</b>（选中）时给持有者速度与急迫，<b>放在物品栏里没拿</b>时给缓慢与挖掘疲劳——
 * 原版用 {@code onUpdate} 每 tick 刷 10 tick 的"环境效果"。
 *
 * <p>1.21.1 对应 {@code Item#inventoryTick}；只给 10 tick 是因为每 tick 都会刷新，
 * 用短时间可以保证"一放下就立刻恢复"。
 */
public class GLITTER_SHADOW_BASE extends ItemSeraphBlaster {

    /** 每次刷新的效果时长（原版 {@code new PotionEffect(..., 10, 0, true, false)}）。 */
    private static final int PASSIVE_DURATION_TICKS = 10;

    public GLITTER_SHADOW_BASE(Settings settings, EnumSeraphRarity rarity) {
        super(settings, rarity, EnumSeraphType.SCYTHE);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient || !(entity instanceof PlayerEntity player)) {
            return;
        }
        if (!selected) {
            EffectUtil.applySilent(player, StatusEffects.SLOWNESS, 0, PASSIVE_DURATION_TICKS);
            EffectUtil.applySilent(player, StatusEffects.MINING_FATIGUE, 0, PASSIVE_DURATION_TICKS);
        } else {
            EffectUtil.applySilent(player, StatusEffects.SPEED, 0, PASSIVE_DURATION_TICKS);
            EffectUtil.applySilent(player, StatusEffects.HASTE, 0, PASSIVE_DURATION_TICKS);
        }
    }
}
