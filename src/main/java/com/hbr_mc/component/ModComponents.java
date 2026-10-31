package com.hbr_mc.component;

import com.hbr_mc.HBR_MC;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * 本 mod 的自定义数据组件（Data Component）。
 *
 * <p>1.21.1 用组件取代了 1.12.2 的任意 NBT。这里用强类型组件承载 Seraph 物品的成长数据，
 * 而不是塞进 {@code CUSTOM_DATA}：
 * <ul>
 *   <li>不会污染物品的通用 NBT（{@code custom_data} 会参与物品比较，且 {@code /data} 可见）；</li>
 *   <li>序列化与网络同步都走官方组件通道（{@code codec} + {@code packetCodec}）；</li>
 *   <li>值语义明确，不需要每次自己解析 NBT。</li>
 * </ul>
 *
 * <p>注册时机：在 {@code HBR_MC#onInitialize} 里、注册物品之前调用 {@link #register()}，
 * 让类初始化（静态字段即完成注册）尽早发生。
 */
public final class ModComponents {

    /**
     * Seraph 物品的"已损坏"标记，对应 1.12.2 的 NBT 键 {@code is_broken}。
     *
     * <p>只要这个组件存在且为 {@code true}，物品即视为损坏（不能传送 / 不能攻击，但仍能吸收伤害，
     * 与 1.12.2 一致）。清除它即可"复活"物品。
     */
    public static final ComponentType<Boolean> SERAPH_BROKEN = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            HBR_MC.id("seraph_broken"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL)
                    .build()
    );

    /**
     * 等级，对应 1.12.2 的 NBT 键 {@code lv_idl}。
     *
     * <p>组件缺失时按 1 级处理（见 {@code SeraphUtil#getLevel}），范围由稀有度与突破次数决定。
     */
    public static final ComponentType<Integer> SERAPH_LEVEL = intComponent("seraph_level");

    /** 当前等级内的经验，对应 1.12.2 的 {@code lv_xp}；缺失时按 0 处理。 */
    public static final ComponentType<Integer> SERAPH_XP = intComponent("seraph_xp");

    /** 强化次数，对应 1.12.2 的 {@code boost_lvl}；缺失时按 0 处理。 */
    public static final ComponentType<Integer> SERAPH_BOOST = intComponent("seraph_boost");

    /** 突破次数，对应 1.12.2 的 {@code brk_thru}；缺失时按 0 处理。 */
    public static final ComponentType<Integer> SERAPH_BREAKTHROUGH = intComponent("seraph_breakthrough");

    private ModComponents() {
    }

    private static ComponentType<Integer> intComponent(String path) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                HBR_MC.id(path),
                ComponentType.<Integer>builder()
                        .codec(Codec.INT)
                        .packetCodec(PacketCodecs.VAR_INT)
                        .build()
        );
    }

    /**
     * 触发本类初始化，从而完成上面静态字段的注册。
     *
     * <p>注册本身在类加载时完成，这个方法只是让调用点显式化、并保证顺序可控。
     */
    public static void register() {
    }
}
