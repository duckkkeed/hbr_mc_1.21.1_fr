package com.hbr_mc.block;

import com.hbr_mc.item.seraph.SeraphUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 治疗补给箱，对应 1.12.2 的 {@code blocks/BlockHealStash}。
 *
 * <p>右键（服务端）依次做四件事：
 * <ol>
 *   <li>在方块位置播放"玩家升级"音效（{@code ENTITY_PLAYER_LEVELUP}，{@code BLOCKS} 频道）；</li>
 *   <li>给玩家回 {@code healAmount} 点血；</li>
 *   <li>若主手/副手拿着炽天使：满修 + 清空技能使用次数（{@code recoverSeraphSkill}），
 *       并给该物品 200 tick 冷却；</li>
 *   <li>一次性版本用完后方块消失。</li>
 * </ol>
 *
 * <p>两个变体对应原版 {@code RegisterBlocks#HEAL_STASH} / {@code #HEAL_STASH_PERMA}：
 * <ul>
 *   <li>{@code heal_stash}：硬度 10、回 5 点、一次性；方块本身不掉落（原版 {@code getItemDropped = AIR}
 *       且不可精准采集）；</li>
 *   <li>{@code heal_stash_perma}：不可破坏、回 99 点、可反复使用。</li>
 * </ul>
 *
 * <p><b>与 1.12.2 的差异</b>：原版 {@code DesignBanHeal} 有个 {@code BAN_HEAL} 总开关（默认开启），
 * 会取消玩家的一切治疗，补给箱靠临时放行来绕过；本工程暂未移植该开关，
 * 所以补给箱目前只是"额外治疗"，并没有"禁止其它治疗"的效果。
 */
public class HealStashBlock extends Block {

    /** 回血量，对应原版构造参数 {@code healAmount}。 */
    private final float healAmount;

    /** 是否一次性（用后移除方块），对应原版 {@code setOneUse(boolean)}。 */
    private final boolean oneUse;

    public HealStashBlock(AbstractBlock.Settings settings, float healAmount, boolean oneUse) {
        super(settings);
        this.healAmount = healAmount;
        this.oneUse = oneUse;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.heal(this.healAmount);

        ItemStack stack = SeraphUtil.getFirstSeraphInHand(player);
        if (!stack.isEmpty()) {
            // 原版依次调用 repairSeraphFully + recoverSeraphSkill；后者已经包含"满修 + 次数清零"
            SeraphUtil.recoverSeraphSkill(stack);
            player.getItemCooldownManager().set(stack.getItem(), 200);
        }

        if (this.oneUse) {
            world.removeBlock(pos, false);
        }
        return ActionResult.SUCCESS;
    }
}
