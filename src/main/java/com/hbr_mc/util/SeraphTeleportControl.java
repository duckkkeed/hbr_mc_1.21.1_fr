package com.hbr_mc.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Shared Seraph teleport execution logic used by teleport items.
 */
public final class SeraphTeleportControl {
    private SeraphTeleportControl() {}

    public static boolean tryRapidFireTeleport(ServerPlayerEntity player, World world, BlockPos clickedPos) {
        if (player == null || world == null || world.isClient) return false;
        if (!SeraphTeleportWorldStorage.get(player)) return false;

        BlockPos basePos = clickedPos;
        BlockState clickedState = world.getBlockState(clickedPos);
        if (clickedState.getCollisionShape(world, clickedPos).isEmpty()) {
            basePos = clickedPos.down();
        }

        BlockState baseState = world.getBlockState(basePos);
        if (baseState.getCollisionShape(world, basePos).isEmpty()) return false;
        if (!world.getFluidState(basePos).isEmpty()) return false;

        BlockPos feetPos = basePos.up();
        BlockPos headPos = feetPos.up();

        if (!world.getBlockState(feetPos).getCollisionShape(world, feetPos).isEmpty()) return false;
        if (!world.getBlockState(headPos).getCollisionShape(world, headPos).isEmpty()) return false;
        if (!world.getFluidState(feetPos).isEmpty()) return false;
        if (!world.getFluidState(headPos).isEmpty()) return false;

        double tx = feetPos.getX() + 0.5D;
        double ty = feetPos.getY();
        double tz = feetPos.getZ() + 0.5D;

        player.teleport((ServerWorld) world, tx, ty, tz, player.getYaw(), player.getPitch());
        player.fallDistance = 0.0F;
        return true;
    }

    public static boolean tryRapidFireTeleport(PlayerEntity player, World world, BlockPos clickedPos) {
        if (player instanceof ServerPlayerEntity) {
            return tryRapidFireTeleport((ServerPlayerEntity) player, world, clickedPos);
        }
        return false;
    }
}
