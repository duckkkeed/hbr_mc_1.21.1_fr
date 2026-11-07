package com.hbr_mc.worldgen.feature;

import com.hbr_mc.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * 随机补给箱的地表生成，对应 1.12.2 的 {@code worldgen/ModGenDropsOverworld}。
 *
 * <p>原版逻辑：每区块抽一列（x/z 在区块内随机），从 y=255 往下找第一个
 * 「方块顶面是完整实心 + 上方是空气」的位置，按 {@code POT_DENSITY = 0.1} 的概率在其上方放一个补给箱。
 * 也就是说平均每 10 个区块生成 1 个，且只出现在地表（含树顶，和原版一致）。
 *
 * <p>1.21.1 拆成两层：
 * <ul>
 *   <li>「10% 概率」交给 placed_feature 的 {@code minecraft:rarity_filter chance: 10}（每区块判定一次）；</li>
 *   <li>「找地表并放置」由本特性完成——扫描从世界顶部开始，因此不依赖高度图类型。</li>
 * </ul>
 *
 * <p>差异：1.12.2 的列坐标以区块中心为基准取 {@code +0..15}，会溢出到相邻区块最多 8 格；
 * 这里用 {@code in_square} 严格限制在本区块内。
 */
public class RandomDropCrateFeature extends Feature<DefaultFeatureConfig> {

    public RandomDropCrateFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        int x = origin.getX();
        int z = origin.getZ();

        BlockPos.Mutable below = new BlockPos.Mutable();
        BlockPos.Mutable above = new BlockPos.Mutable();
        for (int y = world.getTopY() - 1; y > world.getBottomY(); y--) {
            below.set(x, y, z);
            BlockState state = world.getBlockState(below);
            if (!state.isSideSolidFullSquare(world, below, Direction.UP)) {
                continue;
            }
            above.set(x, y + 1, z);
            if (!world.getBlockState(above).isAir()) {
                continue;
            }
            // flags = 2：通知客户端，不触发邻接更新（世界生成阶段的常规做法）。
            world.setBlockState(above, ModBlocks.RANDOM_DROP_BASIC.getDefaultState(), Block.NOTIFY_LISTENERS);
            return true;
        }
        return false;
    }
}
