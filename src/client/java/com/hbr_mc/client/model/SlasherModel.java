package com.hbr_mc.client.model;

import com.hbr_mc.entity.EntityCancer;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/**
 * 星癌体的 Slasher 模型，对应 1.12.2 的 {@code client/models/entities/ModelSlasher}（Blockbench 导出）。
 *
 * <p>1.12 的 {@code ModelRenderer} 树直接翻译成 1.21.1 的 {@link ModelPartData}：
 * 旋转点/立方体坐标不变，{@code rotateAngleX/Y/Z} 对应 {@code pitch/yaw/roll}；
 * 贴图 64×64，贴图文件在 {@code textures/entity/cancer/slasher.png}。
 *
 * <p>动画也和原版一致：中心球跟随头部朝向，整体轻微左右摇摆，
 * 内环缓慢旋转，攻击时内外环一起摆动。
 */
public class SlasherModel<T extends EntityCancer> extends SinglePartEntityModel<T> {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart outerRingRotate;
    private final ModelPart innerRingRotate;
    private final ModelPart centerBallRotate;

    public SlasherModel(ModelPart root) {
        this.root = root;
        this.base = root.getChild("base");
        this.outerRingRotate = base.getChild("outerRingRotate");
        this.innerRingRotate = base.getChild("innerRingRotate");
        this.centerBallRotate = base.getChild("centerBallRotate");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData base = root.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        // ---- 外环 ----
        ModelPartData outerRingRotate = base.addChild("outerRingRotate", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartData outerRing = outerRingRotate.addChild("outerRing", ModelPartBuilder.create(),
                ModelTransform.rotation(0.0F, 0.0F, 0.7854F));

        ModelPartData thick2 = outerRing.addChild("thick2", ModelPartBuilder.create()
                .uv(0, 16).cuboid(-7.0F, 6.0F, -1.0F, 13.0F, 1.0F, 2.0F)
                .uv(0, 13).cuboid(-7.0F, -7.0F, -1.0F, 13.0F, 1.0F, 2.0F), ModelTransform.NONE);
        thick2.addChild("innerRingV3", ModelPartBuilder.create()
                .uv(0, 19).cuboid(-6.0F, 8.0F, -1.0F, 12.0F, 1.0F, 2.0F)
                .uv(0, 10).cuboid(-7.0F, -5.0F, -1.0F, 14.0F, 1.0F, 2.0F),
                ModelTransform.of(2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        ModelPartData thin2 = outerRing.addChild("thin2", ModelPartBuilder.create()
                .uv(0, 26).cuboid(-5.5F, 5.5F, -1.0F, 11.0F, 2.0F, 1.0F)
                .uv(25, 25).cuboid(-5.5F, -7.5F, -1.0F, 11.0F, 2.0F, 1.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.5F));
        thin2.addChild("innerRingV4", ModelPartBuilder.create()
                .uv(0, 7).cuboid(-7.5F, 5.5F, -1.0F, 15.0F, 2.0F, 1.0F)
                .uv(0, 4).cuboid(-7.5F, -7.5F, -1.0F, 15.0F, 2.0F, 1.0F),
                ModelTransform.rotation(0.0F, 0.0F, 1.5708F));

        ModelPartData blade = outerRing.addChild("blade", ModelPartBuilder.create()
                .uv(0, 24).cuboid(-6.5F, 6.5F, -1.0F, 13.0F, 2.0F, 0.0F)
                .uv(0, 22).cuboid(-6.5F, -8.5F, -1.0F, 13.0F, 2.0F, 0.0F),
                ModelTransform.pivot(0.0F, 0.0F, 1.0F));
        blade.addChild("innerRingV5", ModelPartBuilder.create()
                .uv(0, 2).cuboid(-8.5F, 6.5F, -1.0F, 17.0F, 2.0F, 0.0F)
                .uv(0, 0).cuboid(-8.5F, -8.5F, -1.0F, 17.0F, 2.0F, 0.0F),
                ModelTransform.rotation(0.0F, 0.0F, 1.5708F));

        // ---- 内环 ----
        ModelPartData innerRingRotate = base.addChild("innerRingRotate", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartData innerRing = innerRingRotate.addChild("innerRing", ModelPartBuilder.create(),
                ModelTransform.rotation(0.0F, 0.0F, 0.7854F));

        ModelPartData thick = innerRing.addChild("thick", ModelPartBuilder.create()
                .uv(28, 14).cuboid(-5.0F, 4.0F, -1.0F, 10.0F, 1.0F, 2.0F)
                .uv(26, 20).cuboid(-5.0F, -5.0F, -1.0F, 10.0F, 1.0F, 2.0F), ModelTransform.NONE);
        thick.addChild("innerRingV", ModelPartBuilder.create()
                .uv(30, 11).cuboid(-4.0F, 4.0F, -1.0F, 8.0F, 1.0F, 2.0F)
                .uv(30, 8).cuboid(-4.0F, -5.0F, -1.0F, 8.0F, 1.0F, 2.0F),
                ModelTransform.rotation(0.0F, 0.0F, 1.5708F));

        ModelPartData thin = innerRing.addChild("thin", ModelPartBuilder.create()
                .uv(0, 32).cuboid(-3.5F, 3.5F, -1.0F, 9.0F, 2.0F, 1.0F)
                .uv(0, 29).cuboid(-5.5F, -5.5F, -1.0F, 9.0F, 2.0F, 1.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.5F));
        thin.addChild("innerRingV2", ModelPartBuilder.create()
                .uv(30, 17).cuboid(-3.5F, 3.5F, -1.0F, 9.0F, 2.0F, 1.0F)
                .uv(32, 4).cuboid(-5.5F, -5.5F, -1.0F, 9.0F, 2.0F, 1.0F),
                ModelTransform.rotation(0.0F, 0.0F, 1.5708F));

        // ---- 中心球 ----
        ModelPartData centerBallRotate = base.addChild("centerBallRotate", ModelPartBuilder.create(), ModelTransform.NONE);
        centerBallRotate.addChild("center", ModelPartBuilder.create()
                .uv(20, 28).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .uv(6, 35).cuboid(-1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 1.0F)
                .uv(0, 35).cuboid(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 1.0F)
                .uv(34, 0).cuboid(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 2.0F)
                .uv(32, 28).cuboid(-0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 2.0F)
                .uv(34, 34).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                .uv(34, 34).cuboid(-1.0F, 2.0F, -1.0F, 2.0F, 1.0F, 2.0F)
                .uv(12, 35).cuboid(-3.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                .uv(12, 35).cuboid(2.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                .uv(26, 23).cuboid(2.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F)
                .uv(26, 23).cuboid(-3.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F),
                ModelTransform.NONE);

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        // 1.12.2：centerBallRotate.rotateAngleY = (netHeadYaw + 90) * DEG；rotateAngleZ = headPitch * DEG
        centerBallRotate.yaw = (headYaw + 90.0F) * DEG_TO_RAD;
        centerBallRotate.roll = headPitch * DEG_TO_RAD;

        // 待机：像滑雪/骑车一样轻微摆动
        base.roll = MathHelper.cos(animationProgress * 0.15F) * 0.1F;

        // 内环缓慢旋转
        innerRingRotate.yaw = MathHelper.cos(animationProgress * 0.1F) * (float) Math.PI;

        // 攻击摆动（原版注释：copied from zombie）
        float attackSwing = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 2.0F * limbDistance * 0.5F;
        outerRingRotate.roll = attackSwing;
        innerRingRotate.roll = attackSwing;
    }
}
