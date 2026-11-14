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
import net.minecraft.util.math.MathHelper;

/** 由 1.12.2 的 MarionetteModel 自动转换而来。 */
public class MarionetteModel<T extends EntityCancer> extends SinglePartEntityModel<T> {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final ModelPart p_bone;
    private final ModelPart p_legs;
    private final ModelPart p_neck;
    private final ModelPart p_head;
    private final ModelPart p_body;

    public MarionetteModel(ModelPart root) {
        this.p_bone = root.getChild("bone");
        this.p_legs = this.p_bone.getChild("legs");
        this.p_neck = this.p_bone.getChild("neck");
        this.p_head = this.p_neck.getChild("head");
        this.p_body = this.p_bone.getChild("body");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData p_bone = root.addChild("bone", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        ModelPartBuilder builder_p_legs = ModelPartBuilder.create();
        builder_p_legs.uv(16, 0)
                .cuboid(-1.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F);
        builder_p_legs.uv(0, 0)
                .cuboid(0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F);
        ModelPartData p_legs = p_bone.addChild("legs", builder_p_legs, ModelTransform.NONE);
        ModelPartBuilder builder_p_neck = ModelPartBuilder.create();
        builder_p_neck.uv(15, 11)
                .cuboid(-1.0F, -7.0F, -1.0F, 2.0F, 2.0F, 2.0F);
        ModelPartData p_neck = p_bone.addChild("neck", builder_p_neck, ModelTransform.NONE);
        ModelPartBuilder builder_p_head = ModelPartBuilder.create();
        builder_p_head.uv(0, 0)
                .cuboid(-2.5F, -4.5F, -2.5F, 5.0F, 5.0F, 5.0F);
        ModelPartData p_head = p_neck.addChild("head", builder_p_head, ModelTransform.pivot(0.0F, -6.5F, 0.0F));
        ModelPartBuilder builder_p_body = ModelPartBuilder.create();
        builder_p_body.uv(0, 11)
                .cuboid(-2.0F, -5.0F, -1.5F, 4.0F, 3.0F, 3.0F);
        ModelPartData p_body = p_bone.addChild("body", builder_p_body, ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return p_bone;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // 1.12.2：head.rotateAngleX = 10°·sin(age×0.1)；rotateAngleZ = 10°·cos(age×0.1)
        float headSwing = 10.0F * DEG_TO_RAD;
        p_head.pitch = MathHelper.sin(animationProgress * 0.1F) * headSwing;
        p_head.roll = MathHelper.cos(animationProgress * 0.1F) * headSwing;
    }
}
