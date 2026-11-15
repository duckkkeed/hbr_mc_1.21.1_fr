package com.hbr_mc.attribute;

import com.hbr_mc.HBR_MC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * 本 mod 的六维属性与 DP 属性，对应 1.12.2 的 {@code init/RegisterAttr}。
 *
 * <h2>与 1.12.2 的对应</h2>
 * <table>
 *   <caption>属性对照</caption>
 *   <tr><th>1.12.2 字段</th><th>本工程</th><th>含义</th></tr>
 *   <tr><td>{@code STR}</td><td>{@link #STR}</td><td>力量（物理攻击）</td></tr>
 *   <tr><td>{@code DEX}</td><td>{@link #DEX}</td><td>灵巧（射击攻击）</td></tr>
 *   <tr><td>{@code END}</td><td>{@link #END}</td><td>耐久（防御侧，与 MEN 一起决定减伤）</td></tr>
 *   <tr><td>{@code MEN}</td><td>{@link #MEN}</td><td>精神（防御侧，与 END 一起决定减伤）</td></tr>
 *   <tr><td>{@code INT}</td><td>{@link #INT}</td><td>智力（治疗量、控制成功率）</td></tr>
 *   <tr><td>{@code LUC}</td><td>{@link #LUC}</td><td>幸运（暴击率、控制成功率）</td></tr>
 *   <tr><td>{@code DP_MAX}</td><td>{@link #DP_MAX}</td><td>DP 上限（护盾式耐久条，HUD 未移植）</td></tr>
 *   <tr><td>{@code DP_LOSS_SYNC}</td><td>{@link #DP_LOSS_SYNC}</td><td>DP 已损失量（同步用）</td></tr>
 * </table>
 *
 * <h2>为什么要 mixin</h2>
 * 1.21.1 的属性是<b>注册表对象</b>（{@code Registries.ATTRIBUTE}），且每个实体的属性实例来自
 * {@code DefaultAttributeRegistry} 里为该实体类型登记的那一份；Fabric 没有"给所有生物追加一个属性"的 API
 * （{@code FabricDefaultAttributeRegistry} 只能整体替换某个实体类型的属性表）。
 * 因此这里用 {@code AttributeContainerMixin} 在 {@code AttributeContainer} 构造时把这 8 个属性追加进去，
 * 语义等价于 1.12.2 在 {@code EntityConstructing} 事件里对每个 {@code EntityLivingBase} 调用
 * {@code getAttributeMap().registerAttribute(attr)}。
 *
 * <p>属性基值都是 0（原版同样用 {@code getNewAttrNonPercentNonNegative(name, 0)}），
 * 实际数值来自武器（{@code ItemSeraphBase} 写入的 {@code ATTRIBUTE_MODIFIERS} 组件），
 * 装备/手持时由原版 {@code LivingEntity} 的属性系统自动叠加。
 */
public final class ModAttributes {

    /** 属性的允许范围，与 1.12.2 的 {@code MIN = -9999999} / {@code MAX = 999999} 一致。 */
    public static final double MIN_VALUE = -9999999.0D;
    public static final double MAX_VALUE = 999999.0D;

    public static final RegistryEntry<EntityAttribute> STR = register("str", "力量");
    public static final RegistryEntry<EntityAttribute> DEX = register("dex", "灵巧");
    public static final RegistryEntry<EntityAttribute> END = register("end", "耐久");
    public static final RegistryEntry<EntityAttribute> MEN = register("men", "精神");
    public static final RegistryEntry<EntityAttribute> INT = register("int", "智力");
    public static final RegistryEntry<EntityAttribute> LUC = register("luc", "幸运");
    public static final RegistryEntry<EntityAttribute> DP_MAX = register("dpmax", "DP 上限");
    /** 这是"已损失量"而不是当前值（原版注释：{@code This is the loss, not the current value.}）。 */
    public static final RegistryEntry<EntityAttribute> DP_LOSS_SYNC = register("dpsync", "DP 损失");

    /** 六维（不含 DP），提示框与循环里按这个顺序显示。 */
    public static final RegistryEntry<EntityAttribute>[] SIX = newArray();

    private ModAttributes() {
    }

    @SuppressWarnings("unchecked")
    private static RegistryEntry<EntityAttribute>[] newArray() {
        return new RegistryEntry[]{STR, DEX, END, MEN, INT, LUC};
    }

    private static RegistryEntry<EntityAttribute> register(String path, String cnName) {
        EntityAttribute attribute = new ClampedEntityAttribute(
                "attribute.hbr_mc." + path, 0.0D, MIN_VALUE, MAX_VALUE)
                .setTracked(false)
                .setCategory(EntityAttribute.Category.POSITIVE);
        return Registry.registerReference(Registries.ATTRIBUTE, HBR_MC.id(path), attribute);
    }

    /** 触发类初始化（属性在类加载时注册）。在 {@code HBR_MC#onInitialize} 里尽早调用。 */
    public static void register() {
    }

    /** 全部属性（供 {@code AttributeContainerMixin} 追加）。 */
    public static RegistryEntry<EntityAttribute>[] all() {
        return new RegistryEntry[]{STR, DEX, END, MEN, INT, LUC, DP_MAX, DP_LOSS_SYNC};
    }

    /** 属性提示框里用的显示名（对应原版 {@code "attribute.name." + attr.getName()}）。 */
    public static String translationKey(RegistryEntry<EntityAttribute> attribute) {
        return attribute.value().getTranslationKey();
    }

    /**
     * 读取某实体的属性数值。
     *
     * <p>与原版一致：实体没有这个属性（例如属性系统被其它 mod 换掉）时返回 0，调用方不必判空。
     */
    public static double get(LivingEntity entity, RegistryEntry<EntityAttribute> attribute) {
        if (entity == null) {
            return 0.0D;
        }
        var instance = entity.getAttributeInstance(attribute);
        return instance == null ? 0.0D : instance.getValue();
    }

    /** 同 {@link #get}，转成 float（战斗公式里用 float）。 */
    public static float getFloat(LivingEntity entity, RegistryEntry<EntityAttribute> attribute) {
        return (float) get(entity, attribute);
    }

    /** 调试用：把六维格式化成一行。 */
    public static String describe(LivingEntity entity) {
        StringBuilder sb = new StringBuilder();
        for (RegistryEntry<EntityAttribute> attribute : SIX) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(attribute.value().getTranslationKey().replace("attribute.hbr_mc.", ""))
                    .append('=')
                    .append(Math.round(get(entity, attribute)));
        }
        return sb.toString();
    }
}
