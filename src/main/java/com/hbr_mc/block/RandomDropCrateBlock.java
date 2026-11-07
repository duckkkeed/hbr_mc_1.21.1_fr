package com.hbr_mc.block;

import com.hbr_mc.item.ModItems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 随机补给箱，对应 1.12.2 的 {@code blocks/BlockCrate}（注册名 {@code random_1}）。
 *
 * <p>行为：
 * <ul>
 *   <li>右键（距离方块中心 5 格内）：掉一份随机补给并移除方块，音效为木头的破坏音、音调 0.5；</li>
 *   <li>打碎：同样掉随机补给，且受时运影响——时运 &gt; 0 时按 {@code rand(10) < 时运} 再掉一份；</li>
 *   <li>右键时把玩家幸运值（{@code PlayerEntity#getLuck}）当作原版的 fortune 传入，和 1.12.2 一致。</li>
 * </ul>
 *
 * <p>掉落权重照搬 1.12.2 的 {@code RegisterDrop#initCrateList}：
 * 战斗报告书 I（权重 10，1–5 个）、战斗报告书 II（10，1–2）、阳石水滴（10，1–5）、碧琥珀（5，1）。
 *
 * <p>世界生成按需求暂不移植。
 */
public class RandomDropCrateBlock extends Block {

    /** 权重总和：10 + 10 + 10 + 5。 */
    private static final int TOTAL_WEIGHT = 35;

    private static List<Drop> drops;

    public RandomDropCrateBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    private static List<Drop> drops() {
        if (drops == null) {
            drops = List.of(
                    new Drop(10, ModItems.XP_ITEM_1, 1, 5),
                    new Drop(10, ModItems.XP_ITEM_2, 1, 2),
                    new Drop(10, ModItems.GEM_DROPLET, 1, 5),
                    new Drop(5, ModItems.GEM_AMBER, 1, 1)
            );
        }
        return drops;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        // 1.12.2 用 getDistanceSq(pos) < 25 判定（约 5 格），太远则退回默认交互。
        if (player.squaredDistanceTo(Vec3d.ofCenter(pos)) >= 25.0) {
            return ActionResult.PASS;
        }
        Random random = world.getRandom();
        dropContent(world, pos, (int) player.getLuck(), random);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1.0F, 0.5F);
        return ActionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        Random random = builder.getWorld().getRandom();
        List<ItemStack> result = new ArrayList<>();
        result.add(pick(random));

        int fortune = fortuneOf(builder);
        if (fortune > 0 && random.nextInt(10) < fortune) {
            result.add(pick(random));
        }
        return result;
    }

    /** 右键时直接在世界里掉一份（原版 {@code dropBlockAsItem(world, pos, state, (int) player.getLuck())}）。 */
    private static void dropContent(World world, BlockPos pos, int fortune, Random random) {
        Block.dropStack(world, pos, pick(random));
        if (fortune > 0 && random.nextInt(10) < fortune) {
            Block.dropStack(world, pos, pick(random));
        }
    }

    /** 按权重抽一份补给；count 在 [min, max] 内均匀取（对应 1.12.2 的 {@code ChanceTuple#getStack}）。 */
    private static ItemStack pick(Random random) {
        int weight = random.nextInt(TOTAL_WEIGHT);
        for (Drop drop : drops()) {
            weight -= drop.weight;
            if (weight < 0) {
                int count = drop.min + random.nextInt(drop.max - drop.min + 1);
                return new ItemStack(drop.item, count);
            }
        }
        return ItemStack.EMPTY;
    }

    /** 从掉落上下文里取工具的时运等级（对应 1.12.2 {@code getDrops(..., fortune)} 的 fortune 参数）。 */
    private static int fortuneOf(LootContextParameterSet.Builder builder) {
        ItemStack tool = builder.get(LootContextParameters.TOOL);
        if (tool == null || tool.isEmpty()) {
            return 0;
        }
        Registry<Enchantment> enchantments = builder.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        return enchantments.getEntry(Enchantments.FORTUNE)
                .map(entry -> EnchantmentHelper.getLevel(entry, tool))
                .orElse(0);
    }

    private record Drop(int weight, Item item, int min, int max) {
    }
}
