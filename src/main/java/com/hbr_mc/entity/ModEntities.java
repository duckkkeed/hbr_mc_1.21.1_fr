package com.hbr_mc.entity;

import com.hbr_mc.HBR_MC;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/**
 * 本 mod 的实体注册。
 *
 * <p>星癌体各变体（对应 1.12.2 的 {@code entities/cancer}）：
 * <ul>
 *   <li>{@code c_slasher} —— 1.5×3；</li>
 *   <li>{@code c_crest_hopper} —— 2×4；</li>
 *   <li>{@code c_small_hopper} —— 1.8×2；</li>
 *   <li>{@code c_doll} —— 0.8×1.4；</li>
 *   <li>{@code c_marionette} —— 0.6×1.2。</li>
 * </ul>
 *
 * <p>自然生成与刷怪蛋按需求暂不移植；可用 {@code /summon hbr_mc:c_slasher} 等测试。
 * 旧的通用实体 {@code hbr_mc:cancer} 已移除，避免再被当成 Slasher 生成。
 */
public final class ModEntities {

    public static final EntityType<EntitySlasher> C_SLASHER = register("c_slasher",
            EntityType.Builder.create(EntitySlasher::new, SpawnGroup.MONSTER)
                    .dimensions(1.5F, 3.0F)
                    .eyeHeight(1.5F)
                    .maxTrackingRange(64)
                    .build("c_slasher"));

    public static final EntityType<EntityCrestHopper> C_CREST_HOPPER = register("c_crest_hopper",
            EntityType.Builder.create(EntityCrestHopper::new, SpawnGroup.MONSTER)
                    .dimensions(2.0F, 4.0F)
                    .eyeHeight(3.4F)
                    .maxTrackingRange(64)
                    .build("c_crest_hopper"));

    public static final EntityType<EntitySmallHopper> C_SMALL_HOPPER = register("c_small_hopper",
            EntityType.Builder.create(EntitySmallHopper::new, SpawnGroup.MONSTER)
                    .dimensions(1.8F, 2.0F)
                    .eyeHeight(1.7F)
                    .maxTrackingRange(64)
                    .build("c_small_hopper"));

    public static final EntityType<EntityDoll> C_DOLL = register("c_doll",
            EntityType.Builder.create(EntityDoll::new, SpawnGroup.MONSTER)
                    .dimensions(0.8F, 1.4F)
                    .eyeHeight(1.19F)
                    .maxTrackingRange(64)
                    .build("c_doll"));

    public static final EntityType<EntityMarionette> C_MARIONETTE = register("c_marionette",
            EntityType.Builder.create(EntityMarionette::new, SpawnGroup.MONSTER)
                    .dimensions(0.6F, 1.2F)
                    .eyeHeight(1.02F)
                    .maxTrackingRange(64)
                    .build("c_marionette"));

    private ModEntities() {
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        Registry.register(Registries.ENTITY_TYPE, HBR_MC.id(id), type);
        return type;
    }

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        FabricDefaultAttributeRegistry.register(C_SLASHER, EntitySlasher.createSlasherAttributes());
        FabricDefaultAttributeRegistry.register(C_CREST_HOPPER, EntityCrestHopper.createCrestHopperAttributes());
        FabricDefaultAttributeRegistry.register(C_SMALL_HOPPER, EntitySmallHopper.createSmallHopperAttributes());
        FabricDefaultAttributeRegistry.register(C_DOLL, EntityDoll.createDollAttributes());
        FabricDefaultAttributeRegistry.register(C_MARIONETTE, EntityMarionette.createMarionetteAttributes());
    }
}
