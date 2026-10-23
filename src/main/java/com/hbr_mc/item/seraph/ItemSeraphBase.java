package com.hbr_mc.item.seraph;

import com.hbr_mc.potion.ModPotion;
import com.hbr_mc.util.SeraphTeleportControl;
import com.hbr_mc.util.SeraphTeleportWorldStorage;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.UseAction;
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
 * <p>本类已移植原版中<b>不依赖缺失子系统</b>的部分：右键传送、冷却、耐久消耗、
 * 稀有度、发光、技能调用流程（骨架）。原版里依赖 {@code SeraphUtil} / {@code IDLNBTUtil} /
 * {@code ItemLottery} / 属性与 Boost 表的内容见类尾的「未移植清单」。
 *
 * <h2>稀有度</h2>
 * 稀有度按<b>物品实例</b>保存（原版也是实例字段 {@code seraphRarity}，但外面还有一份 static 字段，
 * 本移植只保留实例这一份）。实例会在构造时自动登记，客户端据此按稀有度批量设置提示框配色
 * （见 {@code com.hbr_mc.client.tooltip.SeraphTooltipStyles}）。
 *
 * <h2>右键传送（对应原版 {@code onItemUse}）</h2>
 * <ul>
 *   <li>需要先用 {@code /seraphtp}（别名 {@code /srtp}、{@code /toggletp}）打开该玩家的传送开关，
 *       开关按玩家存在世界存档里；未开启时第一次右键会给出提示（原版 {@code need_teleport_hint}）。</li>
 *   <li>耐久已损坏（{@code damage >= maxDamage}）或在冷却中时不传送。</li>
 *   <li>传送方向与落点规则见 {@link SeraphTeleportControl}。</li>
 *   <li>成功后：5 tick 冷却、按移动距离扣耐久、播放末影果传送音效；失败播放发射器失败音效。</li>
 * </ul>
 *
 * <h2>未移植清单（原版有、这里没有，需要先补齐对应子系统）</h2>
 * <ol>
 *   <li>{@code getMaxDamage}：1.21.1 耐久由 {@code Item.Settings#maxDamage} 固定，不能按 NBT 等级动态计算；
 *       这里改用 {@code SeraphItemSettings} 按稀有度给出近似值。</li>
 *   <li>{@code getAttributeModifiers} / {@code getAttrValue} / {@code getAttrValuePercent}：依赖
 *       {@code SeraphUtil}、{@code SeraphAttrData}、{@code SeraphBoostUnit}、{@code RegisterAttr} 等属性系统，全部缺失。</li>
 *   <li>{@code castSkillSneak/NonSneak/Friend/Enemy}、{@code getSkillLimit/Level/Left}、{@code skillUseMark}、
 *       {@code calcAtkBuffSkill}：依赖技能与 Buff 系统（原 {@code IDLNBTUtil} + {@code RegisterEffects} + {@code ModConfig.COMBAT}）。</li>
 *   <li>{@code onHit(LivingHurtEvent)}（手持时把伤害转为耐久消耗）：需要 1.21.1 的伤害事件/混合注入。</li>
 *   <li>{@code setDamage}：1.21.1 的 {@code ItemStack#setDamage} 不能再覆写；"损坏"状态改用
 *       {@link ItemStack#getDamage()} 与 {@link ItemStack#getMaxDamage()} 判定（见 {@link #isBroken(ItemStack)}）。</li>
 *   <li>{@code getSubItems}（创造栏里的满级/满突破变体）：依赖 Boost/突破系统。</li>
 * </ol>
 */
public class ItemSeraphBase extends Item {

    /** 已构造的全部 Seraph 物品（物品在注册表里是单例，构造即注册）。 */
    private static final List<ItemSeraphBase> REGISTERED = new ArrayList<>();

    /** 传送成功后的冷却 tick 数（原版 {@code setCooldown(this, 5)}）。 */
    protected static final int TELEPORT_COOLDOWN_TICKS = 5;

    /** 每个会话只提示一次：如何打开传送开关（原版 {@code need_teleport_hint}）。 */
    private static boolean needTeleportHint = true;

    private final EnumSeraphRarity seraphRarity;
    /** 战斗职业（原版 {@code seraphClass}）；属性系统移植后才会真正使用。 */
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

    /** 全部已登记的 Seraph 物品（只读）。 */
    public static List<ItemSeraphBase> registeredItems() {
        return Collections.unmodifiableList(REGISTERED);
    }

    /** 物品是否已损坏（原版 {@code SeraphUtil.isBroken} 的等价实现）。 */
    public static boolean isBroken(ItemStack stack) {
        return stack.getDamage() >= stack.getMaxDamage();
    }

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
     *
     * <p>加了 {@code getMaxDamage() > 0} 的保护：没有耐久属性的物品若返回 true，
     * 原版 {@code getItemBarStep} 里的除法会得到 NaN，画出一条无意义的空槽。
     */
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getMaxDamage() > 0;
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
     *
     * <p>1.21.1 的对应钩子是 {@link Item#getItemBarColor(ItemStack)}（原版实现为
     * {@code hsvToRgb(剩余比例 / 3)}，即绿→红）。这里把"剩余比例"换算回"已损失比例"后
     * 套用原公式，得到<b>崭新=青 → 蓝 → 紫 → 损坏=品红</b>的渐变：
     *
     * <table>
     *   <caption>渐变取样</caption>
     *   <tr><th>已损失</th><th>色相</th><th>颜色</th></tr>
     *   <tr><td>0%</td><td>0.500</td><td>青 <code>#00FFFF</code></td></tr>
     *   <tr><td>25%</td><td>0.583</td><td>蓝青 <code>#0080FF</code></td></tr>
     *   <tr><td>50%</td><td>0.667</td><td>蓝 <code>#0000FF</code></td></tr>
     *   <tr><td>75%</td><td>0.750</td><td>紫 <code>#8000FF</code></td></tr>
     *   <tr><td>100%</td><td>0.833</td><td>品红 <code>#FF00FF</code></td></tr>
     * </table>
     *
     * <p>想改回原版 1.12.2/1.21.1 的绿→红，只需把 {@link #durabilityHue} 里的
     * {@code 1.5F + damageFraction} 换成 {@code 1.0F - damageFraction}。
     *
     * <p>条的长度仍沿用原版 {@link Item#getItemBarStep(ItemStack)}（按剩余耐久 13 格等分），
     * 这里只改颜色。
     */
    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(durabilityHue(stack), 1.0F, 1.0F);
    }

    /**
     * 耐久条色相，对应原版公式里的 {@code Math.max(0.0F, (1.5F + d)) / 3.0F}，
     * 其中 {@code d} 为已损失耐久比例（0.0 = 崭新，1.0 = 完全损坏）。
     */
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
     * 这里对 SS 级用 EPIC，其余沿用当前设置（默认 COMMON）。
     */
    public static Rarity rarityFor(EnumSeraphRarity rarity) {
        return rarity == EnumSeraphRarity.SS ? Rarity.EPIC : Rarity.RARE;
    }

    // ------------------------------------------------------------ 命中不掉耐久 --

    /**
     * 命中实体时的钩子（对应 1.12.2 的 {@code hitEntity}）：<b>Seraph 物品不会因为命中实体而损耗耐久</b>。
     *
     * <p>1.12.2 与 1.21.1 的机制不同，不能照抄"返回 true"：
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
     *
     * <p>本方法返回 {@code false}：不触发后续 {@link #postDamageEntity}，因此不会扣耐久。
     * 若你希望命中计入原版的"已使用"统计（{@code Stats.USED}），可以改成返回 {@code true}
     * —— 因为下面的 {@link #postDamageEntity} 已被固定为空实现，即使链路启动也不会掉耐久。
     */
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    /**
     * 命中后处理：刻意留空，确保 Seraph 物品不会因为命中实体而损耗耐久。
     *
     * <p>原版 {@code Item} 基类本就为空，真正扣耐久的是 {@code SwordItem#postDamageEntity}
     * （内调 {@code stack.damage(1, attacker, EquipmentSlot.MAINHAND)}）。
     * 这里显式声明空实现，作为"即使将来 {@link #postHit} 改成返回 true 也不掉耐久"的保险。
     *
     * <p>注意：这与原版 1.12.2 {@code hitEntity} 注释里的 "don't drop durability" 是同一意图。
     */
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

        // 传送前的玩家位置，用于结算移动距离
        Vec3d startPos = player.getPos();

        SeraphTeleportControl.Status status = SeraphTeleportControl.trySeraphTeleport(
                serverPlayer, world, clickedPos, clickedFace, true);

        switch (status) {
            case SUCCESS -> {
                // 1.12.2 的 stack.damageItem(distance, player) 会作用在"正在使用的那只手"上的物品；
                // 1.21.1 需要显式给出栏位（用于破碎提示/统计），因此按 context 的手映射
                EquipmentSlot slot = context.getHand() == Hand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                return finishTeleport(serverPlayer, world, stack, startPos, slot);
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

    /** 传送成功后的收尾：冷却、按距离扣耐久、传送音效。 */
    private static ActionResult finishTeleport(ServerPlayerEntity player, World world, ItemStack stack,
                                               Vec3d startPos, EquipmentSlot slot) {
        player.getItemCooldownManager().set((Item) stack.getItem(), TELEPORT_COOLDOWN_TICKS);

        int distance = (int) startPos.distanceTo(player.getPos());
        if (distance > 0) {
            stack.damage(distance, player, slot);
        }

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        // 对应原版 EnumActionResult.SUCCESS：服务端完成动作、物品未被消耗
        return ActionResult.SUCCESS_NO_ITEM_USED;
    }

    /**
     * 发送"如何开启传送"的一次性提示（原版 {@code need_teleport_hint}，每个会话一次）。
     */
    private static void sendHint(PlayerEntity player, String translationKey) {
        if (!needTeleportHint) return;
        needTeleportHint = false;
        player.sendMessage(Text.translatable(translationKey), false);
    }

    // ------------------------------------------------------ 技能调用流程（骨架） --

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

    /** 使用时长（原版 20 tick）。 */
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
     * <p>原版还会检查 {@code canUseSkills}（身上没有 SELF_RECOIL 反作用力效果）。
     */
    @Override
    public net.minecraft.util.TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!canUseSkills(user) || user.getItemCooldownManager().isCoolingDown(this)) {
            return net.minecraft.util.TypedActionResult.fail(stack);
        }
        user.setCurrentHand(hand);
        return net.minecraft.util.TypedActionResult.success(stack);
    }

    /** 能否使用技能：身上没有反作用力效果（对应原版 {@code canUseSkills}）。 */
    public static boolean canUseSkills(LivingEntity entity) {
        RegistryEntry<StatusEffect> selfRecoil = Registries.STATUS_EFFECT.getEntry(ModPotion.SELF_RECOIL);
        return selfRecoil == null || !entity.hasStatusEffect(selfRecoil);
    }

    // ------------------------------------------------------------------ 未实现 --

    // TODO(移植阶段二)：以下原版能力需要先补齐对应子系统，目前刻意不提供空实现以免误用。
    //
    //  · 属性/Boost：getAttributeModifiers、getAttrValue、getAttrValuePercent、addToMap
    //      依赖 SeraphUtil(evel/breakThrough)、SeraphAttrData、SeraphBoostUnit、RegisterAttr、IDLNBTDef
    //  · 技能数值：getSkillLimit / getSkillLevel / getSkillLevelMax / getSkillLeft / canUseSkill /
    //      getMaxSkillSlot / skillUseMark（依赖 NBT 技能等级与使用次数）
    //  · 友方/敌方交互：itemInteractionForEntity → castSkillFriend / castSkillEnemy（1.21.1 对应
    //      Item#useOnEntity，且需要 EntityPlayer/EntityNPC/IMob/EntityCancer 的等价判定）
    //  · 受击转耐久：onHit(LivingHurtEvent)（1.21.1 需要伤害事件或 Mixin）
    //  · 创造栏变体：getSubItems 的"满级/满突破"条目（依赖 Boost 与突破系统）
    //  · 冷却加速：fasterCooldown（依赖 BreakThrough 与具体物品 RegisterItem.BRAVE_BLUE_SS）
}
