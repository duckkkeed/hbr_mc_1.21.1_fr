package com.hbr_mc.item.seraph;

import com.hbr_mc.component.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Seraph 成长系统的读写工具，对应 1.12.2 的 {@code items/seraph/SeraphUtil}。
 *
 * <h2>与原版的对应</h2>
 * <table>
 *   <caption>数据键与公式</caption>
 *   <tr><th>项目</th><th>1.12.2 NBT</th><th>1.21.1 组件</th><th>规则</th></tr>
 *   <tr><td>等级</td><td>{@code lv_idl}</td><td>{@code hbr_mc:seraph_level}</td><td>默认 1，夹在 [1, 最大等级]</td></tr>
 *   <tr><td>经验</td><td>{@code lv_xp}</td><td>{@code hbr_mc:seraph_xp}</td><td>默认 0，升级所需 {@code 等级 * 100}</td></tr>
 *   <tr><td>强化</td><td>{@code boost_lvl}</td><td>{@code hbr_mc:seraph_boost}</td><td>夹在 [0, 强化上限]</td></tr>
 *   <tr><td>突破</td><td>{@code brk_thru}</td><td>{@code hbr_mc:seraph_breakthrough}</td><td>夹在 [0, 突破上限]</td></tr>
 *   <tr><td>技能等级</td><td>{@code sLv_1..3}</td><td>{@code hbr_mc:seraph_skill_level_N}</td><td>默认 1，上限 10</td></tr>
 *   <tr><td>技能已用次数</td><td>{@code usage_1..3}</td><td>{@code hbr_mc:seraph_skill_usage_N}</td><td>默认 0，使用后自增</td></tr>
 *   <tr><td>损坏</td><td>{@code is_broken}</td><td>{@code hbr_mc:seraph_broken}</td><td>见 {@link ItemSeraphBase#isBroken}</td></tr>
 * </table>
 *
 * <p>最大等级（对应原版 {@code getMaxLevel}）：SS = 120 + 突破×10，S = 110 + 突破×2，A = 100 + 突破×1。
 *
 * <p>等级还会联动耐久：原版覆写 {@code getMaxDamage(ItemStack)} 按等级实时计算，
 * 1.21.1 不能覆写，所以这里在等级变化时把结果写进物品堆的 {@code MAX_DAMAGE} 组件
 * （公式见 {@link ItemSeraphBase#maxDamageForLevel(int)}），并顺手刷新属性修饰符组件
 * （见 {@link SeraphAttributes}）。
 */
public final class SeraphUtil {

    /** 技能等级上限（原版 {@code getSkillLevelMax} 恒返回 10）。 */
    public static final int SKILL_LEVEL_MAX = 10;

    /** 技能槽位数量上限（原版 {@code USAGE} / {@code SKILL_LEVEL} 数组长度）。 */
    public static final int SKILL_SLOT_COUNT = 3;

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

    /** 设置等级（会自动夹到允许范围），并同步按等级计算的耐久上限与属性修饰符。 */
    public static void setLevel(ItemStack stack, int level) {
        if (!isSeraph(stack)) return;
        stack.set(ModComponents.SERAPH_LEVEL, clampedLevel(level, stack));
        applyLevelDurability(stack);
        SeraphAttributes.refresh(stack);
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

    /** 设置强化次数（夹在 [0, 上限]）。强化会改变六维，所以要刷新属性修饰符。 */
    public static void setBoost(ItemStack stack, int value) {
        if (!isSeraph(stack)) return;
        int max = SeraphBoostConst.getMaxBoost(stack);
        stack.set(ModComponents.SERAPH_BOOST, Math.max(0, Math.min(value, max)));
        SeraphAttributes.refresh(stack);
    }

    public static void addBoost(ItemStack stack, int amount) {
        setBoost(stack, getBoost(stack) + amount);
    }

    public static int getMaxBoost(ItemStack stack) {
        return SeraphBoostConst.getMaxBoost(stack);
    }

    /**
     * 把成长数据从一件炽天使复制到另一件（用于 A → SS 升阶，对应 1.12.2 {@code SeraphRankUp} 复制 NBT）。
     *
     * <p>只复制等级/经验/强化/突破/损坏标记/技能等级；目标物品的耐久按它自己的稀有度重新计算，
     * 属性修饰符也按目标物品重算（SS 的百分比表与 A 不同）。
     */
    public static void copyProgression(ItemStack from, ItemStack to) {
        if (!isSeraph(from) || !isSeraph(to)) {
            return;
        }
        // 先写突破，setLevel 里夹等级时才会用上新的上限。
        to.set(ModComponents.SERAPH_BREAKTHROUGH, getBreakThrough(from));
        to.set(ModComponents.SERAPH_XP, getXP(from));
        to.set(ModComponents.SERAPH_BOOST, getBoost(from));
        // 技能等级也随武器一起升阶（原版复制的是整份 NBT）
        for (int slot = 0; slot < SKILL_SLOT_COUNT; slot++) {
            setSkillLevel(to, slot, getSkillLevel(from, slot));
        }
        setLevel(to, getLevel(from));
        if (ItemSeraphBase.isBroken(from)) {
            to.set(ModComponents.SERAPH_BROKEN, true);
        }
        SeraphAttributes.refresh(to);
    }

    // ------------------------------------------------------------------ 技能 --

    /**
     * 技能等级（对应原版 {@code getSkillLevel}）：缺失时按 1 级，夹在 [1, 10]。
     *
     * <p>原版没有做上界夹断（靠 {@code SeraphUtil.skillLevelUp} 自己判上限），
     * 这里统一夹一次，避免命令/配方写入越界值后技能次数异常。
     */
    public static int getSkillLevel(ItemStack stack, int slot) {
        if (!isValidSlot(slot)) return 1;
        return Math.max(1, Math.min(stack.getOrDefault(ModComponents.SERAPH_SKILL_LEVELS[slot], 1), SKILL_LEVEL_MAX));
    }

    public static void setSkillLevel(ItemStack stack, int slot, int level) {
        if (!isSeraph(stack) || !isValidSlot(slot)) return;
        stack.set(ModComponents.SERAPH_SKILL_LEVELS[slot], Math.max(1, Math.min(level, SKILL_LEVEL_MAX)));
    }

    /** 技能等级上限（对应原版 {@code getSkillLevelMax}，恒 10）。 */
    public static int getSkillLevelMax(ItemStack stack, int slot) {
        return SKILL_LEVEL_MAX;
    }

    /**
     * 技能升级，对应原版 {@code SeraphUtil.skillLevelUp}。
     *
     * <p>原版实现有个小坑：先读 {@code getSkillLevel}（默认 1，永远不会是负数），
     * 再判断 {@code level < 0} 才写 1，所以那段分支是死代码——这里保留了同样的最终效果
     * （缺失 → 1，未满 → +1），但去掉了死分支。
     */
    public static void skillLevelUp(ItemStack stack, int slot) {
        int level = getSkillLevel(stack, slot);
        if (level < SKILL_LEVEL_MAX) {
            setSkillLevel(stack, slot, level + 1);
        }
    }

    /** 该槽位已使用次数（对应原版 {@code usage_N}）。 */
    public static int getSkillUsage(ItemStack stack, int slot) {
        if (!isValidSlot(slot)) return 0;
        return stack.getOrDefault(ModComponents.SERAPH_SKILL_USAGES[slot], 0);
    }

    /** 记录一次使用（对应原版 {@code skillUseMark}）。 */
    public static void markSkillUsed(ItemStack stack, int slot) {
        if (!isSeraph(stack) || !isValidSlot(slot)) return;
        stack.set(ModComponents.SERAPH_SKILL_USAGES[slot], getSkillUsage(stack, slot) + 1);
    }

    /** 清零所有槽位的已用次数（对应原版 {@code recoverSeraphSkill} 里循环置 0 的部分）。 */
    public static void resetSkillUsage(ItemStack stack) {
        if (!isSeraph(stack)) return;
        for (int slot = 0; slot < SKILL_SLOT_COUNT; slot++) {
            stack.set(ModComponents.SERAPH_SKILL_USAGES[slot], 0);
        }
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < SKILL_SLOT_COUNT;
    }

    // ------------------------------------------------------------------ 耐久 --

    /**
     * 修复一定耐久但<b>不清除</b>损坏标记（对应原版 {@code cureSeraph}）。
     *
     * <p>注意原版的行为：只在"没损坏"时生效——已经损坏的武器要靠 {@link #revive} 才能真正复活。
     */
    public static void cureSeraph(ItemStack stack, double amount) {
        if (stack.isEmpty() || ItemSeraphBase.isBroken(stack)) {
            return;
        }
        stack.setDamage(Math.max(0, (int) (stack.getDamage() - amount)));
    }

    /**
     * 复活并修复（对应原版 {@code reviveSeraph}）。
     *
     * @return 修复前是否处于损坏状态
     */
    public static boolean revive(ItemStack stack, double amount) {
        return ItemSeraphBase.revive(stack, amount);
    }

    /** 完全修复（对应原版 {@code repairSeraphFully}）。 */
    public static void repairFully(ItemStack stack) {
        ItemSeraphBase.repairFully(stack);
    }

    /**
     * 修复耐久 + 清空技能使用次数（对应原版 {@code recoverSeraphSkill}）。
     */
    public static void recoverSeraphSkill(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemSeraphBase.repairFully(stack);
        resetSkillUsage(stack);
    }

    // -------------------------------------------------------------- 手持查询 --

    /** 主手优先、其次副手的炽天使（不判损坏），对应原版 {@code getFirstSeraphInHand}。 */
    public static ItemStack getFirstSeraphInHand(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (isSeraph(main)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        return isSeraph(off) ? off : ItemStack.EMPTY;
    }

    /** 主手优先、其次副手的「未损坏」炽天使，对应原版 {@code getFirstSeraphNonBrokenInHand}。 */
    public static ItemStack getFirstSeraphNonBrokenInHand(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (isFunctioningSeraph(main)) {
            return main;
        }
        ItemStack off = player.getOffHandStack();
        return isFunctioningSeraph(off) ? off : ItemStack.EMPTY;
    }

    /** 是否为"能用的"炽天使（炽天使且未损坏），对应原版私有的 {@code isFunctioningSeraph}。 */
    public static boolean isFunctioningSeraph(ItemStack stack) {
        return isSeraph(stack) && !ItemSeraphBase.isBroken(stack);
    }

    /** 主手是否握着一把能用的炽天使（原版 {@code canAttackWithMainHandSeraph}）。 */
    public static boolean canAttackWithMainHandSeraph(PlayerEntity player) {
        return isFunctioningSeraph(player.getMainHandStack());
    }

    /**
     * 该生物是否"正在使用炽天使"（主手持有且未损坏），对应原版 {@code SeraphUtil.isUsingSeraph}。
     *
     * <p>DP 的破壳判定用它：只有拿着未损坏炽天使的攻击者才能打破星癌体的壳。
     */
    public static boolean isUsingSeraph(net.minecraft.entity.LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }
        return isFunctioningSeraph(player.getMainHandStack());
    }

    // ------------------------------------------------------------- 内部工具 --

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
