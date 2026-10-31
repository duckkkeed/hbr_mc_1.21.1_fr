package com.hbr_mc.command;

import com.hbr_mc.item.seraph.SeraphUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * {@code /seraph}：读写主手 Seraph 武器的成长数据，供测试与管理使用。
 *
 * <p>原版 1.12.2 的升级经验来自击杀 Cancer、强化来自「武器 + 5 个癌壳」配方、突破来自同名武器/碎片配方；
 * 这些实体与材料在 1.21.1 工程里还没有移植，所以先提供命令入口：
 *
 * <pre>
 * /seraph info                  查看主手武器当前等级 / 经验 / 强化 / 突破
 * /seraph level &lt;等级&gt;          直接设置等级（自动夹到上限）
 * /seraph xp &lt;数量&gt;             增加经验（可连升多级，会发升级提示）
 * /seraph boost &lt;次数&gt;          设置强化次数（夹到 0..上限）
 * /seraph breakthrough &lt;次数&gt;   设置突破次数（会抬高等级上限）
 * </pre>
 *
 * <p>需要权限等级 2（与 {@code /seraphtp} 一致，管理员可用）。
 */
public final class CommandSeraph {

    private static final SimpleCommandExceptionType NO_SERAPH = new SimpleCommandExceptionType(
            Text.translatable("command.hbr_mc.seraph.no_item"));

    private CommandSeraph() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("seraph")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("info")
                                .executes(ctx -> info(ctx.getSource())))
                        .then(CommandManager.literal("level")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(1))
                                        .executes(ctx -> setLevel(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")))))
                        .then(CommandManager.literal("xp")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> addXp(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(CommandManager.literal("boost")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setBoost(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")))))
                        .then(CommandManager.literal("breakthrough")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setBreakThrough(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")))))
                ));
    }

    private static ItemStack mainHandSeraph(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ItemStack stack = player.getMainHandStack();
        if (!SeraphUtil.isSeraph(stack)) {
            throw NO_SERAPH.create();
        }
        return stack;
    }

    private static int setLevel(ServerCommandSource source, int level) throws CommandSyntaxException {
        SeraphUtil.setLevel(mainHandSeraph(source), level);
        return info(source);
    }

    private static int addXp(ServerCommandSource source, int amount) throws CommandSyntaxException {
        ItemStack stack = mainHandSeraph(source);
        SeraphUtil.addXP(stack, amount, source.getPlayer());
        return info(source);
    }

    private static int setBoost(ServerCommandSource source, int value) throws CommandSyntaxException {
        SeraphUtil.setBoost(mainHandSeraph(source), value);
        return info(source);
    }

    private static int setBreakThrough(ServerCommandSource source, int value) throws CommandSyntaxException {
        SeraphUtil.setBreakThrough(mainHandSeraph(source), value);
        return info(source);
    }

    private static int info(ServerCommandSource source) throws CommandSyntaxException {
        ItemStack stack = mainHandSeraph(source);
        int level = SeraphUtil.getLevel(stack);
        source.sendFeedback(() -> Text.translatable("command.hbr_mc.seraph.info",
                level,
                SeraphUtil.getXP(stack),
                SeraphUtil.getXPForLevel(level),
                SeraphUtil.getBoost(stack),
                SeraphUtil.getMaxBoost(stack),
                SeraphUtil.getBreakThrough(stack),
                SeraphUtil.getMaxBreakThrough(stack)), false);
        return 1;
    }
}
