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
 * <p>目前只有星癌体 {@code hbr_mc:cancer}（1.12.2 的 {@code EntityCancer} 基类）。
 * 自然生成与刷怪蛋按需求暂不移植；可以用 {@code /summon hbr_mc:cancer} 测试。
 */
public final class ModEntities {

    public static final EntityType<EntityCancer> CANCER = register("cancer",
            EntityType.Builder.create(EntityCancer::new, SpawnGroup.MONSTER)
                    .dimensions(0.6F, 1.8F)
                    .maxTrackingRange(64)
                    .build("cancer"));

    private ModEntities() {
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        Registry.register(Registries.ENTITY_TYPE, HBR_MC.id(id), type);
        return type;
    }

    /** 在 {@code HBR_MC#onInitialize} 里调用一次。 */
    public static void register() {
        FabricDefaultAttributeRegistry.register(CANCER, EntityCancer.createCancerAttributes());
    }
}
