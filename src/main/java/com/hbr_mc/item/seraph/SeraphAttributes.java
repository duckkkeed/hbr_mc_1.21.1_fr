package com.hbr_mc.item.seraph;

import com.hbr_mc.HBR_MC;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * 把炽天使的成长数据换算成原版属性修饰符，对应 1.12.2 {@code ItemSeraphBase#getAttributeModifiers}。
 *
 * <h2>为什么不是覆写方法</h2>
 * 1.12.2 覆写 {@code getAttributeModifiers(slot, stack)}，每次读取时<b>实时计算</b>，所以等级/强化/突破一变就生效。
 * 1.21.1 的属性修饰符是物品堆上的 <b>组件</b>（{@code DataComponentTypes.ATTRIBUTE_MODIFIERS}）：
 * {@code ItemStack#applyAttributeModifiers} 只读组件，{@code Item} 没有"按堆计算"的覆写点。
 *
 * <p>所以这里采取"<b>数据变化时重写组件</b>"的做法：
 * <ul>
 *   <li>{@link SeraphUtil#setLevel} / {@code setBoost} / {@code setBreakThrough} / {@code copyProgression}
 *       改完数据后调用 {@link #refresh(ItemStack)}；</li>
 *   <li>创造栏的满级条目、配方产物在生成时调用一次；</li>
 *   <li>旧存档/外部生成的物品由 {@link ItemSeraphBase#inventoryTick} 兜底刷新（一秒一次，且只在内容不同才写）。</li>
 * </ul>
 *
 * <h2>修饰符对照</h2>
 * <table>
 *   <caption>1.12.2 → 1.21.1</caption>
 *   <tr><th>1.12.2</th><th>本工程</th></tr>
 *   <tr><td>攻击力 {@code level + 3}（op 0）</td><td>{@code GENERIC_ATTACK_DAMAGE}，{@code ADD_VALUE}</td></tr>
 *   <tr><td>攻击速度 {@code -2.4}（op 0）</td><td>{@code GENERIC_ATTACK_SPEED}，{@code ADD_VALUE}</td></tr>
 *   <tr><td>六维固定值（op 0）</td><td>对应属性，{@code ADD_VALUE}</td></tr>
 *   <tr><td>六维百分比（op 1，即"乘以 1+x"）</td><td>对应属性，{@code ADD_MULTIPLIED_TOTAL}</td></tr>
 * </table>
 *
 * <p>原版用 {@code HideFlags} 隐藏属性行、再由 {@code DescForSeraph} 自己画属性说明；
 * 这里用 {@code withShowInTooltip(false)} 达到同样效果，提示框里的属性行由
 * {@link ItemSeraphBase#appendTooltip} 输出。
 */
public final class SeraphAttributes {

    /** 固定值修饰符的 id（对应原版 {@code SERAPH_MODIFIER_BASE}）。 */
    private static final Identifier BASE_ID = HBR_MC.id("seraph/base");
    /** 百分比修饰符的 id（对应原版第二个 UUID {@code uuidPer}）。 */
    private static final Identifier PERCENT_ID = HBR_MC.id("seraph/percent");

    private SeraphAttributes() {
    }

    /**
     * 重写物品堆上的属性修饰符组件。
     *
     * <p>只有内容确实不同时才写，避免每个 tick 都触发组件同步。
     */
    public static void refresh(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSeraphBase item) || stack.isEmpty()) {
            return;
        }
        AttributeModifiersComponent wanted = build(item, stack);
        AttributeModifiersComponent current = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (Objects.equals(current, wanted)) {
            return;
        }
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, wanted);
    }

    /** 按当前成长数据生成一份属性修饰符组件（不写回物品堆）。 */
    public static AttributeModifiersComponent build(ItemSeraphBase item, ItemStack stack) {
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();

        // 攻击力 / 攻击速度：与原版一致，攻击力随等级成长
        builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                new EntityAttributeModifier(BASE_ID, SeraphUtil.getLevel(stack) + 3.0D,
                        EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);
        builder.add(EntityAttributes.GENERIC_ATTACK_SPEED,
                new EntityAttributeModifier(BASE_ID, -2.4000000953674316D,
                        EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND);

        for (RegistryEntry<EntityAttribute> attribute : com.hbr_mc.attribute.ModAttributes.SIX) {
            builder.add(attribute,
                    new EntityAttributeModifier(BASE_ID, item.getAttrValue(stack, attribute),
                            EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND);
            builder.add(attribute,
                    new EntityAttributeModifier(PERCENT_ID, item.getAttrValuePercent(stack, attribute),
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    AttributeModifierSlot.MAINHAND);
        }

        // 原版靠 HideFlags 隐藏，这里关掉原版属性行，改由 appendTooltip 自己输出
        return builder.build().withShowInTooltip(false);
    }
}
