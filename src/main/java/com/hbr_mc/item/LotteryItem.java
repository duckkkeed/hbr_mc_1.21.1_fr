package com.hbr_mc.item;

import com.hbr_mc.item.seraph.EnumSeraphRarity;
import com.hbr_mc.item.seraph.ItemSeraphBase;
import com.hbr_mc.item.seraph.SeraphAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * 「电子军人手册」抽卡券，移植自 1.12.2 的 {@code items/ItemLottery}。
 *
 * <h2>规则（与原版一致）</h2>
 * <ul>
 *   <li>右键消耗一张，按 {@link #SS_CHANCE}（原版 {@code ModConfig.CONFIG.SS_CHANCE = 0.1}）判定稀有度；</li>
 *   <li>命中 SS：发两张音效 + 屏幕中央标题（文本键 {@code item.hbr_mc.<id>.toast}）；</li>
 *   <li>否则给一把 A 级；两种结果都会在聊天栏以斜体播报武器名；</li>
 *   <li>候选池由 {@link ItemSeraphBase} 构造时自动登记（对应原版 {@code ItemLottery.addA/addSS}），
 *       所以新增炽天使不需要改本类。</li>
 * </ul>
 *
 * <h2>与 1.12.2 的差异</h2>
 * <ul>
 *   <li>原版用 {@code addItemStackToInventory} 并忽略返回值；这里在背包满时把武器掉在脚下，
 *       避免抽到的东西凭空消失（抽出物是贵重物品，丢失比原版更糟）。</li>
 *   <li>创造模式不消耗抽卡券（原版会消耗）。</li>
 *   <li>原版的 {@code eraseAttrDesc}（隐藏属性行）在 1.21.1 由
 *       {@link SeraphAttributes#refresh} 写入的组件天然满足，无需单独处理。</li>
 * </ul>
 */
public class LotteryItem extends Item {

    /** 抽出 SS 的概率（原版默认配置 0.1）。 */
    public static final float SS_CHANCE = 0.1F;

    private static final List<Item> A_ITEMS = new ArrayList<>();
    private static final List<Item> SS_ITEMS = new ArrayList<>();

    public LotteryItem(Settings settings) {
        super(settings);
    }

    /** 由 {@link ItemSeraphBase} 的构造器调用：把新武器加进抽卡池（只收 A 与 SS）。 */
    public static void registerSeraph(Item item) {
        if (!(item instanceof ItemSeraphBase seraph)) {
            return;
        }
        switch (seraph.getSeraphRarity()) {
            case SS -> SS_ITEMS.add(item);
            case A -> A_ITEMS.add(item);
            case S -> {
                // 原版抽卡池里没有 S 级（S 只是 A→SS 的中间形态），保持一致
            }
        }
    }

    /** 抽卡池里的 A 级数量（调试/自检用）。 */
    public static int aPoolSize() {
        return A_ITEMS.size();
    }

    /** 抽卡池里的 SS 级数量（调试/自检用）。 */
    public static int ssPoolSize() {
        return SS_ITEMS.size();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (A_ITEMS.isEmpty() || SS_ITEMS.isEmpty()) {
            // 抽卡池没货（理论上不会发生）：不当场消耗
            return TypedActionResult.fail(stack);
        }

        boolean isSS = world.getRandom().nextFloat() < SS_CHANCE;
        Item result = isSS
                ? SS_ITEMS.get(world.getRandom().nextInt(SS_ITEMS.size()))
                : A_ITEMS.get(world.getRandom().nextInt(A_ITEMS.size()));

        ItemStack loot = new ItemStack(result);
        SeraphAttributes.refresh(loot);

        if (isSS) {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS, 1.5F, 1.0F);
            world.playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundCategory.PLAYERS, 1.5F, 1.0F);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Text title = Text.translatable(result.getTranslationKey() + ".toast")
                        .formatted(Formatting.LIGHT_PURPLE);
                serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(title));
            }
        } else {
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        player.sendMessage(loot.getName().copy().formatted(Formatting.ITALIC), false);
        if (!player.getInventory().insertStack(loot)) {
            player.dropItem(loot, false);
        }
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.hbr_mc.lottery.pool",
                A_ITEMS.size(), SS_ITEMS.size()).formatted(Formatting.GRAY));
    }
}
