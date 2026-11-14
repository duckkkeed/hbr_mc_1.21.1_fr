package com.hbr_mc.client.model;

import com.hbr_mc.entity.EntityCancer;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

/** 由 1.12.2 的 SmallHopperModel 自动转换而来。 */
public class SmallHopperModel<T extends EntityCancer> extends SinglePartEntityModel<T> {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final ModelPart p_base;
    private final ModelPart p_leg;
    private final ModelPart p_bone2;
    private final ModelPart p_bone;
    private final ModelPart p_leg2;
    private final ModelPart p_bone3;
    private final ModelPart p_bone4;
    private final ModelPart p_leg3;
    private final ModelPart p_bone5;
    private final ModelPart p_bone6;
    private final ModelPart p_leg4;
    private final ModelPart p_bone7;
    private final ModelPart p_bone8;
    private final ModelPart p_center;

    public SmallHopperModel(ModelPart root) {
        this.p_base = root.getChild("base");
        this.p_leg = this.p_base.getChild("leg");
        this.p_bone2 = this.p_leg.getChild("bone2");
        this.p_bone = this.p_leg.getChild("bone");
        this.p_leg2 = this.p_base.getChild("leg2");
        this.p_bone3 = this.p_leg2.getChild("bone3");
        this.p_bone4 = this.p_leg2.getChild("bone4");
        this.p_leg3 = this.p_base.getChild("leg3");
        this.p_bone5 = this.p_leg3.getChild("bone5");
        this.p_bone6 = this.p_leg3.getChild("bone6");
        this.p_leg4 = this.p_base.getChild("leg4");
        this.p_bone7 = this.p_leg4.getChild("bone7");
        this.p_bone8 = this.p_leg4.getChild("bone8");
        this.p_center = this.p_base.getChild("center");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData p_base = root.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        ModelPartData p_leg = p_base.addChild("leg", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartBuilder builder_p_bone2 = ModelPartBuilder.create();
        builder_p_bone2.uv(16, 22)
                .cuboid(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
        ModelPartData p_bone2 = p_leg.addChild("bone2", builder_p_bone2, ModelTransform.pivot(-7.0F, -10.5F, -7.0F));
        ModelPartBuilder builder_p_bone = ModelPartBuilder.create();
        builder_p_bone.uv(24, 24)
                .cuboid(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F);
        ModelPartData p_bone = p_leg.addChild("bone", builder_p_bone, ModelTransform.pivot(-7.5F, -3.0F, -7.0F));
        ModelPartData p_leg2 = p_base.addChild("leg2", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartBuilder builder_p_bone3 = ModelPartBuilder.create();
        builder_p_bone3.uv(8, 22)
                .cuboid(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
        ModelPartData p_bone3 = p_leg2.addChild("bone3", builder_p_bone3, ModelTransform.pivot(-7.0F, -10.5F, 7.0F));
        ModelPartBuilder builder_p_bone4 = ModelPartBuilder.create();
        builder_p_bone4.uv(24, 16)
                .cuboid(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F);
        ModelPartData p_bone4 = p_leg2.addChild("bone4", builder_p_bone4, ModelTransform.pivot(-7.5F, -3.0F, 7.0F));
        ModelPartData p_leg3 = p_base.addChild("leg3", ModelPartBuilder.create(), ModelTransform.NONE);
        ModelPartBuilder builder_p_bone5 = ModelPartBuilder.create();
        builder_p_bone5.uv(0, 22)
                .cuboid(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
        ModelPartData p_bone5 = p_leg3.addChild("bone5", builder_p_bone5, ModelTransform.pivot(7.0F, -10.5F, 7.0F));
        ModelPartBuilder builder_p_bone6 = ModelPartBuilder.create();
        builder_p_bone6.uv(24, 8)
                .cuboid(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F);
        ModelPartData p_bone6 = p_leg3.addChild("bone6", builder_p_bone6, ModelTransform.pivot(7.5F, -3.0F, 7.0F));
        ModelPartData p_leg4 = p_base.addChild("leg4", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, -1.0F));
        ModelPartBuilder builder_p_bone7 = ModelPartBuilder.create();
        builder_p_bone7.uv(16, 11)
                .cuboid(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
        ModelPartData p_bone7 = p_leg4.addChild("bone7", builder_p_bone7, ModelTransform.pivot(7.0F, -10.5F, -6.0F));
        ModelPartBuilder builder_p_bone8 = ModelPartBuilder.create();
        builder_p_bone8.uv(24, 0)
                .cuboid(-0.5F, -3.0F, -1.0F, 1.0F, 6.0F, 2.0F);
        ModelPartData p_bone8 = p_leg4.addChild("bone8", builder_p_bone8, ModelTransform.pivot(7.5F, -3.0F, -6.0F));
        ModelPartBuilder builder_p_center = ModelPartBuilder.create();
        builder_p_center.uv(0, 0)
                .cuboid(-0.5F, 3.5F, -0.5F, 1.0F, 2.0F, 1.0F);
        builder_p_center.uv(28, 6)
                .cuboid(-1.0F, -5.5F, -1.0F, 2.0F, 2.0F, 2.0F);
        builder_p_center.uv(0, 11)
                .cuboid(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F);
        builder_p_center.uv(0, 0)
                .cuboid(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F);
        ModelPartData p_center = p_base.addChild("center", builder_p_center, ModelTransform.pivot(0.0F, -11.5F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return p_base;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        p_center.yaw = headYaw * DEG_TO_RAD;
        p_center.pitch = headPitch * DEG_TO_RAD;

        float legRaise = 0.1F;
        float legOmega = 0.35F;
        if (entity.isOnGround()) {
            float dy1 = (float) (legRaise * Math.sin(legOmega * animationProgress));
            float dy2 = (float) (legRaise * Math.sin(legOmega * animationProgress + Math.PI));
            p_leg.pivotY = dy1;
            p_leg2.pivotY = dy2;
            p_leg3.pivotY = dy1;
            p_leg4.pivotY = dy2;
        } else {
            p_leg.pivotY = legRaise;
            p_leg2.pivotY = legRaise;
            p_leg3.pivotY = legRaise;
            p_leg4.pivotY = legRaise;
        }
    }
}
