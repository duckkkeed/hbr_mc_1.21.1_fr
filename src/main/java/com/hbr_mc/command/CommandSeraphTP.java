package com.hbr_mc.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.hbr_mc.util.SeraphTeleportWorldStorage;

public final class CommandSeraphTP {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("seraphtp").executes(ctx -> toggle(ctx.getSource())));
            dispatcher.register(CommandManager.literal("srtp").executes(ctx -> toggle(ctx.getSource())));
            dispatcher.register(CommandManager.literal("toggletp").executes(ctx -> toggle(ctx.getSource())));
        });
    }

    private static int toggle(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        boolean isOn = SeraphTeleportWorldStorage.get(player);
        SeraphTeleportWorldStorage.set(player, !isOn);
        Text msg = Text.translatable(!isOn ? "hbr_mc.msg.teleport_allow" : "hbr_mc.msg.teleport_deny");
        source.sendFeedback(() -> msg, false);
        return 1;
    }
}
