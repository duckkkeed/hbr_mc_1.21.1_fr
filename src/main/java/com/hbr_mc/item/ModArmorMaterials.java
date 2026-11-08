package com.hbr_mc.item;

import com.hbr_mc.HBR_MC;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Map;

/**
 * 本 mod 的盔甲材质。
 *
 * <p>1.12.2 用 Forge 的 {@code EnumHelper.addArmorMaterial("cancer", "hbr_mc:cancer", 9999,
 * new int[]{3, 6, 8, 3}, 10, SoundEvents.BLOCK_ANVIL_PLACE, 0)} 注册；1.21.1 需要注册到
 * {@code Registries.ARMOR_MATERIAL}，并用 {@link RegistryEntry} 传给 {@link ArmorItem}。
 *
 * <table>
 *   <caption>防护值与 1.12.2 的对应</caption>
 *   <tr><th>部位</th><th>1.12.2（feet/legs/chest/head = 3/6/8/3）</th></tr>
 *   <tr><td>头盔</td><td>3</td></tr>
 *   <tr><td>胸甲</td><td>8</td></tr>
 *   <tr><td>护腿</td><td>6</td></tr>
 *   <tr><td>靴子</td><td>3</td></tr>
 * </table>
 *
 * <p>其它参数：耐久系数 9999（原版 {@code maxDamageFactor}）、附魔性 10、穿戴音效铁砧、
 * 韧性 0、击退抗性 0。修复材料为空（原版该材质同样没有指定修复物品）。
 *
 * <p><b>未移植</b>：1.12.2 的 {@code ItemArmorCancer} 还会给六维属性（STR/DEX/END/MEN/INT/LUC）
 * 加 14 点、并按随机/开孔系统分配额外属性；这些依赖尚未移植的属性系统与开孔系统。
 */
public final class ModArmorMaterials {

    /** 1.12.2 的 {@code maxDamageFactor = 9999}。 */
    public static final int CANCER_DURABILITY = 9999;

    public static final RegistryKey<ArmorMaterial> CANCER_KEY =
            RegistryKey.of(RegistryKeys.ARMOR_MATERIAL, HBR_MC.id("cancer"));

    public static final ArmorMaterial CANCER = new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.HELMET, 3,
                    ArmorItem.Type.CHESTPLATE, 8,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.BOOTS, 3,
                    // 狼铠用，1.12.2 没有对应值，取铁甲同档。
                    ArmorItem.Type.BODY, 11
            ),
            10,
            RegistryEntry.of(SoundEvents.BLOCK_ANVIL_PLACE),
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(HBR_MC.id("cancer"))),
            0.0F,
            0.0F
    );

    static {
        Registry.register(Registries.ARMOR_MATERIAL, CANCER_KEY.getValue(), CANCER);
    }

    private ModArmorMaterials() {
    }

    /** 取已注册的材质条目，供 {@link ArmorItem} 构造使用。 */
    public static RegistryEntry<ArmorMaterial> cancer() {
        return Registries.ARMOR_MATERIAL.getEntry(CANCER_KEY).orElseThrow();
    }
}
