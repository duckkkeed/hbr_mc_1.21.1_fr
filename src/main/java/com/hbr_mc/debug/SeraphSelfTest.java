package com.hbr_mc.debug;

import com.hbr_mc.HBR_MC;
import com.mojang.authlib.GameProfile;
import com.hbr_mc.attribute.ModAttributes;
import com.hbr_mc.combat.CombatUtil;
import com.hbr_mc.combat.DpService;
import com.hbr_mc.entity.EntityCancer;
import com.hbr_mc.entity.EntitySmallHopper;
import com.hbr_mc.entity.ModEntities;
import com.hbr_mc.entity.projectile.EntityHBRProjectile;
import com.hbr_mc.item.LotteryItem;
import com.hbr_mc.item.ModItems;
import com.hbr_mc.item.seraph.EnumBoostType;
import com.hbr_mc.item.seraph.EnumSeraphClass;
import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.SeraphAttrData;
import com.hbr_mc.item.seraph.SeraphBoostUnit;
import com.hbr_mc.item.seraph.SeraphUtil;
import com.hbr_mc.potion.EffectUtil;
import com.hbr_mc.potion.ModPotion;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.SpawnSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 开发期自检：用一条命令验证"属性 / 成长 / 技能 / 伤害 / 配方 / 弹丸"整条链路是否真的通。
 *
 * <h2>用法</h2>
 * <pre>
 * Windows:  $env:HBR_MC_SELFTEST="true"; .\gradlew.bat runServer
 * Linux:    HBR_MC_SELFTEST=true ./gradlew runServer
 * </pre>
 * 服务端启动完成后会自动跑一遍检查、把结果打进日志，然后<b>自动关服</b>，
 * 所以不需要手动输入 {@code stop}，也不会留下需要清理的"卡住的服务端"。
 *
 * <p>没有设置环境变量时本类什么都不做（不注册事件、不产生任何运行时开销）。
 *
 * <h2>为什么需要它</h2>
 * 属性在 1.21.1 是"注册表对象 + 物品堆组件"两层结构，任何一层接错都只在特定时机才炸
 * （例如"组件写对了但实体上没有这个属性"）。这些检查把整条链路在无界面环境里跑一遍：
 * <ol>
 *   <li>实体身上确实存在 8 个自定义属性（验证 {@code AttributeContainerMixin}）；</li>
 *   <li>炽天使物品的 {@code ATTRIBUTE_MODIFIERS} 组件条目数与数值正确（验证 {@code SeraphAttributes}）；</li>
 *   <li>把组件修饰符套到实体上后，最终属性值 = 固定值 × (1 + 百分比)（验证整套属性换算）；</li>
 *   <li>等级 / 强化 / 突破 / 耐久的夹断与组件联动；</li>
 *   <li>技能次数与已用次数的换算；</li>
 *   <li>伤害公式真的能扣血（且多段伤害不吃无敌帧）；</li>
 *   <li>7 条炽天使配方都被数据包加载；</li>
 *   <li>弹丸实体能生成、推进、命中并消失。</li>
 * </ol>
 */
public final class SeraphSelfTest {

    /** 环境变量名：设为 {@code true} 时启用自检。 */
    private static final String ENV_FLAG = "HBR_MC_SELFTEST";

    private static final List<String> FAILURES = new ArrayList<>();
    private static int checks = 0;
    /** 测试区域只在第一次计算时打一条日志，避免每次取坐标都刷屏。 */
    private static boolean areaLogged = false;

    private SeraphSelfTest() {
    }

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        if (!"true".equalsIgnoreCase(System.getenv(ENV_FLAG))) {
            return;
        }
        HBR_MC.LOGGER.info("[自测] 检测到 {}，服务端启动后将执行 Seraph 自检", ENV_FLAG);
        ServerLifecycleEvents.SERVER_STARTED.register(SeraphSelfTest::run);
    }

    private static void run(MinecraftServer server) {
        HBR_MC.LOGGER.info("[自测] ===== Seraph 自检开始 =====");
        try {
            checkRegistry();
            checkAttributesOnEntity(server);
            checkItemAttributes(server);
            checkGrowth(server);
            checkSkills();
            checkDamage(server);
            checkRecipes(server);
            checkProjectile(server);
            checkDp(server);
            checkCancerSixAttr(server);
            checkCancerDrop(server);
            checkCancerSpawns(server);
            checkRealSpawner(server);
            checkCancerAttack(server);
        } catch (Throwable t) {
            FAILURES.add("自检过程抛异常: " + t);
            HBR_MC.LOGGER.error("[自测] 自检过程抛异常", t);
        } finally {
            HBR_MC.LOGGER.info("[自测] ===== 结束：{} 项检查，{} 项失败 =====", checks, FAILURES.size());
            for (String failure : FAILURES) {
                HBR_MC.LOGGER.error("[自测] FAIL: {}", failure);
            }
            if (FAILURES.isEmpty()) {
                HBR_MC.LOGGER.info("[自测] RESULT OK: Seraph 属性/成长/技能/伤害/配方/弹丸 链路全部通过");
            } else {
                HBR_MC.LOGGER.error("[自测] RESULT FAILED");
            }
            server.stop(false);
        }
    }

    // ------------------------------------------------------------- 各项检查 --

    private static void checkRegistry() {
        int size = ItemSeraphBase.registeredItems().size();
        check("已登记炽天使物品 = 21（实际 " + size + "）", size == 21);
        check("抽卡池 A=" + LotteryItem.aPoolSize() + " SS=" + LotteryItem.ssPoolSize(),
                LotteryItem.aPoolSize() > 0 && LotteryItem.ssPoolSize() > 0);

        // 刷怪蛋：5 个星癌体变体都能从刷怪蛋反查到实体类型
        Item[] eggs = {
                ModItems.C_SLASHER_SPAWN_EGG, ModItems.C_CREST_HOPPER_SPAWN_EGG,
                ModItems.C_SMALL_HOPPER_SPAWN_EGG, ModItems.C_DOLL_SPAWN_EGG,
                ModItems.C_MARIONETTE_SPAWN_EGG};
        int eggCount = 0;
        for (Item egg : eggs) {
            if (egg instanceof SpawnEggItem) {
                eggCount++;
            }
        }
        check("刷怪蛋 5/5 已注册为 SpawnEggItem（实际 " + eggCount + "）", eggCount == 5);
        check("刷怪蛋能反查到实体类型",
                SpawnEggItem.forEntity(ModEntities.C_SLASHER) == ModItems.C_SLASHER_SPAWN_EGG
                        && SpawnEggItem.forEntity(ModEntities.C_CREST_HOPPER) == ModItems.C_CREST_HOPPER_SPAWN_EGG
                        && SpawnEggItem.forEntity(ModEntities.C_SMALL_HOPPER) == ModItems.C_SMALL_HOPPER_SPAWN_EGG
                        && SpawnEggItem.forEntity(ModEntities.C_DOLL) == ModItems.C_DOLL_SPAWN_EGG
                        && SpawnEggItem.forEntity(ModEntities.C_MARIONETTE) == ModItems.C_MARIONETTE_SPAWN_EGG);
    }

    /** 验证 mixin：任意生物身上都能取到 8 个自定义属性实例。 */
    private static void checkAttributesOnEntity(MinecraftServer server) {
        ZombieEntity zombie = spawnZombie(server, testPos(server, 0.0D));
        int found = 0;
        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.all()) {
            if (zombie.getAttributeInstance(attribute) != null) {
                found++;
            }
        }
        check("生物身上存在 " + found + "/8 个自定义属性", found == 8);
        zombie.discard();
    }

    /** 验证物品组件：条目数、以及"固定值 × (1 + 百分比)"的最终属性。 */
    private static void checkItemAttributes(MinecraftServer server) {
        ItemStack stack = new ItemStack(ModItems.BRAVE_BLUE);
        SeraphUtil.setLevel(stack, 10);
        SeraphUtil.setBoost(stack, 18);
        SeraphUtil.setBreakThrough(stack, 5);

        AttributeModifiersComponent component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        check("ATTRIBUTE_MODIFIERS 组件存在", component != null);
        if (component == null) {
            return;
        }
        // 2 条（攻击力/攻速）+ 6 属性 × 2 条（固定值/百分比）
        check("属性修饰符条目数 = 14（实际 " + component.modifiers().size() + "）",
                component.modifiers().size() == 14);
        check("属性行不在原版提示框里显示", !component.showInTooltip());

        ZombieEntity zombie = spawnZombie(server, testPos(server, 0.0D));
        stack.applyAttributeModifiers(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            var instance = zombie.getAttributeInstance(attribute);
            if (instance != null) {
                instance.addTemporaryModifier(modifier);
            }
        });

        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.SIX) {
            double expectedFixed = expectedFixed(stack, attribute);
            double expectedPercent = expectedPercent(stack, attribute);
            double expected = expectedFixed * (1.0D + expectedPercent);
            double actual = ModAttributes.get(zombie, attribute);
            check("属性 " + shortName(attribute) + " 期望 " + round(expected) + " 实际 " + round(actual),
                    Math.abs(expected - actual) < 1.0E-4D);
        }
        zombie.discard();
    }

    private static void checkGrowth(MinecraftServer server) {
        ItemStack stack = new ItemStack(ModItems.BRAVE_BLUE);
        SeraphUtil.setLevel(stack, 10);
        int expectedMax = ((ItemSeraphBase) ModItems.BRAVE_BLUE).maxDamageForLevel(10);
        check("耐久上限随等级写入（" + stack.getMaxDamage() + " == " + expectedMax + "）",
                stack.getMaxDamage() == expectedMax);

        // 强化上限夹断
        SeraphUtil.setBoost(stack, 999);
        check("强化夹到上限 18（实际 " + SeraphUtil.getBoost(stack) + "）", SeraphUtil.getBoost(stack) == 18);
        // 突破上限夹断
        SeraphUtil.setBreakThrough(stack, 999);
        check("突破夹到上限 20（实际 " + SeraphUtil.getBreakThrough(stack) + "）",
                SeraphUtil.getBreakThrough(stack) == 20);

        // 耐久夹断 + 损坏标记 + 修复
        ItemStack durability = new ItemStack(ModItems.BRAVE_BLUE);
        SeraphUtil.setLevel(durability, 1);
        int max = durability.getMaxDamage();
        ItemSeraphBase.damageDurability(durability, max);
        check("耐久到上限不判定损坏", !ItemSeraphBase.isBroken(durability));
        ItemSeraphBase.damageDurability(durability, 1);
        check("再超 1 点判定损坏且耐久夹在上限",
                ItemSeraphBase.isBroken(durability) && durability.getDamage() == max);
        ItemSeraphBase.repairFully(durability);
        check("完全修复后标记清除", !ItemSeraphBase.isBroken(durability) && durability.getDamage() == 0);
    }

    private static void checkSkills() {
        ItemStack stack = new ItemStack(ModItems.BRAVE_BLUE_SS);
        SeraphUtil.setSkillLevel(stack, 0, 3);
        SeraphUtil.setSkillLevel(stack, 1, 2);
        ItemSeraphBase seraph = (ItemSeraphBase) stack.getItem();

        // 槽 0：5 + clamp(3-1,0,5) = 7；槽 1：2 + clamp(2-1,0,2) = 3
        check("修复槽次数 = 7（实际 " + seraph.getSkillLimit(stack, 0) + "）", seraph.getSkillLimit(stack, 0) == 7);
        check("终极槽次数 = 3（实际 " + seraph.getSkillLimit(stack, 1) + "）", seraph.getSkillLimit(stack, 1) == 3);

        seraph.skillUseMark(stack, 0);
        seraph.skillUseMark(stack, 0);
        check("用掉 2 次后剩余 = 5（实际 " + seraph.getSkillLeft(stack, 0) + "）",
                seraph.getSkillLeft(stack, 0) == 5);
        SeraphUtil.resetSkillUsage(stack);
        check("次数清零后剩余 = 7（实际 " + seraph.getSkillLeft(stack, 0) + "）",
                seraph.getSkillLeft(stack, 0) == 7);

        // 无技能配置的槽位：-1 表示不可用
        ItemStack plain = new ItemStack(ModItems.RAPID_FIRE_S);
        ItemSeraphBase plainSeraph = (ItemSeraphBase) plain.getItem();
        check("未配置次数的槽位不可施放（limit=" + plainSeraph.getSkillLimit(plain, 0) + "）",
                !plainSeraph.canUseSkill(plain, 0));
        check("S 级可显示 2 个技能槽（实际 " + plainSeraph.getMaxSkillSlot(plain) + "）",
                plainSeraph.getMaxSkillSlot(plain) == 2);
    }

    private static void checkDamage(MinecraftServer server) {
        ZombieEntity attacker = spawnZombie(server, testPos(server, 0.0D));
        ZombieEntity target = spawnZombie(server, testPos(server, 2.0D));

        attacker.getAttributeInstance(ModAttributes.STR).setBaseValue(500);
        attacker.getAttributeInstance(ModAttributes.DEX).setBaseValue(500);
        attacker.getAttributeInstance(ModAttributes.LUC).setBaseValue(0);
        target.getAttributeInstance(ModAttributes.MEN).setBaseValue(100);
        target.getAttributeInstance(ModAttributes.END).setBaseValue(100);
        target.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH)
                .setBaseValue(1000);
        target.setHealth(1000);

        float before = target.getHealth();
        CombatUtil.attackAsHBR(attacker, target, CombatUtil.EnumAttrType.STR_FOCUS, CombatUtil.EnumDefType.END,
                100.0F, 120.0F);
        float after = target.getHealth();
        check("伤害结算扣血（" + round(before) + " -> " + round(after) + "）", after < before);

        // 多段：连续两次都应生效（原版会把无敌帧清零）
        float afterFirst = after;
        CombatUtil.attackAsHBR(attacker, target, CombatUtil.EnumAttrType.STR_FOCUS, CombatUtil.EnumDefType.END,
                100.0F, 120.0F);
        check("第二段仍然生效（" + round(afterFirst) + " -> " + round(target.getHealth()) + "）",
                target.getHealth() < afterFirst);

        attacker.discard();
        target.discard();
    }

    private static void checkRecipes(MinecraftServer server) {
        String[] ids = {"seraph_xp_book", "seraph_boost_shell", "seraph_boost_material",
                "seraph_breakthrough_same", "seraph_breakthrough_shard", "seraph_rank_up", "seraph_decomp"};
        int loaded = 0;
        for (String id : ids) {
            if (server.getRecipeManager().get(HBR_MC.id(id)).isPresent()) {
                loaded++;
            }
        }
        check("炽天使配方 " + loaded + "/7 已加载", loaded == ids.length);
    }

    private static void checkProjectile(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        // 用统一的安全高度与出生点附近的位置放测试实体：写死 y 或只取一列地表都可能落在
        // 山体里，弹丸第一 tick 就会被"撞方块"判定吃掉（自测里踩过两次）。
        double groundY = safeY(server);
        double baseX = baseX(server);
        double baseZ = baseZ(server);
        ZombieEntity shooter = spawnZombie(server, new Vec3d(baseX, groundY, baseZ));
        ZombieEntity target = spawnZombie(server, new Vec3d(baseX + 3.0D, groundY, baseZ));

        EntityHBRProjectile bullet = ModEntities.HBR_PROJECTILE.create(world);
        check("弹丸实体可创建", bullet != null);
        if (bullet == null) {
            shooter.discard();
            target.discard();
            return;
        }
        bullet.refreshPositionAndAngles(baseX, groundY + 1.0, baseZ, 0.0F, 0.0F);
        bullet.setVelocity(new Vec3d(1.0, 0.0, 0.0));
        bullet.minPotency = 20.0F;
        bullet.cap = 150.0F;
        bullet.shooter = shooter;
        world.spawnEntity(bullet);

        float healthBefore = target.getHealth();
        for (int i = 0; i < 40 && !bullet.isRemoved(); i++) {
            bullet.tick();
        }
        check("弹丸在 40 tick 内消失", bullet.isRemoved());
        check("弹丸命中目标并造成伤害（" + round(healthBefore) + " -> " + round(target.getHealth()) + "）",
                target.getHealth() < healthBefore);
        shooter.discard();
        target.discard();
    }

    /**
     * DP 系统：星癌体的壳（吸收值 = DP）、破壳规则、破壳反馈、DP 服务扣减与损失同步。
     *
     * <p>玩家侧的"武器耐久当 DP"走 {@code SeraphDamageAbsorb}，需要真实玩家才能触发，
     * 无头服务端测不了（进游戏后用 {@code /seraph} 或挨打即可验证）。
     */
    private static void checkDp(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        EntitySmallHopper cancer = ModEntities.C_SMALL_HOPPER.create(world);
        check("星癌体实体可创建", cancer != null);
        if (cancer == null) {
            return;
        }
        Vec3d pos = testPos(server, 6.0D);
        cancer.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        world.spawnEntity(cancer);
        cancer.initialize(world, world.getLocalDifficulty(cancer.getBlockPos()),
                net.minecraft.entity.SpawnReason.COMMAND, null);

        check("壳 = DP，上限 20（实际 " + DpService.dpMax(cancer) + "）", DpService.dpMax(cancer) == 20.0F);
        check("壳 = DP，当前 20（实际 " + DpService.dp(cancer) + "）", DpService.dp(cancer) == 20.0F);

        ZombieEntity mob = spawnZombie(server, testPos(server, 8.0D));
        check("普通来源不能破壳", !DpService.canBreak(cancer.getDamageSources().mobAttack(mob)));
        check("虚空伤害可以破壳", DpService.canBreak(cancer.getDamageSources().outOfWorld()));

        boolean immune = cancer.isInvulnerableTo(cancer.getDamageSources().mobAttack(mob));
        check("壳未破时免疫普通来源", immune);

        float healthBefore = cancer.getHealth();
        boolean damaged = cancer.damage(cancer.getDamageSources().mobAttack(mob), 5.0F);
        check("普通来源被完全挡下（不扣壳不扣血）",
                !damaged && cancer.getHealth() == healthBefore && cancer.getAbsorptionAmount() == 20.0F);

        boolean broken = cancer.damage(cancer.getDamageSources().outOfWorld(), 25.0F);
        check("可破壳来源打穿壳（吸收归零）", broken && cancer.getAbsorptionAmount() == 0.0F);
        check("破壳后获得反作用力", EffectUtil.level(cancer, ModPotion.SELF_RECOIL) > 0);

        // DP 服务：扣减、剩余、损失同步
        cancer.setDpMax(20.0F);
        float absorbed = DpService.damageDp(cancer, 7.0F);
        check("damageDp 吸收 7（实际 " + absorbed + "）", Math.abs(absorbed - 7.0F) < 1.0E-4F);
        check("扣减后 DP = 13（实际 " + DpService.dp(cancer) + "）", Math.abs(DpService.dp(cancer) - 13.0F) < 1.0E-4F);
        check("DP_LOSS_SYNC = 7（实际 " + ModAttributes.get(cancer, ModAttributes.DP_LOSS_SYNC) + "）",
                Math.abs(ModAttributes.get(cancer, ModAttributes.DP_LOSS_SYNC) - 7.0D) < 1.0E-4D);

        // 壳破之后的普通伤害可以正常掉血（先把壳清掉、并清掉受击无敌帧）
        cancer.setDpMax(0.0F);
        cancer.timeUntilRegen = 0;
        float hpAfterBreak = cancer.getHealth();
        boolean hitAfterBreak = cancer.damage(cancer.getDamageSources().mobAttack(mob), 3.0F);
        check("壳破后普通伤害正常结算", hitAfterBreak && cancer.getHealth() < hpAfterBreak);

        mob.discard();
        cancer.discard();
    }

    /**
     * 验证星癌体六维（1.12.2 的 {@code set6Attr}）：五个变体的六项都等于各自的值。
     *
     * <p>同时验证 {@code AttributeContainerMixin} 没有把默认容器里的基值盖成 0
     * （mixin 会跳过 fallback 里已有的属性）。
     */
    private static void checkCancerSixAttr(MinecraftServer server) {
        checkCancerSix(server, "c_slasher", ModEntities.C_SLASHER, 50.0D);
        checkCancerSix(server, "c_crest_hopper", ModEntities.C_CREST_HOPPER, 40.0D);
        checkCancerSix(server, "c_small_hopper", ModEntities.C_SMALL_HOPPER, 20.0D);
        checkCancerSix(server, "c_doll", ModEntities.C_DOLL, 10.0D);
        checkCancerSix(server, "c_marionette", ModEntities.C_MARIONETTE, 3.0D);
    }

    private static void checkCancerSix(MinecraftServer server, String name,
                                       EntityType<? extends EntityCancer> type, double expected) {
        EntityCancer cancer = type.create(server.getOverworld());
        if (cancer == null) {
            check(name + " 六维：实体可创建", false);
            return;
        }
        StringBuilder actual = new StringBuilder();
        boolean ok = true;
        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.SIX) {
            double value = ModAttributes.get(cancer, attribute);
            ok &= Math.abs(value - expected) < 1.0E-4D;
            if (actual.length() > 0) {
                actual.append(' ');
            }
            actual.append(shortName(attribute)).append('=').append(Math.round(value));
        }
        check(name + " 六维全为 " + (int) expected + "（实际 " + actual + "）", ok);
        cancer.discard();
    }

    /**
     * 验证"掉落按 STR 生成"这条链路：STR=250 时癌壳应至少 2 个（原版 {@code advancedDrop}：整数部分必掉）。
     *
     * <p>顺带验证 {@code EntityCancer#getLootTableId()} 覆写成 {@code minecraft:empty} 后
     * 不会走数据包战利品表、也不会崩。
     */
    private static void checkCancerDrop(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        Vec3d pos = testPos(server, 10.0D);
        Box area = Box.of(pos, 8.0D, 8.0D, 8.0D);

        // 清掉测试区里残留的掉落物，避免"新掉落合并进旧堆"导致计数偏小
        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, area, item -> true)) {
            item.discard();
        }

        EntityCancer cancer = ModEntities.C_SLASHER.create(world);
        if (cancer == null) {
            check("星癌体掉落：实体可创建", false);
            return;
        }
        cancer.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        world.spawnEntity(cancer);
        cancer.initialize(world, world.getLocalDifficulty(cancer.getBlockPos()),
                net.minecraft.entity.SpawnReason.COMMAND, null);
        var str = cancer.getAttributeInstance(ModAttributes.STR);
        if (str == null) {
            check("星癌体掉落：STR 属性实例存在", false);
            cancer.discard();
            return;
        }
        str.setBaseValue(250.0D);

        // 实际 STR 可能已经被群系加成（雪地/沙漠）；掉率 = STR/100，整数部分必掉、余数按概率
        int guaranteed = (int) (ModAttributes.get(cancer, ModAttributes.STR) / 100.0D);
        cancer.damage(cancer.getDamageSources().outOfWorld(), 1000.0F);

        int shellCount = world.getEntitiesByClass(ItemEntity.class, area,
                        item -> item.getStack().isOf(ModItems.CANCER_SHELL))
                .stream().mapToInt(item -> item.getStack().getCount()).sum();
        int extraCount = world.getEntitiesByClass(ItemEntity.class, area,
                        item -> item.getStack().isOf(ModItems.BOOST_1_B))
                .stream().mapToInt(item -> item.getStack().getCount()).sum();
        check("STR=" + (int) ModAttributes.get(cancer, ModAttributes.STR) + " 掉癌壳 ≥ " + guaranteed
                + "（实际 " + shellCount + "）", shellCount >= guaranteed);
        check("STR=" + (int) ModAttributes.get(cancer, ModAttributes.STR) + " 掉诺瓦原水 B ≥ " + guaranteed
                + "（实际 " + extraCount + "）", extraCount >= guaranteed);
        check("单次掉落不超过整数部分 +1（" + shellCount + " / " + guaranteed + "）", shellCount <= guaranteed + 1);

        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, area, item -> true)) {
            item.discard();
        }
        cancer.discard();
    }

    /**
     * 验证近战走的是六维公式（{@code CombatUtil.attackAsHBR}）而不是原版攻击力直扣：
     * STR/DEX 拉到很低时几乎打不动（原版 20 攻击力会直接打死 20 血的僵尸），拉高后一击必杀。
     */
    private static void checkCancerAttack(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        EntityCancer slasher = ModEntities.C_SLASHER.create(world);
        ZombieEntity zombie = spawnZombie(server, testPos(server, 14.0D));
        if (slasher == null) {
            check("星癌体近战：实体可创建", false);
            zombie.discard();
            return;
        }
        Vec3d pos = testPos(server, 12.0D);
        slasher.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        world.spawnEntity(slasher);

        setAttr(slasher, ModAttributes.STR, -100.0D);
        setAttr(slasher, ModAttributes.DEX, -100.0D);
        setAttr(slasher, ModAttributes.LUC, 0.0D);
        boolean hit = slasher.tryAttack(zombie);
        check("六维极低时仍算命中", hit);
        check("六维极低时几乎打不动（实际 HP=" + round(zombie.getHealth()) + "）", zombie.getHealth() > 19.0D);

        setAttr(slasher, ModAttributes.STR, 500.0D);
        setAttr(slasher, ModAttributes.DEX, 500.0D);
        slasher.tryAttack(zombie);
        check("六维拉满时一击必杀", zombie.isDead() || zombie.getHealth() <= 0.0F);

        slasher.discard();
        zombie.discard();
    }

    private static void setAttr(LivingEntity entity, RegistryEntry<EntityAttribute> attribute, double value) {
        var instance = entity.getAttributeInstance(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }


    /**
     * 验证自然生成注册（对应 1.12.2 {@code RegisterSpawn}）：
     * 五条生成项都在主世界群系的 MONSTER 池里、每次 1~3 只，落点规则是 {@code ON_GROUND}；
     * 并且"头顶不见天"的位置会被 {@link EntityCancer#canSpawn} 拒绝。
     */
    private static void checkCancerSpawns(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        List<SpawnSettings.SpawnEntry> entries = world.getBiome(world.getSpawnPos()).value()
                .getSpawnSettings().getSpawnEntries(SpawnGroup.MONSTER).getEntries();

        checkCancerSpawnEntry("c_slasher", ModEntities.C_SLASHER, entries);
        checkCancerSpawnEntry("c_crest_hopper", ModEntities.C_CREST_HOPPER, entries);
        checkCancerSpawnEntry("c_small_hopper", ModEntities.C_SMALL_HOPPER, entries);
        checkCancerSpawnEntry("c_doll", ModEntities.C_DOLL, entries);
        checkCancerSpawnEntry("c_marionette", ModEntities.C_MARIONETTE, entries);

        check("星癌体落点规则 = ON_GROUND",
                SpawnRestriction.getLocation(ModEntities.C_SLASHER) == SpawnLocationTypes.ON_GROUND);
        check("星癌体水下不窒息（can_breathe_under_water 标签）",
                ModEntities.C_SLASHER.isIn(EntityTypeTags.CAN_BREATHE_UNDER_WATER));

        BlockPos spawn = world.getSpawnPos();
        BlockPos underground = new BlockPos(spawn.getX(),
                world.getTopY(Heightmap.Type.WORLD_SURFACE, spawn.getX(), spawn.getZ()) - 10, spawn.getZ());
        check("头顶不见天的位置不能自然生成",
                !EntityCancer.canSpawn(ModEntities.C_SLASHER, world, net.minecraft.entity.SpawnReason.NATURAL,
                        underground, world.getRandom()));

        // 1.12.2 的星癌体没有继承 EntityMob，因此没有"怪物只在暗处生成"的光照检查：
        // 正午露天也必须允许生成（作为对照，同一位置的原版僵尸会被拦住）。
        // 用一块临时泥土在露天空域构造落点，避免出生点地形（水面/台阶/树叶）影响判定。
        BlockPos probe = new BlockPos(spawn.getX(), 300, spawn.getZ());
        BlockState oldState = world.getBlockState(probe);
        world.setBlockState(probe, Blocks.DIRT.getDefaultState());
        long timeBefore = world.getTimeOfDay();
        world.setTimeOfDay(6000L); // 正午
        BlockPos feet = probe.up();
        boolean cancerAtNoon = EntityCancer.canSpawn(ModEntities.C_SLASHER, world,
                net.minecraft.entity.SpawnReason.NATURAL, feet, world.getRandom());
        boolean zombieAtNoon = HostileEntity.canSpawnInDark(EntityType.ZOMBIE, world,
                net.minecraft.entity.SpawnReason.NATURAL, feet, world.getRandom());
        world.setTimeOfDay(timeBefore);
        world.setBlockState(probe, oldState);

        check("正午露天允许星癌体自然生成", cancerAtNoon);
        check("对照：同一位置原版僵尸正午被光照拦住", !zombieAtNoon);

        // 在真实地形上扫一圈，逐项检查 spawner 的门槛（正午）：
        // 脚下方块允许生成 → ON_GROUND 落点 → 空间足够 → 本 mod 的谓词
        long noon = world.getTimeOfDay();
        world.setTimeOfDay(6000L);
        int candidates = 0;
        int allowed = 0;
        int space = 0;
        int predicate = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int x = spawn.getX() + dx * 6;
                int z = spawn.getZ() + dz * 6;
                int top = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                for (int y = top; y > top - 4; y--) {
                    BlockPos feetPos = new BlockPos(x, y, z);
                    if (!world.getBlockState(feetPos.down())
                            .allowsSpawning(world, feetPos.down(), ModEntities.C_SLASHER)) {
                        continue;
                    }
                    if (!world.getBlockState(feetPos).isAir() || !world.getBlockState(feetPos.up()).isAir()) {
                        continue;
                    }
                    candidates++;
                    if (!SpawnRestriction.isSpawnPosAllowed(ModEntities.C_SLASHER, world, feetPos)) {
                        continue;
                    }
                    allowed++;
                    if (!world.isSpaceEmpty(ModEntities.C_SLASHER
                            .getSpawnBox(feetPos.getX() + 0.5, feetPos.getY(), feetPos.getZ() + 0.5))) {
                        continue;
                    }
                    space++;
                    if (EntityCancer.canSpawn(ModEntities.C_SLASHER, world,
                            net.minecraft.entity.SpawnReason.NATURAL, feetPos, world.getRandom())) {
                        predicate++;
                        if (predicate == 1) {
                            HBR_MC.LOGGER.info("[自测] 首个可通过的落点：{}（下方 {}）",
                                    feetPos, world.getBlockState(feetPos.down()));
                        }
                    }
                }
            }
        }
        world.setTimeOfDay(noon);
        HBR_MC.LOGGER.info("[自测] 真实地表正午：候选 {}，落点允许 {}，空间 {}，谓词通过 {}",
                candidates, allowed, space, predicate);
        check("真实地表正午存在可通过的生成点（谓词通过 " + predicate + "）", predicate > 0);
    }

    /**
     * 用假玩家 + 真实 {@code SpawnHelper} 走一遍自然生成流程（正午），确认星癌体真的能刷出来。
     *
     * <p>这条检查是踩坑后加的：只验证 {@code SpawnRestriction} 谓词是不够的 ——
     * {@code PathAwareEntity#canSpawn} 还会用 {@code getPathfindingFavor} 做第二道趋光判断，
     * 白天（亮度 1.0 → favor -0.5）会直接拦掉。1.21.1 的 {@code HostileEntity} 把"趋暗"写在这里，
     * 而 1.12.2 的星癌体继承 {@code EntityCreature} 时拿到的 {@code getBlockPathWeight} 是 0。
     */
    private static void checkRealSpawner(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        BlockPos spawn = world.getSpawnPos();
        BlockPos surface = new BlockPos(spawn.getX() + 40,
                world.getTopY(Heightmap.Type.WORLD_SURFACE, spawn.getX() + 40, spawn.getZ() + 40), spawn.getZ() + 40);
        // 假玩家：SpawnHelper 需要"24 格外有玩家"才允许生成
        GameProfile profile = new GameProfile(UUID.randomUUID(), "spawn_probe");
        ServerPlayerEntity fake = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
        fake.refreshPositionAndAngles(surface.getX() + 20, surface.getY(), surface.getZ(), 0.0F, 0.0F);
        world.getPlayers().add(fake);
        long timeBefore = world.getTimeOfDay();
        world.setTimeOfDay(6000L); // 正午
        try {
            for (int i = 0; i < 300; i++) {
                net.minecraft.world.SpawnHelper.spawnEntitiesInChunk(SpawnGroup.MONSTER, world, surface);
            }
        } finally {
            world.setTimeOfDay(timeBefore);
            world.getPlayers().remove(fake);
        }
        Box box = Box.of(new Vec3d(surface.getX() + 0.5, surface.getY() + 0.5, surface.getZ() + 0.5), 48.0, 48.0, 48.0);
        List<EntityCancer> spawned = world.getEntitiesByClass(EntityCancer.class, box, entity -> true);
        HBR_MC.LOGGER.info("[自测] 真实刷怪流程（正午）生成 {} 只星癌体", spawned.size());
        check("正午用真实 SpawnHelper 能刷出星癌体（实际 " + spawned.size() + " 只）", !spawned.isEmpty());
        for (EntityCancer cancer : spawned) {
            cancer.discard();
        }
    }

    private static <T extends EntityCancer> void checkCancerSpawnEntry(String name, EntityType<T> type,
                                                                       List<SpawnSettings.SpawnEntry> entries) {
        boolean found = entries.stream().anyMatch(entry -> entry.type == type
                && entry.minGroupSize == 1
                && entry.maxGroupSize == 3
                && entry.getWeight().getValue() >= 1);
        check(name + " 已加入主世界 MONSTER 生成表（1~3 只）", found);
    }

    /** 测试实体的安全落点：世界出生点附近（保证区块已加载）、地表以上 6 格。 */
    private static Vec3d testPos(MinecraftServer server, double dx) {
        return new Vec3d(baseX(server) + dx, safeY(server), baseZ(server));
    }

    private static double baseX(MinecraftServer server) {
        return server.getOverworld().getSpawnPos().getX() + 0.5D;
    }

    private static double baseZ(MinecraftServer server) {
        return server.getOverworld().getSpawnPos().getZ() + 0.5D;
    }

    /**
     * 测试用安全高度。
     *
     * <p>两个坑：①写死 y（如 100）可能正好在山体里；②只在 (0,0) 取地表时，如果该区块没被加载，
     * {@code getTopY} 会返回世界底部，实体就生成在虚空/地下——所以这里取<b>世界出生点</b>附近
     * 一段走廊的最高地表，并用出生点 Y 兜底（自测里踩过两次）。
     */
    private static double safeY(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        net.minecraft.util.math.BlockPos spawn = world.getSpawnPos();
        int max = world.getBottomY();
        for (int dx = 0; dx <= 12; dx++) {
            max = Math.max(max, world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,
                    spawn.getX() + dx, spawn.getZ()));
        }
        double y = Math.max(max + 6.0D, spawn.getY() + 8.0D);
        if (!areaLogged) {
            areaLogged = true;
            HBR_MC.LOGGER.info("[自测] 测试区域：spawn={} 最高地表={} 使用 y={}", spawn, max, y);
        }
        return y;
    }

    private static ZombieEntity spawnZombie(MinecraftServer server, Vec3d pos) {        ServerWorld world = server.getOverworld();
        ZombieEntity zombie = EntityType.ZOMBIE.create(world);
        if (zombie == null) {
            throw new IllegalStateException("无法创建测试用僵尸");
        }
        zombie.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        zombie.setPersistent();
        world.spawnEntity(zombie);
        return zombie;
    }

    /** 独立复算"固定值"部分（不调用被测方法，避免自证）。 */
    private static double expectedFixed(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        ItemSeraphBase seraph = (ItemSeraphBase) stack.getItem();
        EnumSeraphRarity rarity = seraph.getSeraphRarity();
        double value = SeraphUtil.getLevel(stack) + 7;
        // 原版 switch 缺 break：SS 再吃一次 S 的 +5
        if (rarity == EnumSeraphRarity.SS) {
            value += 15;
        }
        if (rarity == EnumSeraphRarity.SS || rarity == EnumSeraphRarity.S) {
            value += 5;
        }
        int boost = SeraphUtil.getBoost(stack);
        EnumSeraphClass seraphClass = seraph.getSeraphClass();
        for (int i = 0; i < boost; i++) {
            SeraphBoostUnit unit = SeraphAttrData.boostUnitAt(seraphClass, i);
            if (unit == null) {
                break;
            }
            if (unit.attr() == attribute || unit.attr() == null) {
                if (unit.type() == EnumBoostType.FIXED) {
                    value += (int) unit.value();
                }
            }
        }
        return value;
    }

    /** 独立复算"百分比"部分。 */
    private static double expectedPercent(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
        ItemSeraphBase seraph = (ItemSeraphBase) stack.getItem();
        Float base = SeraphAttrData.getData(seraph.getSeraphRarity(), seraph.getSeraphClass()).get(attribute);
        double value = base == null ? 0.0D : base;
        int boost = SeraphUtil.getBoost(stack);
        for (int i = 0; i < boost; i++) {
            SeraphBoostUnit unit = SeraphAttrData.boostUnitAt(seraph.getSeraphClass(), i);
            if (unit == null) {
                break;
            }
            if (unit.attr() == attribute || unit.attr() == null) {
                if (unit.type() == EnumBoostType.PERCENT) {
                    value += unit.value();
                }
            }
        }
        value += com.hbr_mc.item.seraph.SeraphBoostConst.attrPercentagePerBreak(seraph.getSeraphRarity())
                * SeraphUtil.getBreakThrough(stack);
        return value;
    }

    private static String shortName(RegistryEntry<EntityAttribute> attribute) {
        return attribute.value().getTranslationKey().replace("attribute.hbr_mc.", "");
    }

    private static String round(double value) {
        return String.format("%.3f", value);
    }

    private static void check(String name, boolean ok) {
        checks++;
        if (ok) {
            HBR_MC.LOGGER.info("[自测] OK   {}", name);
        } else {
            FAILURES.add(name);
            HBR_MC.LOGGER.error("[自测] FAIL {}", name);
        }
    }

    /** 供 {@code /seraph} 调试时打印属性组件用。 */
    public static String describeComponent(ItemStack stack) {
        AttributeModifiersComponent component = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (component == null) {
            return "无属性组件";
        }
        StringBuilder sb = new StringBuilder();
        for (AttributeModifiersComponent.Entry entry : component.modifiers()) {
            sb.append(shortName(entry.attribute())).append(' ')
                    .append(entry.modifier().operation()).append(' ')
                    .append(round(entry.modifier().value())).append("; ");
        }
        return sb.toString();
    }
}
