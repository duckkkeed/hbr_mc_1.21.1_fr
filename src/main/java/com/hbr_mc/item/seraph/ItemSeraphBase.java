package com.hbr_mc.item.seraph;

import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatConfig;
import com.hbr_mc.component.ModComponents;
import com.hbr_mc.item.ModItems;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import com.hbr_mc.util.SeraphTeleportControl;
import com.hbr_mc.util.SeraphTeleportWorldStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 所有「Seraph」系物品的基类，对应 1.12.2 Forge 版工程的 {@code ItemSeraphBase}。
 *
 * <h2>已移植的行为</h2>
 * <ul>
 *   <li><b>命中不掉耐久</b>：{@link #postHit} 返回 false + {@link #postDamageEntity} 留空（§1）；</li>
 *   <li><b>耐久条常显 + 动态颜色</b>：{@link #isItemBarVisible} / {@link #getItemBarColor}（§2）；</li>
 *   <li><b>右键传送</b>：{@link #useOnBlock} → {@link SeraphTeleportControl}（§3）；</li>
 *   <li><b>稀有度 / 发光 / 堆叠 / 耐久</b>：见 {@link SeraphItemSettings}（§4）；</li>
 *   <li><b>蓄力技能流程</b>：{@link #use} / {@link #finishUsing} / {@link #onStoppedUsing}（§5）与技能次数（§12）；</li>
 *   <li><b>被攻击「伤害转耐久」</b>：见 {@code com.hbr_mc.event.SeraphDamageAbsorb}（§1.6）；</li>
 *   <li><b>成长数据</b>：等级/经验/强化/突破见 {@link SeraphUtil}（§10）；</li>
 *   <li><b>属性加成</b>：{@link #getAttrValue} / {@link #getAttrValuePercent} + {@link SeraphAttributes}（§6）；</li>
 *   <li><b>技能与战斗</b>：{@link #castSkillEnemy} / {@link #castSkillFriend} / {@link #postCastSkill} /
 *       {@link #calcAtkBuffSkill}（§12）。</li>
 * </ul>
 *
 * <h2>1.21.1 上无法照搬的部分</h2>
 * <ul>
 *   <li>{@code getMaxDamage} / {@code setDamage} 不能被覆写：改为写 {@code MAX_DAMAGE} 组件 +
 *       {@link #damageDurability} 夹断（见 {@link SeraphUtil#setLevel}）；</li>
 *   <li>{@code getAttributeModifiers} 没有"按物品堆实时计算"的覆写点：改为"数据变化时重写
 *       {@code ATTRIBUTE_MODIFIERS} 组件"（见 {@link SeraphAttributes}）；</li>
 *   <li>{@code itemInteractionForEntity} 更名为 {@link #useOnEntity}；</li>
 *   <li>{@code shouldCauseReequipAnimation} / {@code doesSneakBypassUse} / {@code canItemEditBlocks} /
 *       {@code getHasSubtypes} 是 Forge 专有钩子，1.21.1 没有对应 API（不影响现有行为）。</li>
 * </ul>
 *
 * <h2>1.12.2 里刻意保留的"原始行为"</h2>
 * <ul>
 *   <li>{@link #getAttrValue} 的 switch <b>缺 break</b>：SS 先 +15 再落到 S 再 +5（合计 +20）；</li>
 *   <li>{@link #getMaxSkillSlot} 的 switch 同样是 SS=2、S 落到末尾=2、A=1；</li>
 *   <li>{@link #calcAtkBuffSkill} 的层数扣减公式（{@code lastBuffLv - 2}）照抄。</li>
 * </ul>
 */
public class ItemSeraphBase extends Item {

    /** 已构造的全部 Seraph 物品（物品在注册表里是单例，构造即注册）。 */
    private static final List<ItemSeraphBase> REGISTERED = new ArrayList<>();

    /** 传送成功后的冷却 tick 数（原版 {@code setCooldown(this, 5)}）。 */
    protected static final int TELEPORT_COOLDOWN_TICKS = 5;

    /** 每个会话只提示一次：如何打开传送开关（原版 {@code need_teleport_hint}）。 */
    private static boolean needTeleportHint = true;

    /** 技能槽位常量：原版 {@code SLOT_ULTI = 1}（终极技固定放 1 号槽）。 */
    public static final int SLOT_ULTI = 1;

    /**
     * 本次技能结算消耗掉的攻击力上升层数折算出的总倍率（原版 {@code currentBuffRateTotal}）。
     *
     * <p>弹射物在生成时会<b>快照</b>这个值，命中时再套用，避免"飞行途中又放了个技能"影响已在飞的子弹
     * （原版 {@code EntityHBRProjectile} 的构造里就是拷一份 {@code atkUpRate}）。
     */
    public static float currentBuffRateTotal = 0.0F;

    private final EnumSeraphRarity seraphRarity;
    /** 战斗职业（原版 {@code seraphClass}），决定强化表与 SS 百分比表。 */
    private EnumSeraphClass seraphClass = EnumSeraphClass.FIGHTER;
    /** 武器类型（原版 {@code final EnumSeraphType type}，由构造参数决定）。 */
    private EnumSeraphType seraphType;

    public ItemSeraphBase(Settings settings, EnumSeraphRarity seraphRarity) {
        this(settings, seraphRarity, null, EnumSeraphClass.FIGHTER);
    }

    public ItemSeraphBase(Settings settings, EnumSeraphRarity seraphRarity, EnumSeraphType seraphType) {
        this(settings, seraphRarity, seraphType, EnumSeraphClass.FIGHTER);
    }

    public ItemSeraphBase(Settings settings, EnumSeraphRarity seraphRarity,
                          EnumSeraphType seraphType, EnumSeraphClass seraphClass) {
        super(settings);
        this.seraphRarity = seraphRarity;
        this.seraphType = seraphType;
        this.seraphClass = seraphClass;
        REGISTERED.add(this);
        // 抽卡池登记（对应原版 ItemSeraphBase 构造里的 ItemLottery.addA/addSS）
        com.hbr_mc.item.LotteryItem.registerSeraph(this);
    }

    // ------------------------------------------------------------------ 属性 --

    /** 本物品的稀有度。 */
    public EnumSeraphRarity getSeraphRarity() {
        return seraphRarity;
    }

    /** 战斗职业。 */
    public EnumSeraphClass getSeraphClass() {
        return seraphClass;
    }

    /** 武器类型；未指定时为 {@code null}。 */
    public EnumSeraphType getSeraphType() {
        return seraphType;
    }

    /**
     * 按等级计算耐久上限（对应 1.12.2 覆写的 {@code getMaxDamage(ItemStack)}）：
     * {@code base = 600 + 7 * level}，再按稀有度放大。
     *
     * <p>1.21.1 不能覆写 {@code getMaxDamage}，所以由 {@link SeraphUtil#setLevel} 把结果写进
     * 物品堆的 {@code MAX_DAMAGE} 组件。
     */
    public int maxDamageForLevel(int level) {
        int base = 600 + 7 * level;
        return switch (seraphRarity) {
            case SS -> (int) (base * 1.7F) + 200;
            case S -> (int) (base * 1.5F) + 50;
            case A -> (int) (base * 1.3F);
        };
    }

    /** 全部已登记的 Seraph 物品（只读）。 */
    public static List<ItemSeraphBase> registeredItems() {
        return Collections.unmodifiableList(REGISTERED);
    }

    /**
     * 固定值属性加成，对应 1.12.2 的 {@code getAttrValue}。
     *
     * <pre>
     * base = 等级 + 7
     * switch (稀有度) {
     *     case SS: base += 15;   // ← 原版这里没有 break
     *     case S:  base += 5;
     *     default: ;
     * }
     * 然后遍历前 boost 条强化，把 FIXED 项累加（attr 相同，或该条目的 attr 为 null 也算）
     * </pre>
     *
     * <p><b>原版 switch 缺 break 已确认是有意的现状</b>（见 docs/seraph-port.md §9），
     * 效果是 SS 合计 +20、S +5、A +0。若你想改成互斥分支（SS=15、S=5），把 fallthrough 拆开即可。
     */
    public float getAttrValue(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        int base = SeraphUtil.getLevel(stack) + 7;
        switch (seraphRarity) {
            case SS:
                base += 15;
                // 故意 fallthrough：原版就是 SS 再吃一次 S 的 +5
            case S:
                base += 5;
            default:
                break;
        }

        int boostLevel = SeraphUtil.getBoost(stack);
        for (int i = 0; i < boostLevel; i++) {
            SeraphBoostUnit unit = SeraphAttrData.boostUnitAt(seraphClass, i);
            if (unit == null) {
                break;
            }
            if (unit.attr() == attribute || unit.attr() == null) {
                if (unit.type() == EnumBoostType.FIXED) {
                    base += (int) unit.value();
                }
            }
        }
        return base;
    }

    /**
     * 百分比属性加成，对应 1.12.2 的 {@code getAttrValuePercent}。
     *
     * <p>起点是 {@link SeraphAttrData} 里按稀有度 + 职业查到的表；再累加强化里的 PERCENT 项；
     * 最后加上"每次突破 +N%"（{@link SeraphBoostConst#attrPercentagePerBreak} × 突破次数）。
     */
    public float getAttrValuePercent(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        Float tableValue = SeraphAttrData.getData(seraphRarity, seraphClass).get(attribute);
        float base = tableValue == null ? 0.0F : tableValue;

        int boostLevel = SeraphUtil.getBoost(stack);
        for (int i = 0; i < boostLevel; i++) {
            SeraphBoostUnit unit = SeraphAttrData.boostUnitAt(seraphClass, i);
            if (unit == null) {
                break;
            }
            if (unit.attr() == attribute || unit.attr() == null) {
                if (unit.type() == EnumBoostType.PERCENT) {
                    base += unit.value();
                }
            }
        }

        base += SeraphBoostConst.attrPercentagePerBreak(seraphRarity) * SeraphUtil.getBreakThrough(stack);
        return base;
    }

    // ------------------------------------------------------------------ 耐久 --

    /**
     * 物品是否已损坏（对应 1.12.2 的 {@code SeraphUtil.isBroken}）。
     *
     * <p>注意：<b>不是</b>比较 {@code damage} 与 {@code maxDamage}，而是读自定义组件
     * {@link ModComponents#SERAPH_BROKEN}。原版在 {@code setDamage} 覆写里
     * "当伤害<b>超过</b>上限"时才写这个标记，因此耐久刚好等于上限的物品还不算损坏（与原版一致）。
     */
    public static boolean isBroken(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModComponents.SERAPH_BROKEN));
    }

    /**
     * 增加耐久损耗，行为对齐 1.12.2 的 {@code Item.setDamage} 覆写：
     * 超过上限时<b>夹到 {@code maxDamage}</b> 并打上损坏标记，<b>永不销毁物品</b>。
     *
     * <p>1.21.1 的 {@code ItemStack#damage(...)} 在耐久归零时会打碎物品，
     * 而 1.12.2 的 Seraph 物品是"夹到上限 + 记损坏"而不是消失，所以这里不用 {@code damage(...)}。
     *
     * @param amount 耐久损耗（可以是小数，按原版语义向下取整）
     * @return 本次之后是否处于损坏状态
     */
    public static boolean damageDurability(ItemStack stack, double amount) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0 || amount <= 0.0D) return isBroken(stack);

        // 用 double 累加避免 int 溢出（原版是 (int)(damage + amount)，等价）
        double total = stack.getDamage() + amount;
        boolean exceeded = total > maxDamage;
        int newDamage = exceeded ? maxDamage : (int) total;

        if (exceeded) {
            setBroken(stack);
        }
        stack.setDamage(newDamage);
        return isBroken(stack);
    }

    /** 打上损坏标记（对应原版 {@code IDLNBTUtil.SetBoolean(stack, KEY_BROKEN, true)}）。 */
    private static void setBroken(ItemStack stack) {
        stack.set(ModComponents.SERAPH_BROKEN, true);
    }

    /** 清除损坏标记。 */
    private static void clearBroken(ItemStack stack) {
        stack.remove(ModComponents.SERAPH_BROKEN);
    }

    /** 完全修复：耐久归零并清除损坏标记（对应 1.12.2 的 {@code SeraphUtil.repairSeraphFully}）。 */
    public static void repairFully(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.setDamage(0);
        clearBroken(stack);
    }

    /**
     * 修复一定耐久并清除损坏标记（对应 1.12.2 的 {@code SeraphUtil.reviveSeraph}）。
     *
     * @return 修复前是否处于损坏状态
     */
    public static boolean revive(ItemStack stack, double amount) {
        if (stack.isEmpty()) return false;
        boolean wasBroken = isBroken(stack);
        int maxDamage = stack.getMaxDamage();
        int newDamage = Math.max(0, (int) (stack.getDamage() - amount));
        if (maxDamage > 0) {
            newDamage = Math.min(newDamage, maxDamage);
        }
        stack.setDamage(newDamage);
        clearBroken(stack);
        return wasBroken;
    }

    /**
     * 每 tick 的物品栏处理。
     *
     * <p>1.12.2 的 {@code Item#onUpdate} 里"每天一次判定自动修复"那段在原版就是被注释掉的
     * （修复概率仍是 {@code //todo}），这里保持注释状态。
     *
     * <p>真正有用的是<b>属性兜底刷新</b>：旧存档或其它 mod 生成的炽天使没有
     * {@code ATTRIBUTE_MODIFIERS} 组件，或组件与当前成长数据不一致时，这里每秒校正一次
     * （只在内容不同才写，见 {@link SeraphAttributes#refresh}）。
     */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!world.isClient && world.getTime() % 20 == 0) {
            SeraphAttributes.refresh(stack);
        }
        // 原版「每天一次判定自动修复」——仍待定（//todo: chance to repair）：
        // if (!world.isClient && isBroken(stack) && world.getTime() % 24000 == 0) { ... }
    }

    // -------------------------------------------------------------- 展示相关 --

    /** SS 级物品自带附魔光效（对应原版 {@code hasEffect}）。 */
    @Override
    public boolean hasGlint(ItemStack stack) {
        // 1.21.1 的 Item#hasGlint 默认就是 stack.hasEnchantments()，
        // 所以这里等价于 1.12.2 的 stack.isItemEnchanted() || seraphRarity == SS
        return super.hasGlint(stack) || seraphRarity == EnumSeraphRarity.SS;
    }

    /**
     * 耐久条是否始终显示（对应 1.12.2 的 {@code showDurabilityBar} 恒返回 {@code true}）。
     *
     * <p>1.21.1 原版默认只在物品已受损时显示（{@code stack.isDamaged()}），
     * 而 1.12.2 的 Seraph 物品<b>满耐久时也显示</b>一条满格耐久条。这里保留该行为。
     */
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getMaxDamage() > 0;
    }

    /**
     * 提示框内容，对应 1.12.2 客户端 {@code DescForSeraph#onDesc}。
     *
     * <p>原版是把各行 {@code strings.add(1, ...)} 插到第 2 行（顺序因此是反的），
     * 这里改为<b>按可读顺序追加在末尾</b>：等级 → 耐久/损坏 → 加速冷却 → 突破 → 强化 → 技能 → 六维属性。
     * 内容与原版一一对应，只有顺序不同。
     */
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        int level = SeraphUtil.getLevel(stack);
        int maxLevel = SeraphUtil.getMaxLevel(stack);
        if (level >= maxLevel) {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.level_format_max", level));
        } else {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.level_format", level,
                    SeraphUtil.getXP(stack), SeraphUtil.getXPForLevel(level)));
        }

        if (isBroken(stack)) {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.broken").formatted(Formatting.RED));
        } else {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.dura_format",
                    stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
        }

        if (fasterCooldown(stack)) {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.faster_cooldown").formatted(Formatting.AQUA));
        }

        tooltip.add(Text.translatable("desc.hbr_mc.seraph.breakthrough",
                SeraphUtil.getBreakThrough(stack), SeraphUtil.getMaxBreakThrough(stack)).formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.translatable("desc.hbr_mc.seraph.boost",
                SeraphUtil.getBoost(stack), SeraphUtil.getMaxBoost(stack)).formatted(Formatting.LIGHT_PURPLE));

        // 技能行：从高槽位往低槽位显示（与原版倒序插入后的视觉效果一致）。
        // 原版在等级数字前用 '+'（每级一个）与 '*'（每 5 级一个）做层数标记。
        for (int slot = getMaxSkillSlot(stack) - 1; slot >= 0; slot--) {
            String key = getTranslationKey() + ".skill." + slot;
            String text = Text.translatable(key, getSkillLeft(stack, slot), getSkillLimit(stack, slot)).getString();
            tooltip.add(Text.literal(decorateSkillLevel(text, SeraphUtil.getSkillLevel(stack, slot))));
        }

        // 六维：对应原版 DescForSeraph#addDesc（固定值 + 百分比）
        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.SIX) {
            tooltip.add(Text.translatable("desc.hbr_mc.seraph.attr_desc",
                    Text.translatable(attribute.value().getTranslationKey()),
                    String.format("%.0f", getAttrValue(stack, attribute)),
                    String.format("%.0f", getAttrValuePercent(stack, attribute) * 100.0F)));
        }
    }

    /** 技能等级标记：每 5 级加一个 {@code *}，余下的每级加一个 {@code +}（对应原版 DescForSeraph 的写法）。 */
    private static String decorateSkillLevel(String text, int level) {
        StringBuilder prefix = new StringBuilder();
        int left = level;
        while (left >= 5) {
            prefix.insert(0, '*');
            left -= 5;
        }
        while (left >= 2) {
            prefix.insert(0, '+');
            left -= 1;
        }
        return prefix + text;
    }

    /**
     * 耐久条的动态颜色（对应 1.12.2 的 {@code getRGBDurabilityForDisplay}）。
     *
     * <p>1.12.2 原实现：
     * <pre>{@code
     * return MathHelper.hsvToRGB(
     *         Math.max(0.0F, (float) (1.5F + getDurabilityForDisplay(stack))) / 3.0F, 1.0F, 1.0F);
     * }</pre>
     * 其中 {@code getDurabilityForDisplay} 返回<b>已损失比例</b>（0.0 = 崭新，1.0 = 完全损坏）。
     * 因此渐变是 <b>崭新=青 → 蓝 → 紫 → 损坏=品红</b>。
     */
    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(durabilityHue(stack), 1.0F, 1.0F);
    }

    /** 耐久条色相，对应原版公式里的 {@code Math.max(0.0F, (1.5F + d)) / 3.0F}。 */
    private static float durabilityHue(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        // 无耐久属性的物品不会显示耐久条，这里给一个安全值避免除零
        float damageFraction = maxDamage <= 0 ? 0.0F : (float) stack.getDamage() / (float) maxDamage;
        return Math.max(0.0F, 1.5F + damageFraction) / 3.0F;
    }

    /**
     * 物品稀有度（对应原版 {@code getForgeRarity} 返回 {@code EnumRarity.EPIC}）。
     *
     * <p>1.21.1 的稀有度是 {@code Item.Settings#rarity(...)} 组件，可在注册时设置；
     * 这里对 SS 级用 EPIC，其余用 RARE。
     */
    public static Rarity rarityFor(EnumSeraphRarity rarity) {
        return rarity == EnumSeraphRarity.SS ? Rarity.EPIC : Rarity.RARE;
    }

    // ------------------------------------------------------------ 命中不掉耐久 --

    /**
     * 命中实体时的钩子（对应 1.12.2 的 {@code hitEntity}）：<b>Seraph 物品不会因为命中实体而损耗耐久</b>。
     *
     * <pre>
     * 1.12.2:  ItemSword#hitEntity 自己调用 stack.damageItem(1, attacker) 再返回 true
     *          → 覆写 hitEntity 且不调用 damageItem、返回 true，就等于关掉了扣耐久
     *
     * 1.21.1:  PlayerEntity#attack
     *            bl6 = ItemStack#postHit(...)          → Item#postHit(...)       // 返回值决定下一步
     *            if (bl6) ItemStack#postDamageEntity(...)
     *                       → Item#postDamageEntity(...)                        // SwordItem 在这里 stack.damage(1)
     *          → 返回 true 反而会<b>启动</b>扣耐久链路，所以这里必须返回 false
     * </pre>
     */
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    /** 命中后处理：刻意留空，确保 Seraph 物品不会因为命中实体而损耗耐久。 */
    @Override
    public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 不扣耐久：此处不调用 stack.damage(...)
    }

    // -------------------------------------------------------------- 右键传送 --

    /**
     * 右键方块：执行 Seraph 传送（对应原版 {@code onItemUse}）。
     *
     * <p>只在服务端真正传送；客户端负责"如何开启传送"的一次性提示。
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.PASS;
        BlockPos clickedPos = context.getBlockPos();
        Direction clickedFace = context.getSide();
        ItemStack stack = context.getStack();

        boolean teleportEnabled = SeraphTeleportWorldStorage.get(player);

        if (world.isClient) {
            // 客户端：给出一次性提示，不改变任何状态
            // （原版还会提示"该表面不完整、需要粗略模式"；本工程已默认允许非完整面，故不再提示）
            if (!teleportEnabled) {
                sendHint(player, "hbr_mc.msg.teleport_command");
            }
            return ActionResult.PASS;
        }

        if (!teleportEnabled) return ActionResult.PASS;

        // 损坏或冷却中：什么都不做
        if (isBroken(stack) || player.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.FAIL;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Vec3d startPos = player.getPos();

        SeraphTeleportControl.Status status = SeraphTeleportControl.trySeraphTeleport(
                serverPlayer, world, clickedPos, clickedFace, true);

        switch (status) {
            case SUCCESS -> {
                return finishTeleport(serverPlayer, world, stack, startPos);
            }
            case PASS, INVALID_FACE -> {
                return ActionResult.PASS;
            }
            case NO_VALID_DESTINATION -> {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_FAIL,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }

    /**
     * 传送成功后的收尾：冷却、按距离扣耐久、传送音效。
     *
     * <p>扣耐久走 {@link #damageDurability(ItemStack, double)}（夹到上限 + 记损坏），
     * 因此与原版一致：<b>不会因为传送把物品打碎</b>。
     */
    private static ActionResult finishTeleport(ServerPlayerEntity player, World world, ItemStack stack,
                                               Vec3d startPos) {
        player.getItemCooldownManager().set((Item) stack.getItem(), TELEPORT_COOLDOWN_TICKS);

        int distance = (int) startPos.distanceTo(player.getPos());
        if (distance > 0) {
            damageDurability(stack, distance);
        }

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        return ActionResult.SUCCESS_NO_ITEM_USED;
    }

    /** 发送"如何开启传送"的一次性提示（原版 {@code need_teleport_hint}，每个会话一次）。 */
    private static void sendHint(PlayerEntity player, String translationKey) {
        if (!needTeleportHint) return;
        needTeleportHint = false;
        player.sendMessage(Text.translatable(translationKey), false);
    }

    // ------------------------------------------------------------ 技能：流程 --

    /**
     * 长按右键使用结束（对应原版 {@code onItemUseFinish}）：潜行与非潜行走不同技能。
     */
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            if (player.isSneaking()) {
                castSkillSneak(stack, world, player);
            } else {
                castSkillNonSneak(stack, world, player);
            }
        }
        return stack;
    }

    /** 触发技能（非潜行）。原版为空实现，供具体武器覆写。 */
    public void castSkillNonSneak(ItemStack stack, World world, PlayerEntity player) {
    }

    /** 触发技能（潜行）。原版为空实现，供具体武器覆写。 */
    public void castSkillSneak(ItemStack stack, World world, PlayerEntity player) {
    }

    /** 上抬使用（对应原版 {@code onPlayerStoppedUsing}）：炮类武器在这里结算连射。 */
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
    }

    /**
     * 对实体右键：按目标类型分派到友方/敌方技能（对应原版 {@code itemInteractionForEntity}）。
     *
     * <p>原版的判定是：{@code EntityPlayer} 或 {@code EntityNPC} → 友方；{@code IMob} 或
     * {@code EntityCancer} → 敌方。1.21.1 里 {@code IMob} 对应 {@code HostileEntity}；
     * 本工程的星癌体目前继承 {@code HostileEntity}，所以两者被同一条件覆盖。
     *
     * <p>与原版一致：冷却中直接返回 {@code PASS}（不触发技能），且无论是否施法成功都返回
     * {@code PASS}（让后续的 {@code use}/{@code useOnBlock} 仍可能生效）。
     */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.PASS;
        }
        World world = user.getWorld();
        if (entity instanceof PlayerEntity) {
            castSkillFriend(stack, world, user, entity);
        } else if (entity instanceof HostileEntity) {
            castSkillEnemy(stack, world, user, entity);
        }
        // 船、羊等既不友方也不敌方的目标：什么都不做（与原版一致）
        return ActionResult.PASS;
    }

    /** 对友方（玩家/NPC）施法。原版默认返回 false，供具体武器覆写。 */
    public boolean castSkillFriend(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        return false;
    }

    /** 对敌方（怪物/星癌体）施法。原版默认返回 false，供具体武器覆写。 */
    public boolean castSkillEnemy(ItemStack stack, World world, PlayerEntity player, LivingEntity target) {
        return false;
    }

    /** 使用时长（原版 {@code getMaxItemUseDuration} = 20）。 */
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 20;
    }

    /** 使用动画（原版 {@code EnumAction.BOW}）。 */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    /**
     * 右键空气：开始"蓄力使用"，技能在 {@link #finishUsing} 里结算。
     *
     * <p>原版还会检查 {@code canUseSkills}（身上没有 SELF_RECOIL 反作用力效果）与冷却。
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!canUseSkills(user) || user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        user.setCurrentHand(hand);
        return TypedActionResult.success(stack);
    }

    /** 能否使用技能：身上没有反作用力效果（对应原版 {@code canUseSkills}）。 */
    public static boolean canUseSkills(LivingEntity entity) {
        return !EffectUtil.has(entity, ModPotion.SELF_RECOIL);
    }

    // ------------------------------------------------------------ 技能：次数 --

    /**
     * 某槽位的可用次数（原版 {@code getSkillLimit}）。
     *
     * <p>返回 {@code -1} 表示"该武器没为这个槽位配置次数"，此时 {@link #canUseSkill} 恒为 false。
     */
    public int getSkillLimit(ItemStack stack, int slot) {
        return -1;
    }

    /** 技能等级（1 起，默认 1）。 */
    public int getSkillLevel(ItemStack stack, int slot) {
        return SeraphUtil.getSkillLevel(stack, slot);
    }

    /** 技能等级上限（恒 10）。 */
    public int getSkillLevelMax(ItemStack stack, int slot) {
        return SeraphUtil.getSkillLevelMax(stack, slot);
    }

    /** 该槽位剩余可用次数 = 上限 - 已用（原版 {@code getSkillLeft}）。 */
    public int getSkillLeft(ItemStack stack, int slot) {
        return getSkillLimit(stack, slot) - SeraphUtil.getSkillUsage(stack, slot);
    }

    /** 该槽位现在能否施放（原版 {@code canUseSkill}）。 */
    public boolean canUseSkill(ItemStack stack, int slot) {
        return getSkillLimit(stack, slot) > 0 && getSkillLeft(stack, slot) > 0;
    }

    /**
     * 可用的技能槽数量（原版 {@code getMaxSkillSlot}）。
     *
     * <p>原版 switch 同样是 SS→2、S→{@code break}→末尾 {@code return 2}、A→1，即 SS=2 / S=2 / A=1。
     */
    public int getMaxSkillSlot(ItemStack stack) {
        switch (seraphRarity) {
            case SS:
                return 2;
            case S:
                break;
            case A:
                return 1;
            default:
                break;
        }
        return 2;
    }

    /** 记录一次技能使用（原版 {@code skillUseMark}）。 */
    public void skillUseMark(ItemStack stack, int slot) {
        SeraphUtil.markSkillUsed(stack, slot);
    }

    /** 设置冷却（原版 {@code setCoolDown}）。 */
    public void setCoolDown(PlayerEntity caster, int ticks) {
        caster.getItemCooldownManager().set(this, ticks);
    }

    /**
     * 技能结算收尾（原版 {@code postCastSkill}）：重置攻击力上升累计、挥手、播音效、计统计、设冷却。
     *
     * <p>与原版一致：创造模式下冷却固定 20 tick；否则按 {@code SP} 秒，若
     * {@link #fasterCooldown} 成立则打 67 折（突破过的 SS 级武器）。
     */
    protected void postCastSkill(PlayerEntity caster, World world, CombatConfig.SkillConf conf, SoundEvent sound) {
        currentBuffRateTotal = 0.0F; // 无论是否用到，都要重置
        caster.swingHand(caster.getActiveHand());
        caster.incrementStat(Stats.USED.getOrCreateStat(this));
        world.playSound(null, caster.getBlockPos(), sound, SoundCategory.PLAYERS, 1.0F, 1.0F);

        ItemStack stack = caster.getMainHandStack();
        int cooldown = conf.cooldownTicks();
        if (!caster.isCreative()) {
            if (fasterCooldown(stack)) {
                cooldown = (int) (cooldown * 0.67F);
            }
        } else {
            cooldown = 20;
        }
        setCoolDown(caster, cooldown);
    }

    /**
     * 是否享受"冷却加速"（原版 {@code fasterCooldown}）：
     * <b>除了 BraveBlue SS 以外</b>的 SS 级武器，只要突破过（{@code breakthrough > 0}）就生效。
     */
    public boolean fasterCooldown(ItemStack stack) {
        return this != ModItems.BRAVE_BLUE_SS
                && seraphRarity == EnumSeraphRarity.SS
                && SeraphUtil.getBreakThrough(stack) > 0;
    }

    /**
     * 消耗身上的攻击力上升效果，折算成技能威力倍率（原版 {@code calcAtkBuffSkill}）。
     *
     * <p>优先级：GREATER（每层 +90%）→ 普通（+65%）→ LESSER（+50%），总共最多消耗 2 层。
     * 结果累加进 {@link #currentBuffRateTotal}，弹射物会在生成时快照它。
     *
     * <p>原版注释里的期望值：普通 90%/65%/50%（山脇），属性 80%（伊达）/55%（Kula），
     * 与这里的三档系数一致。
     */
    protected static void calcAtkBuffSkill(PlayerEntity caster) {
        int maxCost = 2;
        int removed;

        removed = spendBuff(caster, ModPotion.SKILL_ATK_UP_GREATER, maxCost);
        currentBuffRateTotal += removed * 0.90F;

        if (removed < maxCost) {
            maxCost -= removed;
            removed = spendBuff(caster, ModPotion.SKILL_ATK_UP, maxCost);
            currentBuffRateTotal += removed * 0.65F;
            if (removed < maxCost) {
                maxCost -= removed;
                removed = spendBuff(caster, ModPotion.SKILL_ATK_UP_LESSER, maxCost);
                currentBuffRateTotal += removed * 0.50F;
            }
        }
    }

    /**
     * 消耗一层指定 BUFF，返回实际消耗的层数（原版 {@code spendBuff}）。
     *
     * <p>原版写法：{@code getBuffLevelIDL} 拿到的等级是 1 起；先移除，若剩余层数大于本次消耗，
     * 就按 {@code lastBuffLv - 2} 重新施加（这是原版的层数换算，照抄，见 docs/seraph-port.md §12）。
     */
    private static int spendBuff(PlayerEntity caster, net.minecraft.entity.effect.StatusEffect buff, int cost) {
        int lastBuffLevel = EffectUtil.level(caster, buff);
        EffectUtil.remove(caster, buff);
        if (lastBuffLevel > cost) {
            EffectUtil.apply(caster, buff, lastBuffLevel - 2, CombatConfig.BUFF_TIME_SECONDS);
            return cost;
        }
        return lastBuffLevel;
    }

    // -------------------------------------------------------- 创造栏变体 --

    /**
     * 创造栏里的三个条目：常规 / 满强化 / 满突破（对应 1.12.2 {@code getSubItems}）。
     *
     * <p>原版还会调 {@code eraseAttrDesc}（靠 {@code HideFlags} 隐藏属性行）；
     * 1.21.1 的属性行已由 {@link SeraphAttributes} 用 {@code withShowInTooltip(false)} 关掉，无需处理。
     *
     * <p>注意顺序：先写突破再设置等级，等级上限才会跟着变高（{@code setLevel} 会夹到
     * {@code getMaxLevel}）；强化同理要在刷新属性前写入。
     */
    public List<ItemStack> creativeVariants(Item item) {
        List<ItemStack> variants = new ArrayList<>();

        ItemStack plain = new ItemStack(item);
        SeraphAttributes.refresh(plain);
        variants.add(plain);

        // 满技能等级 + 满等级 + 满强化
        ItemStack maxed = plain.copy();
        for (int slot = 0; slot < getMaxSkillSlot(maxed); slot++) {
            SeraphUtil.setSkillLevel(maxed, slot, getSkillLevelMax(maxed, slot));
        }
        SeraphUtil.setLevel(maxed, SeraphUtil.getMaxLevel(maxed));
        SeraphUtil.setBoost(maxed, SeraphBoostConst.getMaxBoost(seraphRarity));
        variants.add(maxed);

        // 再满突破（等级上限随之提高，所以重新拉满一次等级）
        ItemStack maxBreak = maxed.copy();
        SeraphUtil.setBreakThrough(maxBreak, SeraphBoostConst.getMaxBreakThrough(seraphRarity));
        SeraphUtil.setLevel(maxBreak, SeraphUtil.getMaxLevel(maxBreak));
        variants.add(maxBreak);

        return variants;
    }

    /** 供命令/调试打印用的一行摘要。 */
    public String describe(ItemStack stack) {
        return "lv=" + SeraphUtil.getLevel(stack) + "/" + SeraphUtil.getMaxLevel(stack)
                + " xp=" + SeraphUtil.getXP(stack)
                + " boost=" + SeraphUtil.getBoost(stack) + "/" + SeraphUtil.getMaxBoost(stack)
                + " bt=" + SeraphUtil.getBreakThrough(stack) + "/" + SeraphUtil.getMaxBreakThrough(stack)
                + " broken=" + isBroken(stack);
    }
}
