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

/** 由 1.12.2 的 CrestHopperModel 自动转换而来。 */
public class CrestHopperModel<T extends EntityCancer> extends SinglePartEntityModel<T> {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final ModelPart p_Crest_Hopper;
    private final ModelPart p_All_Body;
    private final ModelPart p_cube_r1;
    private final ModelPart p_cube_r2;
    private final ModelPart p_cube_r3;
    private final ModelPart p_cube_r4;
    private final ModelPart p_cube_r5;
    private final ModelPart p_cube_r6;
    private final ModelPart p_cube_r7;
    private final ModelPart p_cube_r8;
    private final ModelPart p_cube_r9;
    private final ModelPart p_cube_r10;
    private final ModelPart p_cube_r11;
    private final ModelPart p_All_Leg;
    private final ModelPart p_Right_leg;
    private final ModelPart p_Right_front_leg;
    private final ModelPart p_cube_r12;
    private final ModelPart p_cube_r13;
    private final ModelPart p_cube_r14;
    private final ModelPart p_cube_r15;
    private final ModelPart p_Right_front_Left_front_leg;
    private final ModelPart p_cube_r16;
    private final ModelPart p_cube_r17;
    private final ModelPart p_cube_r18;
    private final ModelPart p_cube_r19;
    private final ModelPart p_cube_r20;
    private final ModelPart p_cube_r21;
    private final ModelPart p_cube_r22;
    private final ModelPart p_cube_r23;
    private final ModelPart p_cube_r24;
    private final ModelPart p_cube_r25;
    private final ModelPart p_cube_r26;
    private final ModelPart p_cube_r27;
    private final ModelPart p_Right_middle_leg;
    private final ModelPart p_cube_r28;
    private final ModelPart p_cube_r29;
    private final ModelPart p_Right_middle_Left_front_leg;
    private final ModelPart p_cube_r30;
    private final ModelPart p_cube_r31;
    private final ModelPart p_cube_r32;
    private final ModelPart p_cube_r33;
    private final ModelPart p_cube_r34;
    private final ModelPart p_cube_r35;
    private final ModelPart p_cube_r36;
    private final ModelPart p_cube_r37;
    private final ModelPart p_cube_r38;
    private final ModelPart p_Right_hind_leg;
    private final ModelPart p_cube_r39;
    private final ModelPart p_cube_r40;
    private final ModelPart p_Right_hind_Left_front_leg;
    private final ModelPart p_cube_r41;
    private final ModelPart p_cube_r42;
    private final ModelPart p_cube_r43;
    private final ModelPart p_cube_r44;
    private final ModelPart p_cube_r45;
    private final ModelPart p_cube_r46;
    private final ModelPart p_cube_r47;
    private final ModelPart p_cube_r48;
    private final ModelPart p_cube_r49;
    private final ModelPart p_Left_leg;
    private final ModelPart p_Left_front_leg;
    private final ModelPart p_cube_r50;
    private final ModelPart p_cube_r51;
    private final ModelPart p_cube_r52;
    private final ModelPart p_cube_r53;
    private final ModelPart p_Left_front_Left_front_leg;
    private final ModelPart p_cube_r54;
    private final ModelPart p_cube_r55;
    private final ModelPart p_cube_r56;
    private final ModelPart p_cube_r57;
    private final ModelPart p_cube_r58;
    private final ModelPart p_cube_r59;
    private final ModelPart p_cube_r60;
    private final ModelPart p_cube_r61;
    private final ModelPart p_cube_r62;
    private final ModelPart p_cube_r63;
    private final ModelPart p_cube_r64;
    private final ModelPart p_cube_r65;
    private final ModelPart p_Left_middle_leg;
    private final ModelPart p_cube_r66;
    private final ModelPart p_cube_r67;
    private final ModelPart p_Left_middle_Left_front_leg;
    private final ModelPart p_cube_r68;
    private final ModelPart p_cube_r69;
    private final ModelPart p_cube_r70;
    private final ModelPart p_cube_r71;
    private final ModelPart p_cube_r72;
    private final ModelPart p_cube_r73;
    private final ModelPart p_cube_r74;
    private final ModelPart p_cube_r75;
    private final ModelPart p_cube_r76;
    private final ModelPart p_Left_hind_leg;
    private final ModelPart p_cube_r77;
    private final ModelPart p_cube_r78;
    private final ModelPart p_Left_hind_Left_front_leg;
    private final ModelPart p_cube_r79;
    private final ModelPart p_cube_r80;
    private final ModelPart p_cube_r81;
    private final ModelPart p_cube_r82;
    private final ModelPart p_cube_r83;
    private final ModelPart p_cube_r84;
    private final ModelPart p_cube_r85;
    private final ModelPart p_cube_r86;
    private final ModelPart p_cube_r87;

    public CrestHopperModel(ModelPart root) {
        this.p_Crest_Hopper = root.getChild("Crest_Hopper");
        this.p_All_Body = this.p_Crest_Hopper.getChild("All_Body");
        this.p_cube_r1 = this.p_All_Body.getChild("cube_r1");
        this.p_cube_r2 = this.p_All_Body.getChild("cube_r2");
        this.p_cube_r3 = this.p_All_Body.getChild("cube_r3");
        this.p_cube_r4 = this.p_All_Body.getChild("cube_r4");
        this.p_cube_r5 = this.p_All_Body.getChild("cube_r5");
        this.p_cube_r6 = this.p_All_Body.getChild("cube_r6");
        this.p_cube_r7 = this.p_All_Body.getChild("cube_r7");
        this.p_cube_r8 = this.p_All_Body.getChild("cube_r8");
        this.p_cube_r9 = this.p_All_Body.getChild("cube_r9");
        this.p_cube_r10 = this.p_All_Body.getChild("cube_r10");
        this.p_cube_r11 = this.p_All_Body.getChild("cube_r11");
        this.p_All_Leg = this.p_Crest_Hopper.getChild("All_Leg");
        this.p_Right_leg = this.p_All_Leg.getChild("Right_leg");
        this.p_Right_front_leg = this.p_Right_leg.getChild("Right_front_leg");
        this.p_cube_r12 = this.p_Right_front_leg.getChild("cube_r12");
        this.p_cube_r13 = this.p_Right_front_leg.getChild("cube_r13");
        this.p_cube_r14 = this.p_Right_front_leg.getChild("cube_r14");
        this.p_cube_r15 = this.p_Right_front_leg.getChild("cube_r15");
        this.p_Right_front_Left_front_leg = this.p_Right_front_leg.getChild("Right_front_Left_front_leg");
        this.p_cube_r16 = this.p_Right_front_Left_front_leg.getChild("cube_r16");
        this.p_cube_r17 = this.p_Right_front_Left_front_leg.getChild("cube_r17");
        this.p_cube_r18 = this.p_Right_front_Left_front_leg.getChild("cube_r18");
        this.p_cube_r19 = this.p_Right_front_Left_front_leg.getChild("cube_r19");
        this.p_cube_r20 = this.p_Right_front_Left_front_leg.getChild("cube_r20");
        this.p_cube_r21 = this.p_Right_front_Left_front_leg.getChild("cube_r21");
        this.p_cube_r22 = this.p_Right_front_Left_front_leg.getChild("cube_r22");
        this.p_cube_r23 = this.p_Right_front_Left_front_leg.getChild("cube_r23");
        this.p_cube_r24 = this.p_Right_front_Left_front_leg.getChild("cube_r24");
        this.p_cube_r25 = this.p_Right_front_Left_front_leg.getChild("cube_r25");
        this.p_cube_r26 = this.p_Right_front_Left_front_leg.getChild("cube_r26");
        this.p_cube_r27 = this.p_Right_front_Left_front_leg.getChild("cube_r27");
        this.p_Right_middle_leg = this.p_Right_leg.getChild("Right_middle_leg");
        this.p_cube_r28 = this.p_Right_middle_leg.getChild("cube_r28");
        this.p_cube_r29 = this.p_Right_middle_leg.getChild("cube_r29");
        this.p_Right_middle_Left_front_leg = this.p_Right_middle_leg.getChild("Right_middle_Left_front_leg");
        this.p_cube_r30 = this.p_Right_middle_Left_front_leg.getChild("cube_r30");
        this.p_cube_r31 = this.p_Right_middle_Left_front_leg.getChild("cube_r31");
        this.p_cube_r32 = this.p_Right_middle_Left_front_leg.getChild("cube_r32");
        this.p_cube_r33 = this.p_Right_middle_Left_front_leg.getChild("cube_r33");
        this.p_cube_r34 = this.p_Right_middle_Left_front_leg.getChild("cube_r34");
        this.p_cube_r35 = this.p_Right_middle_Left_front_leg.getChild("cube_r35");
        this.p_cube_r36 = this.p_Right_middle_Left_front_leg.getChild("cube_r36");
        this.p_cube_r37 = this.p_Right_middle_Left_front_leg.getChild("cube_r37");
        this.p_cube_r38 = this.p_Right_middle_Left_front_leg.getChild("cube_r38");
        this.p_Right_hind_leg = this.p_Right_leg.getChild("Right_hind_leg");
        this.p_cube_r39 = this.p_Right_hind_leg.getChild("cube_r39");
        this.p_cube_r40 = this.p_Right_hind_leg.getChild("cube_r40");
        this.p_Right_hind_Left_front_leg = this.p_Right_hind_leg.getChild("Right_hind_Left_front_leg");
        this.p_cube_r41 = this.p_Right_hind_Left_front_leg.getChild("cube_r41");
        this.p_cube_r42 = this.p_Right_hind_Left_front_leg.getChild("cube_r42");
        this.p_cube_r43 = this.p_Right_hind_Left_front_leg.getChild("cube_r43");
        this.p_cube_r44 = this.p_Right_hind_Left_front_leg.getChild("cube_r44");
        this.p_cube_r45 = this.p_Right_hind_Left_front_leg.getChild("cube_r45");
        this.p_cube_r46 = this.p_Right_hind_Left_front_leg.getChild("cube_r46");
        this.p_cube_r47 = this.p_Right_hind_Left_front_leg.getChild("cube_r47");
        this.p_cube_r48 = this.p_Right_hind_Left_front_leg.getChild("cube_r48");
        this.p_cube_r49 = this.p_Right_hind_Left_front_leg.getChild("cube_r49");
        this.p_Left_leg = this.p_All_Leg.getChild("Left_leg");
        this.p_Left_front_leg = this.p_Left_leg.getChild("Left_front_leg");
        this.p_cube_r50 = this.p_Left_front_leg.getChild("cube_r50");
        this.p_cube_r51 = this.p_Left_front_leg.getChild("cube_r51");
        this.p_cube_r52 = this.p_Left_front_leg.getChild("cube_r52");
        this.p_cube_r53 = this.p_Left_front_leg.getChild("cube_r53");
        this.p_Left_front_Left_front_leg = this.p_Left_front_leg.getChild("Left_front_Left_front_leg");
        this.p_cube_r54 = this.p_Left_front_Left_front_leg.getChild("cube_r54");
        this.p_cube_r55 = this.p_Left_front_Left_front_leg.getChild("cube_r55");
        this.p_cube_r56 = this.p_Left_front_Left_front_leg.getChild("cube_r56");
        this.p_cube_r57 = this.p_Left_front_Left_front_leg.getChild("cube_r57");
        this.p_cube_r58 = this.p_Left_front_Left_front_leg.getChild("cube_r58");
        this.p_cube_r59 = this.p_Left_front_Left_front_leg.getChild("cube_r59");
        this.p_cube_r60 = this.p_Left_front_Left_front_leg.getChild("cube_r60");
        this.p_cube_r61 = this.p_Left_front_Left_front_leg.getChild("cube_r61");
        this.p_cube_r62 = this.p_Left_front_Left_front_leg.getChild("cube_r62");
        this.p_cube_r63 = this.p_Left_front_Left_front_leg.getChild("cube_r63");
        this.p_cube_r64 = this.p_Left_front_Left_front_leg.getChild("cube_r64");
        this.p_cube_r65 = this.p_Left_front_Left_front_leg.getChild("cube_r65");
        this.p_Left_middle_leg = this.p_Left_leg.getChild("Left_middle_leg");
        this.p_cube_r66 = this.p_Left_middle_leg.getChild("cube_r66");
        this.p_cube_r67 = this.p_Left_middle_leg.getChild("cube_r67");
        this.p_Left_middle_Left_front_leg = this.p_Left_middle_leg.getChild("Left_middle_Left_front_leg");
        this.p_cube_r68 = this.p_Left_middle_Left_front_leg.getChild("cube_r68");
        this.p_cube_r69 = this.p_Left_middle_Left_front_leg.getChild("cube_r69");
        this.p_cube_r70 = this.p_Left_middle_Left_front_leg.getChild("cube_r70");
        this.p_cube_r71 = this.p_Left_middle_Left_front_leg.getChild("cube_r71");
        this.p_cube_r72 = this.p_Left_middle_Left_front_leg.getChild("cube_r72");
        this.p_cube_r73 = this.p_Left_middle_Left_front_leg.getChild("cube_r73");
        this.p_cube_r74 = this.p_Left_middle_Left_front_leg.getChild("cube_r74");
        this.p_cube_r75 = this.p_Left_middle_Left_front_leg.getChild("cube_r75");
        this.p_cube_r76 = this.p_Left_middle_Left_front_leg.getChild("cube_r76");
        this.p_Left_hind_leg = this.p_Left_leg.getChild("Left_hind_leg");
        this.p_cube_r77 = this.p_Left_hind_leg.getChild("cube_r77");
        this.p_cube_r78 = this.p_Left_hind_leg.getChild("cube_r78");
        this.p_Left_hind_Left_front_leg = this.p_Left_hind_leg.getChild("Left_hind_Left_front_leg");
        this.p_cube_r79 = this.p_Left_hind_Left_front_leg.getChild("cube_r79");
        this.p_cube_r80 = this.p_Left_hind_Left_front_leg.getChild("cube_r80");
        this.p_cube_r81 = this.p_Left_hind_Left_front_leg.getChild("cube_r81");
        this.p_cube_r82 = this.p_Left_hind_Left_front_leg.getChild("cube_r82");
        this.p_cube_r83 = this.p_Left_hind_Left_front_leg.getChild("cube_r83");
        this.p_cube_r84 = this.p_Left_hind_Left_front_leg.getChild("cube_r84");
        this.p_cube_r85 = this.p_Left_hind_Left_front_leg.getChild("cube_r85");
        this.p_cube_r86 = this.p_Left_hind_Left_front_leg.getChild("cube_r86");
        this.p_cube_r87 = this.p_Left_hind_Left_front_leg.getChild("cube_r87");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData p_Crest_Hopper = root.addChild("Crest_Hopper", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        ModelPartBuilder builder_p_All_Body = ModelPartBuilder.create();
        builder_p_All_Body.uv(0, 0)
                .cuboid(-4.0F, -4.4418F, -13.0175F, 8.0F, 8.0F, 25.0F);
        builder_p_All_Body.uv(0, 33)
                .cuboid(-2.0F, -15.4418F, -3.0175F, 4.0F, 44.0F, 4.0F);
        ModelPartData p_All_Body = p_Crest_Hopper.addChild("All_Body", builder_p_All_Body, ModelTransform.pivot(0.0F, -31.5582F, 0.0175F));
        ModelPartBuilder builder_p_cube_r1 = ModelPartBuilder.create();
        builder_p_cube_r1.uv(44, 79)
                .cuboid(-4.0F, -25.0F, -4.0F, 4.0F, 25.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r1 = p_All_Body.addChild("cube_r1", builder_p_cube_r1, ModelTransform.of(2.0F, 28.5582F, 0.9825F, -0.0873F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r2 = ModelPartBuilder.create();
        builder_p_cube_r2.uv(26, 92)
                .cuboid(-4.0F, 0.0F, -4.0F, 4.0F, 11.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r2 = p_All_Body.addChild("cube_r2", builder_p_cube_r2, ModelTransform.of(2.0F, -15.4418F, 0.9825F, 0.1745F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r3 = ModelPartBuilder.create();
        builder_p_cube_r3.uv(36, 66)
                .cuboid(-8.0F, 0.0F, -1.0F, 8.0F, 6.0F, 1.0F);
        ModelPartData p_cube_r3 = p_All_Body.addChild("cube_r3", builder_p_cube_r3, ModelTransform.of(4.0F, -4.9271F, -13.0175F, 1.4835F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r4 = ModelPartBuilder.create();
        builder_p_cube_r4.uv(0, 82)
                .cuboid(-8.0F, 0.0F, -1.0F, 8.0F, 7.0F, 7.0F, new Dilation(0.01F));
        ModelPartData p_cube_r4 = p_All_Body.addChild("cube_r4", builder_p_cube_r4, ModelTransform.of(4.0F, -4.6845F, 13.5682F, -0.7854F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r5 = ModelPartBuilder.create();
        builder_p_cube_r5.uv(78, 83)
                .cuboid(-8.0F, 0.0F, -6.0F, 8.0F, 6.0F, 6.0F, new Dilation(0.01F));
        ModelPartData p_cube_r5 = p_All_Body.addChild("cube_r5", builder_p_cube_r5, ModelTransform.of(4.0F, -4.9271F, -13.0175F, 0.7854F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r6 = ModelPartBuilder.create();
        builder_p_cube_r6.uv(0, 0)
                .cuboid(-8.0F, 0.0F, 0.0F, 8.0F, 22.0F, 1.0F, new Dilation(0.01F));
        ModelPartData p_cube_r6 = p_All_Body.addChild("cube_r6", builder_p_cube_r6, ModelTransform.of(4.0F, -5.3916F, 12.8611F, -1.5272F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r7 = ModelPartBuilder.create();
        builder_p_cube_r7.uv(60, 83)
                .cuboid(-8.0F, -22.0F, 0.0F, 8.0F, 22.0F, 1.0F, new Dilation(0.01F));
        ModelPartData p_cube_r7 = p_All_Body.addChild("cube_r7", builder_p_cube_r7, ModelTransform.of(4.0F, 4.5079F, 12.8611F, 1.5272F, 0.0F, 0.0F));
        ModelPartBuilder builder_p_cube_r8 = ModelPartBuilder.create();
        builder_p_cube_r8.uv(48, 63)
                .cuboid(-5.9867F, -6.3403F, -12.5719F, 4.0F, 4.0F, 12.0F);
        ModelPartData p_cube_r8 = p_All_Body.addChild("cube_r8", builder_p_cube_r8, ModelTransform.of(9.171F, -1.548F, 1.9825F, -0.1309F, 0.1309F, -0.829F));
        ModelPartBuilder builder_p_cube_r9 = ModelPartBuilder.create();
        builder_p_cube_r9.uv(68, 67)
                .cuboid(-5.9867F, -6.3403F, 0.5719F, 4.0F, 4.0F, 12.0F, new Dilation(0.09F));
        ModelPartData p_cube_r9 = p_All_Body.addChild("cube_r9", builder_p_cube_r9, ModelTransform.of(9.171F, -1.548F, 1.9825F, 0.1309F, -0.1309F, -0.829F));
        ModelPartBuilder builder_p_cube_r10 = ModelPartBuilder.create();
        builder_p_cube_r10.uv(16, 66)
                .cuboid(-7.3098F, 3.8732F, -7.9582F, 4.0F, 4.0F, 12.0F);
        ModelPartData p_cube_r10 = p_All_Body.addChild("cube_r10", builder_p_cube_r10, ModelTransform.of(5.2515F, -1.6886F, -0.0175F, -0.1309F, -0.1309F, 0.829F));
        ModelPartBuilder builder_p_cube_r11 = ModelPartBuilder.create();
        builder_p_cube_r11.uv(66, 36)
                .cuboid(0.0F, -4.0F, 0.0F, 4.0F, 4.0F, 12.0F, new Dilation(0.09F));
        ModelPartData p_cube_r11 = p_All_Body.addChild("cube_r11", builder_p_cube_r11, ModelTransform.of(-6.0515F, -1.6886F, 1.9825F, 0.1309F, 0.1309F, 0.829F));
        ModelPartData p_All_Leg = p_Crest_Hopper.addChild("All_Leg", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -28.0F, 0.0F));
        ModelPartData p_Right_leg = p_All_Leg.addChild("Right_leg", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
        ModelPartData p_Right_front_leg = p_Right_leg.addChild("Right_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(3.6216F, -0.9763F, 5.1332F));
        ModelPartBuilder builder_p_cube_r12 = ModelPartBuilder.create();
        builder_p_cube_r12.uv(86, 45)
                .cuboid(-5.0F, -2.0F, -2.0F, 5.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r12 = p_Right_front_leg.addChild("cube_r12", builder_p_cube_r12, ModelTransform.of(9.0F, -8.0F, 12.0F, 0.6779F, -0.6002F, -0.9591F));
        ModelPartBuilder builder_p_cube_r13 = ModelPartBuilder.create();
        builder_p_cube_r13.uv(70, 36)
                .cuboid(-3.3F, -2.0F, -2.0F, 2.0F, 1.0F, 2.0F);
        builder_p_cube_r13.uv(41, 118)
                .cuboid(-4.0F, -1.0F, -2.0F, 4.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r13 = p_Right_front_leg.addChild("cube_r13", builder_p_cube_r13, ModelTransform.of(8.5657F, -8.7373F, 11.4825F, 0.8608F, -0.1666F, -1.4292F));
        ModelPartBuilder builder_p_cube_r14 = ModelPartBuilder.create();
        builder_p_cube_r14.uv(86, 37)
                .cuboid(-7.5F, -1.0F, -1.0F, 15.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r14 = p_Right_front_leg.addChild("cube_r14", builder_p_cube_r14, ModelTransform.of(3.9055F, -3.143F, 4.3729F, 0.3441F, -0.8192F, -0.456F));
        ModelPartBuilder builder_p_cube_r15 = ModelPartBuilder.create();
        builder_p_cube_r15.uv(86, 41)
                .cuboid(-7.5F, -1.0F, -1.0F, 15.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r15 = p_Right_front_leg.addChild("cube_r15", builder_p_cube_r15, ModelTransform.of(3.9055F, -3.143F, 4.3729F, 0.5031F, -0.7471F, -0.6807F));
        ModelPartData p_Right_front_Left_front_leg = p_Right_front_leg.addChild("Right_front_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(8.2787F, -4.2781F, 9.3827F));
        ModelPartBuilder builder_p_cube_r16 = ModelPartBuilder.create();
        builder_p_cube_r16.uv(56, 79)
                .cuboid(-4.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r16 = p_Right_front_Left_front_leg.addChild("cube_r16", builder_p_cube_r16, ModelTransform.of(3.0F, 3.0F, 1.0F, -0.8025F, -0.178F, 1.1796F));
        ModelPartBuilder builder_p_cube_r17 = ModelPartBuilder.create();
        builder_p_cube_r17.uv(66, 29)
                .cuboid(-18.0F, 0.0F, 0.0F, 18.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r17 = p_Right_front_Left_front_leg.addChild("cube_r17", builder_p_cube_r17, ModelTransform.of(6.822F, 19.614F, 6.7764F, -0.8439F, -0.3267F, 1.3447F));
        ModelPartBuilder builder_p_cube_r18 = ModelPartBuilder.create();
        builder_p_cube_r18.uv(89, 117)
                .cuboid(2.1105F, -1.738F, -2.0F, 4.0F, 1.0F, 3.0F, new Dilation(0.03F));
        ModelPartData p_cube_r18 = p_Right_front_Left_front_leg.addChild("cube_r18", builder_p_cube_r18, ModelTransform.of(7.2499F, 28.3396F, 9.5107F, -0.7209F, -0.544F, 1.0385F));
        ModelPartBuilder builder_p_cube_r19 = ModelPartBuilder.create();
        builder_p_cube_r19.uv(23, 107)
                .cuboid(-0.6497F, 0.1799F, -2.0F, 7.0F, 1.0F, 3.0F, new Dilation(0.02F));
        ModelPartData p_cube_r19 = p_Right_front_Left_front_leg.addChild("cube_r19", builder_p_cube_r19, ModelTransform.of(7.2499F, 28.3396F, 9.5107F, -0.5374F, -0.7254F, 0.7318F));
        ModelPartBuilder builder_p_cube_r20 = ModelPartBuilder.create();
        builder_p_cube_r20.uv(67, 114)
                .cuboid(-1.2614F, -1.928F, -2.0F, 4.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r20 = p_Right_front_Left_front_leg.addChild("cube_r20", builder_p_cube_r20, ModelTransform.of(7.2499F, 28.3396F, 9.5107F, -0.7574F, -0.4851F, 1.1126F));
        ModelPartBuilder builder_p_cube_r21 = ModelPartBuilder.create();
        builder_p_cube_r21.uv(51, 114)
                .cuboid(-1.293F, 0.1051F, -2.0F, 5.0F, 1.0F, 3.0F, new Dilation(-0.01F));
        ModelPartData p_cube_r21 = p_Right_front_Left_front_leg.addChild("cube_r21", builder_p_cube_r21, ModelTransform.of(5.9643F, 24.3396F, 7.9786F, -0.7002F, -0.5724F, 0.9995F));
        ModelPartBuilder builder_p_cube_r22 = ModelPartBuilder.create();
        builder_p_cube_r22.uv(106, 86)
                .cuboid(-1.8844F, -1.7509F, -2.0F, 7.0F, 2.0F, 3.0F, new Dilation(0.01F));
        ModelPartData p_cube_r22 = p_Right_front_Left_front_leg.addChild("cube_r22", builder_p_cube_r22, ModelTransform.of(5.9643F, 24.3396F, 7.9786F, -0.8335F, -0.2975F, 1.3106F));
        ModelPartBuilder builder_p_cube_r23 = ModelPartBuilder.create();
        builder_p_cube_r23.uv(15, 115)
                .cuboid(-5.0F, -1.7F, -2.0F, 5.0F, 1.0F, 3.0F, new Dilation(-0.01F));
        ModelPartData p_cube_r23 = p_Right_front_Left_front_leg.addChild("cube_r23", builder_p_cube_r23, ModelTransform.of(5.5144F, 24.3396F, 7.4424F, -0.7399F, -0.5148F, 1.0762F));
        ModelPartBuilder builder_p_cube_r24 = ModelPartBuilder.create();
        builder_p_cube_r24.uv(0, 113)
                .cuboid(-6.0F, -2.0F, -2.0F, 6.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r24 = p_Right_front_Left_front_leg.addChild("cube_r24", builder_p_cube_r24, ModelTransform.of(5.5144F, 24.3396F, 7.4424F, -0.8492F, -0.2324F, 1.3708F));
        ModelPartBuilder builder_p_cube_r25 = ModelPartBuilder.create();
        builder_p_cube_r25.uv(117, 56)
                .cuboid(0.0F, -2.0F, -2.0F, 4.0F, 1.0F, 2.0F);
        builder_p_cube_r25.uv(97, 22)
                .cuboid(0.0F, -1.0F, -2.0F, 11.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r25 = p_Right_front_Left_front_leg.addChild("cube_r25", builder_p_cube_r25, ModelTransform.of(0.2746F, 9.7553F, 2.0849F, -0.8012F, -0.3931F, 1.2154F));
        ModelPartBuilder builder_p_cube_r26 = ModelPartBuilder.create();
        builder_p_cube_r26.uv(58, 106)
                .cuboid(0.0F, -2.0F, -2.0F, 11.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r26 = p_Right_front_Left_front_leg.addChild("cube_r26", builder_p_cube_r26, ModelTransform.of(-1.5554F, -0.8699F, -0.096F, -0.8555F, -0.1996F, 1.4002F));
        ModelPartBuilder builder_p_cube_r27 = ModelPartBuilder.create();
        builder_p_cube_r27.uv(16, 52)
                .cuboid(-23.0F, 0.0F, 0.0F, 23.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r27 = p_Right_front_Left_front_leg.addChild("cube_r27", builder_p_cube_r27, ModelTransform.of(6.822F, 19.614F, 6.7764F, -0.8335F, -0.2975F, 1.3106F));
        ModelPartData p_Right_middle_leg = p_Right_leg.addChild("Right_middle_leg", ModelPartBuilder.create(), ModelTransform.pivot(3.0362F, -1.5966F, -1.0F));
        ModelPartBuilder builder_p_cube_r28 = ModelPartBuilder.create();
        builder_p_cube_r28.uv(95, 18)
                .cuboid(-0.5F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r28 = p_Right_middle_leg.addChild("cube_r28", builder_p_cube_r28, ModelTransform.of(0.4638F, 0.5966F, 0.0F, 0.0F, 0.0F, -0.5672F));
        ModelPartBuilder builder_p_cube_r29 = ModelPartBuilder.create();
        builder_p_cube_r29.uv(78, 95)
                .cuboid(-7.0F, -2.0F, -1.0F, 14.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r29 = p_Right_middle_leg.addChild("cube_r29", builder_p_cube_r29, ModelTransform.of(6.3548F, -2.1504F, 0.0F, 0.0F, 0.0F, -0.3054F));
        ModelPartData p_Right_middle_Left_front_leg = p_Right_middle_leg.addChild("Right_middle_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(11.7621F, -5.3652F, -0.3478F));
        ModelPartBuilder builder_p_cube_r30 = ModelPartBuilder.create();
        builder_p_cube_r30.uv(18, 111)
                .cuboid(-7.8238F, -1.9731F, -1.1466F, 8.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r30 = p_Right_middle_Left_front_leg.addChild("cube_r30", builder_p_cube_r30, ModelTransform.of(14.856F, 33.7547F, -0.3596F, -0.0202F, 0.0387F, 0.7414F));
        ModelPartBuilder builder_p_cube_r31 = ModelPartBuilder.create();
        builder_p_cube_r31.uv(118, 31)
                .cuboid(-2.3F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r31 = p_Right_middle_Left_front_leg.addChild("cube_r31", builder_p_cube_r31, ModelTransform.of(9.4912F, 28.1322F, -0.1956F, -0.0038F, 0.0435F, 1.1344F));
        ModelPartBuilder builder_p_cube_r32 = ModelPartBuilder.create();
        builder_p_cube_r32.uv(79, 112)
                .cuboid(-7.2386F, -1.8288F, -1.1466F, 8.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r32 = p_Right_middle_Left_front_leg.addChild("cube_r32", builder_p_cube_r32, ModelTransform.of(14.856F, 33.7547F, -0.3596F, -0.0309F, 0.0308F, 0.4359F));
        ModelPartBuilder builder_p_cube_r33 = ModelPartBuilder.create();
        builder_p_cube_r33.uv(57, 58)
                .cuboid(-1.0F, -2.0F, -3.0F, 19.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r33 = p_Right_middle_Left_front_leg.addChild("cube_r33", builder_p_cube_r33, ModelTransform.of(2.7449F, 5.5351F, 1.3478F, 0.0F, 0.0F, 1.2654F));
        ModelPartBuilder builder_p_cube_r34 = ModelPartBuilder.create();
        builder_p_cube_r34.uv(41, 20)
                .cuboid(0.0F, 0.0F, -2.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r34 = p_Right_middle_Left_front_leg.addChild("cube_r34", builder_p_cube_r34, ModelTransform.of(6.5723F, 3.8664F, 2.3478F, 0.0F, 0.0436F, 1.3526F));
        ModelPartBuilder builder_p_cube_r35 = ModelPartBuilder.create();
        builder_p_cube_r35.uv(16, 43)
                .cuboid(0.0F, 0.0F, -4.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r35 = p_Right_middle_Left_front_leg.addChild("cube_r35", builder_p_cube_r35, ModelTransform.of(6.5723F, 3.8664F, 2.3478F, 0.0F, 0.0F, 1.3526F));
        ModelPartBuilder builder_p_cube_r36 = ModelPartBuilder.create();
        builder_p_cube_r36.uv(104, 25)
                .cuboid(0.0F, 0.0F, -4.0F, 9.0F, 2.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r36 = p_Right_middle_Left_front_leg.addChild("cube_r36", builder_p_cube_r36, ModelTransform.of(2.0723F, -3.9279F, 2.3478F, 0.0F, 0.0F, 1.0472F));
        ModelPartBuilder builder_p_cube_r37 = ModelPartBuilder.create();
        builder_p_cube_r37.uv(16, 72)
                .cuboid(0.0F, 0.0F, -3.0F, 3.0F, 3.0F, 3.0F);
        ModelPartData p_cube_r37 = p_Right_middle_Left_front_leg.addChild("cube_r37", builder_p_cube_r37, ModelTransform.of(2.0723F, -3.9279F, 2.3478F, 0.0F, 0.7418F, 1.1781F));
        ModelPartBuilder builder_p_cube_r38 = ModelPartBuilder.create();
        builder_p_cube_r38.uv(100, 79)
                .cuboid(0.0F, 0.0F, -4.0F, 9.0F, 3.0F, 4.0F);
        ModelPartData p_cube_r38 = p_Right_middle_Left_front_leg.addChild("cube_r38", builder_p_cube_r38, ModelTransform.of(2.0723F, -3.9279F, 2.3478F, 0.0F, 0.0F, 1.1781F));
        ModelPartData p_Right_hind_leg = p_Right_leg.addChild("Right_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(3.0837F, -0.5966F, -3.6985F));
        ModelPartBuilder builder_p_cube_r39 = ModelPartBuilder.create();
        builder_p_cube_r39.uv(95, 8)
                .cuboid(-0.5F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r39 = p_Right_hind_leg.addChild("cube_r39", builder_p_cube_r39, ModelTransform.of(-0.6692F, 0.5966F, -0.0254F, -0.3298F, 0.4703F, -0.6469F));
        ModelPartBuilder builder_p_cube_r40 = ModelPartBuilder.create();
        builder_p_cube_r40.uv(95, 13)
                .cuboid(-7.0F, -2.0F, -1.0F, 14.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r40 = p_Right_hind_leg.addChild("cube_r40", builder_p_cube_r40, ModelTransform.of(4.2993F, -2.1504F, -3.1907F, -0.1893F, 0.538F, -0.3578F));
        ModelPartData p_Right_hind_Left_front_leg = p_Right_hind_leg.addChild("Right_hind_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(8.5428F, -5.4133F, -6.1313F));
        ModelPartBuilder builder_p_cube_r41 = ModelPartBuilder.create();
        builder_p_cube_r41.uv(41, 110)
                .cuboid(-7.8238F, -1.9731F, -1.1466F, 8.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r41 = p_Right_hind_Left_front_leg.addChild("cube_r41", builder_p_cube_r41, ModelTransform.of(12.4662F, 33.8028F, -8.5434F, 0.3931F, 0.443F, 0.8434F));
        ModelPartBuilder builder_p_cube_r42 = ModelPartBuilder.create();
        builder_p_cube_r42.uv(118, 35)
                .cuboid(-2.2645F, -0.5582F, -1.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r42 = p_Right_hind_Left_front_leg.addChild("cube_r42", builder_p_cube_r42, ModelTransform.of(8.0563F, 27.4835F, -5.5549F, 0.5028F, 0.3096F, 1.1393F));
        ModelPartBuilder builder_p_cube_r43 = ModelPartBuilder.create();
        builder_p_cube_r43.uv(61, 110)
                .cuboid(-7.2386F, -1.8288F, -1.1466F, 8.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r43 = p_Right_hind_Left_front_leg.addChild("cube_r43", builder_p_cube_r43, ModelTransform.of(12.4662F, 33.8028F, -8.5434F, 0.2365F, 0.5385F, 0.5139F));
        ModelPartBuilder builder_p_cube_r44 = ModelPartBuilder.create();
        builder_p_cube_r44.uv(41, 10)
                .cuboid(0.0F, 0.0F, -2.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r44 = p_Right_hind_Left_front_leg.addChild("cube_r44", builder_p_cube_r44, ModelTransform.of(6.9345F, 3.9145F, -1.8092F, 0.5596F, 0.1536F, 1.4093F));
        ModelPartBuilder builder_p_cube_r45 = ModelPartBuilder.create();
        builder_p_cube_r45.uv(41, 15)
                .cuboid(0.0F, 0.0F, -4.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r45 = p_Right_hind_Left_front_leg.addChild("cube_r45", builder_p_cube_r45, ModelTransform.of(6.9345F, 3.9145F, -1.8092F, 0.5564F, 0.1166F, 1.386F));
        ModelPartBuilder builder_p_cube_r46 = ModelPartBuilder.create();
        builder_p_cube_r46.uv(0, 103)
                .cuboid(0.0F, 0.0F, -4.0F, 9.0F, 2.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r46 = p_Right_hind_Left_front_leg.addChild("cube_r46", builder_p_cube_r46, ModelTransform.of(3.1393F, -3.8797F, 0.6087F, 0.5042F, 0.272F, 1.1177F));
        ModelPartBuilder builder_p_cube_r47 = ModelPartBuilder.create();
        builder_p_cube_r47.uv(63, 53)
                .cuboid(-1.0F, -2.0F, -3.0F, 19.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r47 = p_Right_hind_Left_front_leg.addChild("cube_r47", builder_p_cube_r47, ModelTransform.of(3.1692F, 5.5832F, -0.596F, 0.546F, 0.1623F, 1.3109F));
        ModelPartBuilder builder_p_cube_r48 = ModelPartBuilder.create();
        builder_p_cube_r48.uv(103, 117)
                .cuboid(0.0F, 0.0F, -3.0F, 3.0F, 3.0F, 3.0F);
        ModelPartData p_cube_r48 = p_Right_hind_Left_front_leg.addChild("cube_r48", builder_p_cube_r48, ModelTransform.of(3.1393F, -3.8797F, 0.6087F, 0.7992F, 0.8058F, 1.7523F));
        ModelPartBuilder builder_p_cube_r49 = ModelPartBuilder.create();
        builder_p_cube_r49.uv(78, 99)
                .cuboid(0.0F, 0.0F, -4.0F, 9.0F, 3.0F, 4.0F);
        ModelPartData p_cube_r49 = p_Right_hind_Left_front_leg.addChild("cube_r49", builder_p_cube_r49, ModelTransform.of(3.1393F, -3.8797F, 0.6087F, 0.532F, 0.2071F, 1.2347F));
        ModelPartData p_Left_leg = p_All_Leg.addChild("Left_leg", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
        ModelPartData p_Left_front_leg = p_Left_leg.addChild("Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-3.6216F, -0.9763F, 5.1332F));
        ModelPartBuilder builder_p_cube_r50 = ModelPartBuilder.create();
        builder_p_cube_r50.uv(72, 33)
                .cuboid(0.0F, -2.0F, -2.0F, 5.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r50 = p_Left_front_leg.addChild("cube_r50", builder_p_cube_r50, ModelTransform.of(-9.0F, -8.0F, 12.0F, 0.6779F, 0.6002F, 0.9591F));
        ModelPartBuilder builder_p_cube_r51 = ModelPartBuilder.create();
        builder_p_cube_r51.uv(16, 21)
                .cuboid(1.3F, -2.0F, -2.0F, 2.0F, 1.0F, 2.0F);
        builder_p_cube_r51.uv(118, 39)
                .cuboid(0.0F, -1.0F, -2.0F, 4.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r51 = p_Left_front_leg.addChild("cube_r51", builder_p_cube_r51, ModelTransform.of(-8.5657F, -8.7373F, 11.4825F, 0.8608F, 0.1666F, 1.4292F));
        ModelPartBuilder builder_p_cube_r52 = ModelPartBuilder.create();
        builder_p_cube_r52.uv(68, 63)
                .cuboid(-7.5F, -1.0F, -1.0F, 15.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r52 = p_Left_front_leg.addChild("cube_r52", builder_p_cube_r52, ModelTransform.of(-3.9055F, -3.143F, 4.3729F, 0.3441F, 0.8192F, 0.456F));
        ModelPartBuilder builder_p_cube_r53 = ModelPartBuilder.create();
        builder_p_cube_r53.uv(86, 33)
                .cuboid(-7.5F, -1.0F, -1.0F, 15.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r53 = p_Left_front_leg.addChild("cube_r53", builder_p_cube_r53, ModelTransform.of(-3.9055F, -3.143F, 4.3729F, 0.5031F, 0.7471F, 0.6807F));
        ModelPartData p_Left_front_Left_front_leg = p_Left_front_leg.addChild("Left_front_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-8.2787F, -4.2781F, 9.3827F));
        ModelPartBuilder builder_p_cube_r54 = ModelPartBuilder.create();
        builder_p_cube_r54.uv(36, 73)
                .cuboid(0.0F, 0.0F, 0.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r54 = p_Left_front_Left_front_leg.addChild("cube_r54", builder_p_cube_r54, ModelTransform.of(-3.0F, 3.0F, 1.0F, -0.8025F, 0.178F, -1.1796F));
        ModelPartBuilder builder_p_cube_r55 = ModelPartBuilder.create();
        builder_p_cube_r55.uv(66, 25)
                .cuboid(0.0F, 0.0F, 0.0F, 18.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r55 = p_Left_front_Left_front_leg.addChild("cube_r55", builder_p_cube_r55, ModelTransform.of(-6.822F, 19.614F, 6.7764F, -0.8439F, 0.3267F, -1.3447F));
        ModelPartBuilder builder_p_cube_r56 = ModelPartBuilder.create();
        builder_p_cube_r56.uv(78, 116)
                .cuboid(-6.1105F, -1.738F, -2.0F, 4.0F, 1.0F, 3.0F, new Dilation(0.03F));
        ModelPartData p_cube_r56 = p_Left_front_Left_front_leg.addChild("cube_r56", builder_p_cube_r56, ModelTransform.of(-7.2499F, 28.3396F, 9.5107F, -0.7209F, 0.544F, -1.0385F));
        ModelPartBuilder builder_p_cube_r57 = ModelPartBuilder.create();
        builder_p_cube_r57.uv(106, 91)
                .cuboid(-6.3503F, 0.1799F, -2.0F, 7.0F, 1.0F, 3.0F, new Dilation(0.02F));
        ModelPartData p_cube_r57 = p_Left_front_Left_front_leg.addChild("cube_r57", builder_p_cube_r57, ModelTransform.of(-7.2499F, 28.3396F, 9.5107F, -0.5374F, 0.7254F, -0.7318F));
        ModelPartBuilder builder_p_cube_r58 = ModelPartBuilder.create();
        builder_p_cube_r58.uv(30, 87)
                .cuboid(-2.7386F, -1.928F, -2.0F, 4.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r58 = p_Left_front_Left_front_leg.addChild("cube_r58", builder_p_cube_r58, ModelTransform.of(-7.2499F, 28.3396F, 9.5107F, -0.7574F, 0.4851F, -1.1126F));
        ModelPartBuilder builder_p_cube_r59 = ModelPartBuilder.create();
        builder_p_cube_r59.uv(35, 114)
                .cuboid(-3.707F, 0.1051F, -2.0F, 5.0F, 1.0F, 3.0F, new Dilation(-0.01F));
        ModelPartData p_cube_r59 = p_Left_front_Left_front_leg.addChild("cube_r59", builder_p_cube_r59, ModelTransform.of(-5.9643F, 24.3396F, 7.9786F, -0.7002F, 0.5724F, -0.9995F));
        ModelPartBuilder builder_p_cube_r60 = ModelPartBuilder.create();
        builder_p_cube_r60.uv(23, 82)
                .cuboid(-5.1156F, -1.7509F, -2.0F, 7.0F, 2.0F, 3.0F, new Dilation(0.01F));
        ModelPartData p_cube_r60 = p_Left_front_Left_front_leg.addChild("cube_r60", builder_p_cube_r60, ModelTransform.of(-5.9643F, 24.3396F, 7.9786F, -0.8335F, 0.2975F, -1.3106F));
        ModelPartBuilder builder_p_cube_r61 = ModelPartBuilder.create();
        builder_p_cube_r61.uv(114, 114)
                .cuboid(0.0F, -1.7F, -2.0F, 5.0F, 1.0F, 3.0F, new Dilation(-0.01F));
        ModelPartData p_cube_r61 = p_Left_front_Left_front_leg.addChild("cube_r61", builder_p_cube_r61, ModelTransform.of(-5.5144F, 24.3396F, 7.4424F, -0.7399F, 0.5148F, -1.0762F));
        ModelPartBuilder builder_p_cube_r62 = ModelPartBuilder.create();
        builder_p_cube_r62.uv(99, 112)
                .cuboid(0.0F, -2.0F, -2.0F, 6.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r62 = p_Left_front_Left_front_leg.addChild("cube_r62", builder_p_cube_r62, ModelTransform.of(-5.5144F, 24.3396F, 7.4424F, -0.8492F, 0.2324F, -1.3708F));
        ModelPartBuilder builder_p_cube_r63 = ModelPartBuilder.create();
        builder_p_cube_r63.uv(105, 56)
                .cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 1.0F, 2.0F);
        builder_p_cube_r63.uv(97, 0)
                .cuboid(-11.0F, -1.0F, -2.0F, 11.0F, 1.0F, 2.0F);
        ModelPartData p_cube_r63 = p_Left_front_Left_front_leg.addChild("cube_r63", builder_p_cube_r63, ModelTransform.of(-0.2746F, 9.7553F, 2.0849F, -0.8012F, 0.3931F, -1.2154F));
        ModelPartBuilder builder_p_cube_r64 = ModelPartBuilder.create();
        builder_p_cube_r64.uv(104, 52)
                .cuboid(-11.0F, -2.0F, -2.0F, 11.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r64 = p_Left_front_Left_front_leg.addChild("cube_r64", builder_p_cube_r64, ModelTransform.of(1.5554F, -0.8699F, -0.096F, -0.8555F, 0.1996F, -1.4002F));
        ModelPartBuilder builder_p_cube_r65 = ModelPartBuilder.create();
        builder_p_cube_r65.uv(16, 48)
                .cuboid(0.0F, 0.0F, 0.0F, 23.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r65 = p_Left_front_Left_front_leg.addChild("cube_r65", builder_p_cube_r65, ModelTransform.of(-6.822F, 19.614F, 6.7764F, -0.8335F, 0.2975F, -1.3106F));
        ModelPartData p_Left_middle_leg = p_Left_leg.addChild("Left_middle_leg", ModelPartBuilder.create(), ModelTransform.pivot(-3.0362F, -1.5966F, -1.0F));
        ModelPartBuilder builder_p_cube_r66 = ModelPartBuilder.create();
        builder_p_cube_r66.uv(88, 75)
                .cuboid(-13.5F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r66 = p_Left_middle_leg.addChild("cube_r66", builder_p_cube_r66, ModelTransform.of(-0.4638F, 0.5966F, 0.0F, 0.0F, 0.0F, 0.5672F));
        ModelPartBuilder builder_p_cube_r67 = ModelPartBuilder.create();
        builder_p_cube_r67.uv(95, 3)
                .cuboid(-7.0F, -2.0F, -1.0F, 14.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r67 = p_Left_middle_leg.addChild("cube_r67", builder_p_cube_r67, ModelTransform.of(-6.3548F, -2.1504F, 0.0F, 0.0F, 0.0F, 0.3054F));
        ModelPartData p_Left_middle_Left_front_leg = p_Left_middle_leg.addChild("Left_middle_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-11.7621F, -5.3652F, -0.3478F));
        ModelPartBuilder builder_p_cube_r68 = ModelPartBuilder.create();
        builder_p_cube_r68.uv(102, 108)
                .cuboid(-0.1762F, -1.9731F, -1.1466F, 8.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r68 = p_Left_middle_Left_front_leg.addChild("cube_r68", builder_p_cube_r68, ModelTransform.of(-14.856F, 33.7547F, -0.3596F, -0.0202F, -0.0387F, -0.7414F));
        ModelPartBuilder builder_p_cube_r69 = ModelPartBuilder.create();
        builder_p_cube_r69.uv(29, 118)
                .cuboid(-1.7F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r69 = p_Left_middle_Left_front_leg.addChild("cube_r69", builder_p_cube_r69, ModelTransform.of(-9.4912F, 28.1322F, -0.1956F, -0.0038F, -0.0435F, -1.1344F));
        ModelPartBuilder builder_p_cube_r70 = ModelPartBuilder.create();
        builder_p_cube_r70.uv(0, 109)
                .cuboid(-0.7614F, -1.8288F, -1.1466F, 8.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r70 = p_Left_middle_Left_front_leg.addChild("cube_r70", builder_p_cube_r70, ModelTransform.of(-14.856F, 33.7547F, -0.3596F, -0.0309F, -0.0308F, -0.4359F));
        ModelPartBuilder builder_p_cube_r71 = ModelPartBuilder.create();
        builder_p_cube_r71.uv(16, 56)
                .cuboid(-18.0F, -2.0F, -3.0F, 19.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r71 = p_Left_middle_Left_front_leg.addChild("cube_r71", builder_p_cube_r71, ModelTransform.of(-2.7449F, 5.5351F, 1.3478F, 0.0F, 0.0F, -1.2654F));
        ModelPartBuilder builder_p_cube_r72 = ModelPartBuilder.create();
        builder_p_cube_r72.uv(41, 0)
                .cuboid(-26.0F, 0.0F, -2.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r72 = p_Left_middle_Left_front_leg.addChild("cube_r72", builder_p_cube_r72, ModelTransform.of(-6.5723F, 3.8664F, 2.3478F, 0.0F, -0.0436F, -1.3526F));
        ModelPartBuilder builder_p_cube_r73 = ModelPartBuilder.create();
        builder_p_cube_r73.uv(41, 5)
                .cuboid(-26.0F, 0.0F, -4.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r73 = p_Left_middle_Left_front_leg.addChild("cube_r73", builder_p_cube_r73, ModelTransform.of(-6.5723F, 3.8664F, 2.3478F, 0.0F, 0.0F, -1.3526F));
        ModelPartBuilder builder_p_cube_r74 = ModelPartBuilder.create();
        builder_p_cube_r74.uv(100, 102)
                .cuboid(-9.0F, 0.0F, -4.0F, 9.0F, 2.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r74 = p_Left_middle_Left_front_leg.addChild("cube_r74", builder_p_cube_r74, ModelTransform.of(-2.0723F, -3.9279F, 2.3478F, 0.0F, 0.0F, -1.0472F));
        ModelPartBuilder builder_p_cube_r75 = ModelPartBuilder.create();
        builder_p_cube_r75.uv(68, 67)
                .cuboid(-3.0F, 0.0F, -3.0F, 3.0F, 3.0F, 3.0F);
        ModelPartData p_cube_r75 = p_Left_middle_Left_front_leg.addChild("cube_r75", builder_p_cube_r75, ModelTransform.of(-2.0723F, -3.9279F, 2.3478F, 0.0F, -0.7418F, -1.1781F));
        ModelPartBuilder builder_p_cube_r76 = ModelPartBuilder.create();
        builder_p_cube_r76.uv(98, 45)
                .cuboid(-9.0F, 0.0F, -4.0F, 9.0F, 3.0F, 4.0F);
        ModelPartData p_cube_r76 = p_Left_middle_Left_front_leg.addChild("cube_r76", builder_p_cube_r76, ModelTransform.of(-2.0723F, -3.9279F, 2.3478F, 0.0F, 0.0F, -1.1781F));
        ModelPartData p_Left_hind_leg = p_Left_leg.addChild("Left_hind_leg", ModelPartBuilder.create(), ModelTransform.pivot(-3.0837F, -0.5966F, -3.6985F));
        ModelPartBuilder builder_p_cube_r77 = ModelPartBuilder.create();
        builder_p_cube_r77.uv(88, 67)
                .cuboid(-13.5F, -1.0F, -1.0F, 14.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r77 = p_Left_hind_leg.addChild("cube_r77", builder_p_cube_r77, ModelTransform.of(0.6692F, 0.5966F, -0.0254F, -0.3298F, -0.4703F, 0.6469F));
        ModelPartBuilder builder_p_cube_r78 = ModelPartBuilder.create();
        builder_p_cube_r78.uv(88, 71)
                .cuboid(-7.0F, -2.0F, -1.0F, 14.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r78 = p_Left_hind_leg.addChild("cube_r78", builder_p_cube_r78, ModelTransform.of(-4.2993F, -2.1504F, -3.1907F, -0.1893F, -0.538F, 0.3578F));
        ModelPartData p_Left_hind_Left_front_leg = p_Left_hind_leg.addChild("Left_hind_Left_front_leg", ModelPartBuilder.create(), ModelTransform.pivot(-8.5428F, -5.4133F, -6.1313F));
        ModelPartBuilder builder_p_cube_r79 = ModelPartBuilder.create();
        builder_p_cube_r79.uv(82, 108)
                .cuboid(-0.1762F, -1.9731F, -1.1466F, 8.0F, 2.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r79 = p_Left_hind_Left_front_leg.addChild("cube_r79", builder_p_cube_r79, ModelTransform.of(-12.4662F, 33.8028F, -8.5434F, 0.3931F, -0.443F, -0.8434F));
        ModelPartBuilder builder_p_cube_r80 = ModelPartBuilder.create();
        builder_p_cube_r80.uv(0, 118)
                .cuboid(-1.7355F, -0.5582F, -1.0F, 4.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r80 = p_Left_hind_Left_front_leg.addChild("cube_r80", builder_p_cube_r80, ModelTransform.of(-8.0563F, 27.4835F, -5.5549F, 0.5028F, -0.3096F, -1.1393F));
        ModelPartBuilder builder_p_cube_r81 = ModelPartBuilder.create();
        builder_p_cube_r81.uv(108, 97)
                .cuboid(-0.7614F, -1.8288F, -1.1466F, 8.0F, 2.0F, 2.0F);
        ModelPartData p_cube_r81 = p_Left_hind_Left_front_leg.addChild("cube_r81", builder_p_cube_r81, ModelTransform.of(-12.4662F, 33.8028F, -8.5434F, 0.2365F, -0.5385F, -0.5139F));
        ModelPartBuilder builder_p_cube_r82 = ModelPartBuilder.create();
        builder_p_cube_r82.uv(16, 33)
                .cuboid(-26.0F, 0.0F, -2.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r82 = p_Left_hind_Left_front_leg.addChild("cube_r82", builder_p_cube_r82, ModelTransform.of(-6.9345F, 3.9145F, -1.8092F, 0.5596F, -0.1536F, -1.4093F));
        ModelPartBuilder builder_p_cube_r83 = ModelPartBuilder.create();
        builder_p_cube_r83.uv(16, 38)
                .cuboid(-26.0F, 0.0F, -4.0F, 26.0F, 3.0F, 2.0F, new Dilation(0.01F));
        ModelPartData p_cube_r83 = p_Left_hind_Left_front_leg.addChild("cube_r83", builder_p_cube_r83, ModelTransform.of(-6.9345F, 3.9145F, -1.8092F, 0.5564F, -0.1166F, -1.386F));
        ModelPartBuilder builder_p_cube_r84 = ModelPartBuilder.create();
        builder_p_cube_r84.uv(100, 59)
                .cuboid(-9.0F, 0.0F, -4.0F, 9.0F, 2.0F, 4.0F, new Dilation(0.01F));
        ModelPartData p_cube_r84 = p_Left_hind_Left_front_leg.addChild("cube_r84", builder_p_cube_r84, ModelTransform.of(-3.1393F, -3.8797F, 0.6087F, 0.5042F, -0.272F, -1.1177F));
        ModelPartBuilder builder_p_cube_r85 = ModelPartBuilder.create();
        builder_p_cube_r85.uv(16, 61)
                .cuboid(-18.0F, -2.0F, -3.0F, 19.0F, 2.0F, 3.0F);
        ModelPartData p_cube_r85 = p_Left_hind_Left_front_leg.addChild("cube_r85", builder_p_cube_r85, ModelTransform.of(-3.1692F, 5.5832F, -0.596F, 0.546F, -0.1623F, -1.3109F));
        ModelPartBuilder builder_p_cube_r86 = ModelPartBuilder.create();
        builder_p_cube_r86.uv(16, 66)
                .cuboid(-3.0F, 0.0F, -3.0F, 3.0F, 3.0F, 3.0F);
        ModelPartData p_cube_r86 = p_Left_hind_Left_front_leg.addChild("cube_r86", builder_p_cube_r86, ModelTransform.of(-3.1393F, -3.8797F, 0.6087F, 0.7992F, -0.8058F, -1.7523F));
        ModelPartBuilder builder_p_cube_r87 = ModelPartBuilder.create();
        builder_p_cube_r87.uv(0, 96)
                .cuboid(-9.0F, 0.0F, -4.0F, 9.0F, 3.0F, 4.0F);
        ModelPartData p_cube_r87 = p_Left_hind_Left_front_leg.addChild("cube_r87", builder_p_cube_r87, ModelTransform.of(-3.1393F, -3.8797F, 0.6087F, 0.532F, -0.2071F, -1.2347F));
        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public ModelPart getPart() {
        return p_Crest_Hopper;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // 1.12.2：All_Body.rotateAngleY = (180 + headYaw)°；rotateAngleX = -headPitch°；offsetY = -0.1 + 0.05·sin(0.175·age)
        p_All_Body.yaw = (180.0F + headYaw) * DEG_TO_RAD;
        p_All_Body.pitch = -headPitch * DEG_TO_RAD;
        p_All_Body.pivotY = -31.5582F + (-0.1F + 0.05F * (float) Math.sin(0.175F * animationProgress));

        float legRaise = 0.1F;
        float legOmega = 0.35F;
        if (entity.isOnGround()) {
            float dy1 = (float) (legRaise * Math.sin(legOmega * animationProgress));
            float dy2 = (float) (legRaise * Math.sin(legOmega * animationProgress + Math.PI));
            p_Right_front_leg.pivotY = -0.9763F + dy1;
            p_Left_front_leg.pivotY = -0.9763F + dy2;
            p_Right_middle_leg.pivotY = -1.5966F + dy2;
            p_Left_middle_leg.pivotY = -1.5966F + dy1;
            p_Right_hind_leg.pivotY = -0.5966F + dy1;
            p_Left_hind_leg.pivotY = -0.5966F + dy2;
        } else {
            p_Right_front_leg.pivotY = -0.9763F + legRaise * 2.0F;
            p_Left_front_leg.pivotY = -0.9763F + legRaise * 2.0F;
            p_Right_middle_leg.pivotY = -1.5966F + legRaise;
            p_Left_middle_leg.pivotY = -1.5966F + legRaise;
            p_Right_hind_leg.pivotY = -0.5966F + legRaise;
            p_Left_hind_leg.pivotY = -0.5966F + legRaise;
        }
    }
}
