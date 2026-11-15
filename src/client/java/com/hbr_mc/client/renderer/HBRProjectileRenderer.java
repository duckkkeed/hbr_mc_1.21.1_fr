package com.hbr_mc.client.renderer;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.entity.projectile.EntityHBRProjectile;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * 弹丸渲染：始终面向摄像机的小四边形，贴图沿用 1.12.2 的
 * {@code assets/hbr_mc/textures/entity/projectiles/bullet_norm.png}。
 *
 * <p>对应 1.12.2 的 {@code client/renderer/RenderBullet}：原版是一个 sprite 广告牌，
 * 这里用 {@link RenderLayer#getEntityCutoutNoCull} 的四边形等价实现（不做朝向插值）。
 */
public class HBRProjectileRenderer extends EntityRenderer<EntityHBRProjectile> {

    private static final Identifier TEXTURE = HBR_MC.id("textures/entity/projectiles/bullet_norm.png");

    /** 广告牌边长（方块单位）。 */
    private static final float SIZE = 0.5F;

    public HBRProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(EntityHBRProjectile entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // 抬到弹丸中心高度，再套用"面向摄像机"的旋转
        matrices.translate(0.0F, 0.1F, 0.0F);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(SIZE, SIZE, SIZE);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        MatrixStack.Entry entry = matrices.peek();
        float half = 0.5F;
        vertex(buffer, entry, light, -half, -half, 0.0F, 1.0F);
        vertex(buffer, entry, light, half, -half, 1.0F, 1.0F);
        vertex(buffer, entry, light, half, half, 1.0F, 0.0F);
        vertex(buffer, entry, light, -half, half, 0.0F, 0.0F);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry entry, int light,
                               float x, float y, float u, float v) {
        buffer.vertex(entry, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public Identifier getTexture(EntityHBRProjectile entity) {
        return TEXTURE;
    }
}
