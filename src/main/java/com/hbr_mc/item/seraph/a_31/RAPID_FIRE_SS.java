package com.hbr_mc.item.seraph.a_31;

import com.hbr_mc.util.SeraphTeleportControl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Item that performs Seraph teleport when right-clicking blocks while the feature is enabled.
 */
public class RAPID_FIRE_SS extends Item {
    public RAPID_FIRE_SS(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity == null) return ActionResult.PASS;
        if (world.isClient) return ActionResult.PASS;
        if (!(playerEntity instanceof ServerPlayerEntity)) return ActionResult.PASS;

        ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
        BlockPos clickedPos = context.getBlockPos();
        return SeraphTeleportControl.tryRapidFireTeleport(player, world, clickedPos)
                ? ActionResult.SUCCESS
                : ActionResult.PASS;
    }
}
