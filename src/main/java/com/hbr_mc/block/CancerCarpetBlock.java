package com.hbr_mc.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * 星癌体地毯，对应 1.12.2 的 {@code blocks/BlockCarpetBase}（注册名 {@code cancer_carpet}）。
 *
 * <p>1.12.2 的 {@code BlockCarpetBase} 继承自 {@code BlockBase}，自己实现 1/16 高的碰撞箱，
 * 并在 {@code canBlockStay} 里只要求「下方不是空气」；支撑方块被破坏时靠 {@code neighborChanged}
 * 掉落自身。1.21.1 的 {@link CarpetBlock} 已提供碰撞箱与「支撑消失即掉落」的更新逻辑，
 * 唯一差异是原版要求下方为完整实心面，这里覆写 {@link #canPlaceAt} 恢复 1.12.2 的「非空气即可」。
 *
 * <p>方块属性来自 {@code new BlockCarpetBase("cancer_carpet", Material.IRON)
 * .setHardness(4f).setResistance(999f)}：硬度 4、爆炸抗性 999、铁灰色地图色、石头音效（默认）。
 */
public class CancerCarpetBlock extends CarpetBlock {

    public static final MapCodec<CancerCarpetBlock> CODEC = createCodec(CancerCarpetBlock::new);

    public CancerCarpetBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends CancerCarpetBlock> getCodec() {
        return CODEC;
    }

    /** 对应 1.12.2 {@code BlockCarpetBase#canBlockStay}：下方非空气即可。 */
    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !world.getBlockState(pos.down()).isAir();
    }
}
