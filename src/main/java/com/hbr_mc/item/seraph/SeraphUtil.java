package com.hbr_mc.item.seraph;

import com.hbr_mc.component.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Seraph 成长系统的读写工具，对应 1.12.2 的 {@code items/seraph/SeraphUtil}（升级 / 强化 / 突破部分）。
 *
 * <h2>与原版的对应</h2>
 * <table>
 *   <caption>数据键与公式</caption>
 *   <tr><th>项目</th><th>1.12.2 NBT</th><th>1.21.1 组件</th><th>规则</th></tr>
 *   <tr><td>等级</td><td>{@code lv_idl}</td><td>{@code hbr_mc:seraph_level}</td><td>默认 1，夹在 [1, 最大等级]</td></tr>
 *   <tr><td>经验</td><td>{@code lv_xp}</td><td>{@code hbr_mc:seraph_xp}</td><td>默认 0，升级所需 {@code 等级 * 100}</td></tr>
 *   <tr><td>强化</td><td>{@code boost_lvl}</td><td>{@code hbr_mc:seraph_boost}</td><td>夹在 [0, 强化上限]</td></tr>
 *   <tr><td>突破</td><td>{@code brk_thru}</td><td>{@code hbr_mc:seraph_breakthrough}</td><td>夹在 [0, 突破上限]</td></tr>
 * </table>
 *
 * <p>最大等级（对应原版 {@code getMaxLevel}）：SS = 120 + 突破×10，S = 110 + 突破×2，A = 100 + 突破×1。
 *
 * <p>等级还会联动耐久：原版覆写 {@code getMaxDamage(ItemStack)} 按等级实时计算，
 * 1.21.1 不能覆写，所以这里在等级变化时把结果写进物品堆的 {@code MAX_DAMAGE} 组件
 * （公式见 {@link ItemSeraphBase#maxDamageForLevel(int)}）。
 *
 * <p><b>尚未移植</b>：强化/突破对属性（STR/DEX/…）的加成依赖属性系统，
 * 现在只保证次数可读写与上限正确，数值加成等内容见 {@link SeraphBoostConst#attrPercentagePerBreak}。
 */
public final class SeraphUtil {

    private SeraphUtil() {
    }

    public static boolean isSeraph(ItemStack stack) {
        return stack.getItem() instanceof ItemSeraphBase;
    }

    public static boolean isIntactSeraph(ItemStack stack) {
        return isSeraph(stack) && !ItemSeraphBase.isBroken(stack);
    }

    // ------------------------------------------------------------------ 等级 --

    static int clampedLevel(int level, ItemStack stack) {
        return Math.max(1, Math.min(level, getMaxLevel(stack)));
    }

    public static int getLevel(ItemStack stack) {
        return clampedLevel(stack.getOrDefault(ModComponents.SERAPH_LEVEL, 0), stack);
    }

    /** 设置等级（会自动夹到允许范围），并同步按等级计算的耐久上限。 */
    public static void setLevel(ItemStack stack, int level) {
        if (!isSeraph(stack)) return;
        stack.set(ModComponents.SERAPH_LEVEL, clampedLevel(level, stack));
        applyLevelDurability(stack);
    }

    /** 最大等级：稀有度基础值 + 突破次数带来的成长（对应原版 {@code getMaxLevel}）。 */
    public static int getMaxLevel(ItemStack stack) {
        if (stack.getItem() instanceof ItemSeraphBase base) {
            EnumSeraphRarity rarity = base.getSeraphRarity();
            int breakthrough = getBreakThrough(stack);
            return switch (rarity) {
                case SS -> 120 + breakthrough * 10;
                case S -> 110 + breakthrough * 2;
                case A -> 100 + breakthrough;
            };
        }
        return 100;
    }

    public static int getXP(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SERAPH_XP, 0);
    }

    public static void setXP(ItemStack stack, int xp) {
        stack.set(ModComponents.SERAPH_XP, Math.max(0, xp));
    }

    /** 升到下一级所需经验（对应原版 {@code getXPForLevel}：{@code level * 100}）。 */
    public static int getXPForLevel(int level) {
        return level * 100;
    }

    /**
     * 增加经验并按需升级，对应原版 {@code addXP}。
     *
     * <p>与原版一致：循环里的判定用的是<b>起始等级</b> {@code level}（不是 {@code newLevel}），
     * 所以一次加大量经验时，每级都按起始等级扣费。这是原实现的写法，这里保持不改变；
     * 如果以后想改成「逐级递增」，把循环里的 {@code getXPForLevel(level)} 换成 {@code getXPForLevel(newLevel)}。
     *
     * @param holder 升级时接收提示的玩家，可为 {@code null}
     */
    public static void addXP(ItemStack stack, int xp, PlayerEntity holder) {
        int currentXP = getXP(stack) + xp;
        int level = getLevel(stack);
        int newLevel = level;
        int maxLevel = getMaxLevel(stack);
        while (newLevel < maxLevel && currentXP >= getXPForLevel(level)) {
            currentXP -= getXPForLevel(level);
            newLevel++;
        }

        if (level != newLevel) {
            setLevel(stack, newLevel);
            if (holder != null) {
                Text msg = Text.translatable("msg.hbr_mc.level_up", level, newLevel)
                        .formatted(Formatting.BOLD);
                holder.sendMessage(msg, false);
            }
        }
        setXP(stack, currentXP);
    }

    // ------------------------------------------------------------------ 突破 --

    public static int getBreakThrough(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SERAPH_BREAKTHROUGH, 0);
    }

    /** 设置突破次数（夹在 [0, 上限]）；突破会抬高最大等级，因此顺便重新夹一次当前等级。 */
    public static void setBreakThrough(ItemStack stack, int value) {
        if (!isSeraph(stack)) return;
        int max = SeraphBoostConst.getMaxBreakThrough(stack);
        stack.set(ModComponents.SERAPH_BREAKTHROUGH, Math.max(0, Math.min(value, max)));
        setLevel(stack, getLevel(stack));
    }

    public static void addBreakThrough(ItemStack stack, int amount) {
        setBreakThrough(stack, getBreakThrough(stack) + amount);
    }

    public static int getMaxBreakThrough(ItemStack stack) {
        return SeraphBoostConst.getMaxBreakThrough(stack);
    }

    // ------------------------------------------------------------------ 强化 --

    public static int getBoost(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SERAPH_BOOST, 0);
    }

    /** 设置强化次数（夹在 [0, 上限]）。 */
    public static void setBoost(ItemStack stack, int value) {
        if (!isSeraph(stack)) return;
        int max = SeraphBoostConst.getMaxBoost(stack);
        stack.set(ModComponents.SERAPH_BOOST, Math.max(0, Math.min(value, max)));
    }

    public static void addBoost(ItemStack stack, int amount) {
        setBoost(stack, getBoost(stack) + amount);
    }

    public static int getMaxBoost(ItemStack stack) {
        return SeraphBoostConst.getMaxBoost(stack);
    }

    // ------------------------------------------------------------------ 耐久 --

    /**
     * 按当前等级重写物品堆的 {@code MAX_DAMAGE} 组件（对应原版按等级计算的 {@code getMaxDamage}）。
     *
     * <p>只加不减的等级成长下，耐久上限只升不降；但 {@code /seraph level} 允许调低，
     * 所以这里在降低时把当前损伤夹到新上限，避免出现 {@code damage > maxDamage}。
     */
    private static void applyLevelDurability(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSeraphBase base)) return;
        int max = base.maxDamageForLevel(getLevel(stack));
        if (stack.getMaxDamage() != max) {
            stack.set(DataComponentTypes.MAX_DAMAGE, max);
            if (stack.getDamage() > max) {
                stack.setDamage(max);
            }
        }
    }
}
