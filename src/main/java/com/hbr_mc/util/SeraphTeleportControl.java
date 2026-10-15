package com.hbr_mc.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

/**
 * Seraph teleport execution logic
 * <p>Seraph传送执行逻辑。
 *
 * <p>本类对应 1.12.2 Forge 版 {@code ItemSeraphBase#onItemUse} 里的传送部分，语义逐条保留：
 *
 * <ol>
 *   <li><b>竖直面（上/下）</b>：沿视线水平方向冲刺，距离从 5 递减到 4（原 {@code while (distance > 3)}），
 *       取第一个可站立的位置；<b>潜行时向反方向</b>冲刺。</li>
 *   <li><b>水平面（侧面）</b>：从被点击方块的 Y 开始向上搜索（上限为玩家 Y + 10），
 *       找到第一个可站立的位置并传送过去，即"爬上墙顶"。</li>
 *   <li>点击<b>底面</b>直接放弃；目标面不是完整面时，需要开启 rough 模式才允许（原 {@code SERAPH_TELEPORT_ROUGH}）。</li>
 * </ol>
 *
 * <p><b>与 1.12.2 的重要差异</b>：原版的 {@code EntityPlayerMP#attemptTeleport} 会检测目标点能否站立，
 * 不能则返回 {@code false}；而 1.21.1 的 {@link Entity#teleport} 只负责移动、<b>基本总是返回 true</b>。
 * 因此这里改为在传送前用 {@link #canTeleportTo} 自行校验（脚部与头部两格都无碰撞），
 * 否则玩家会被塞进方块里。
 */
public final class SeraphTeleportControl {
    private SeraphTeleportControl() {}

    /** 冲刺起始距离；原版从 5 递减，到 3 为止（故实际尝试 5 与 4）。 */
    private static final int DASH_START_DISTANCE = 5;
    private static final int DASH_MIN_DISTANCE = 3;
    /** 爬墙时相对玩家 Y 的最大搜索高度（原版 {@code yPlayer + 10}）。 */
    private static final int MAX_CLIMB_ABOVE_PLAYER = 10;

    /** 一次传送尝试的结果。 */
    public enum Status {
        /** 传送成功。 */
        SUCCESS,
        /** 未开启传送开关 / 客户端侧：静默放弃，调用方应返回 PASS。 */
        PASS,
        /** 该面不允许传送（底面，或未开启 rough 模式时的非完整面）。 */
        INVALID_FACE,
        /** 搜索了所有候选位置但没有可站立的落点。 */
        NO_VALID_DESTINATION,
    }

    /**
     * 目标坐标能否容纳站立的玩家：脚部与头部两格都必须没有碰撞形状，且不能有流体。
     *
     * <p>流体检查沿用本工程原有的传送实现（避免把玩家送进水里），
     * 1.12.2 原版没有这一条。
     */
    public static boolean canTeleportTo(World world, BlockPos feetPos) {
        if (world.isOutOfHeightLimit(feetPos)) return false;
        BlockPos headPos = feetPos.up();
        if (world.isOutOfHeightLimit(headPos)) return false;

        BlockState feet = world.getBlockState(feetPos);
        BlockState head = world.getBlockState(headPos);
        if (!feet.getCollisionShape(world, feetPos).isEmpty()) return false;
        if (!head.getCollisionShape(world, headPos).isEmpty()) return false;
        if (!world.getFluidState(feetPos).isEmpty()) return false;
        if (!world.getFluidState(headPos).isEmpty()) return false;
        // 落脚点下方也不能是流体（否则等于站到水面上）；对应本工程原有实现的 basePos 检查
        if (!world.getFluidState(feetPos.down()).isEmpty()) return false;
        return true;
    }

    /**
     * 按 1.12.2 的规则尝试把玩家传送到点击位置附近。
     *
     * <p>不做"完整面"判定：台阶、地毯等非完整面同样可以传送，只看落点是否站得住
     * （见 {@link #canTeleportTo}）。原版这里由一个玩家开关 {@code SERAPH_TELEPORT_ROUGH}
     * 控制，本工程按需要改为默认允许，因此不再保留该参数。
     *
     * @param player          必须是服务端玩家，否则返回 {@link Status#PASS}
     * @param world           服务端世界
     * @param clickedPos      右键点击的方块坐标
     * @param clickedFace     右键点击的面
     * @param teleportEnabled 该玩家的传送开关是否打开
     */
    public static Status trySeraphTeleport(ServerPlayerEntity player, World world, BlockPos clickedPos,
                                           Direction clickedFace, boolean teleportEnabled) {
        if (player == null || world == null || world.isClient) return Status.PASS;
        if (!teleportEnabled) return Status.PASS;

        // 底面不传送（原版直接 PASS）
        if (clickedFace == Direction.DOWN) return Status.INVALID_FACE;

        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return dash(player, world) ? Status.SUCCESS : Status.NO_VALID_DESTINATION;
        }
        return climbOntoWall(player, world, clickedPos) ? Status.SUCCESS : Status.NO_VALID_DESTINATION;
    }

    /** 竖直面：沿视线水平方向冲刺（潜行时反向），距离 5 → 4。 */
    private static boolean dash(ServerPlayerEntity player, World world) {
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d horizontal = new Vec3d(look.x, 0.0D, look.z);
        if (horizontal.lengthSquared() < 1.0E-4D) return false;
        horizontal = horizontal.normalize();

        double sign = player.isSneaking() ? -1.0D : 1.0D;
        for (int distance = DASH_START_DISTANCE; distance > DASH_MIN_DISTANCE; distance--) {
            double x = player.getX() + horizontal.x * distance * sign;
            double z = player.getZ() + horizontal.z * distance * sign;
            BlockPos feetPos = BlockPos.ofFloored(x, player.getY(), z);
            if (!canTeleportTo(world, feetPos)) continue;
            return teleportPlayer(player, x, feetPos.getY(), z);
        }
        return false;
    }

    /** 水平面：从被点击方块向上找到墙顶第一个可站立的位置（最多到玩家 Y + 10）。 */
    private static boolean climbOntoWall(ServerPlayerEntity player, World world, BlockPos clickedPos) {
        int maxY = MathHelper.floor(player.getY()) + MAX_CLIMB_ABOVE_PLAYER;
        for (int y = clickedPos.getY(); y <= maxY; y++) {
            BlockPos candidate = new BlockPos(clickedPos.getX(), y, clickedPos.getZ());
            if (!world.getBlockState(candidate).isAir()) continue;
            if (!canTeleportTo(world, candidate)) continue;
            return teleportPlayer(player, candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D);
        }
        return false;
    }

    /** 实际执行传送：保留玩家朝向，并清零下落距离。 */
    private static boolean teleportPlayer(ServerPlayerEntity player, double x, double y, double z) {
        ServerWorld serverWorld = player.getServerWorld();
        boolean moved = player.teleport(serverWorld, x, y, z, Set.of(), player.getYaw(), player.getPitch());
        if (moved) player.fallDistance = 0.0F;
        return moved;
    }
}
