package com.hbr_mc.client.renderer;

import com.hbr_mc.combat.DpConfig;
import com.hbr_mc.combat.DpService;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 头顶 DP 条，移植自 1.12.2 的 {@code client/RenderDP}。
 *
 * <h2>原版行为</h2>
 * 对每个可见生物画一条"背景 + 前景"的横条：位置在实体头顶 + {@code GUI_Y}，
 * 颜色固定青色 {@code (0,255,255)}，宽度 {@code RENDER_HUD_SIZE = 20}（像素），
 * 距离超过 64 格、不可见、隐身的实体不画。
 *
 * <h2>1.21.1 的对应写法</h2>
 * 用 Fabric 渲染 API 的 {@link WorldRenderEvents#AFTER_ENTITIES}：此时实体已经画完，
 * 矩阵栈还处于"相机为原点"的世界坐标系，因此按 {@code 实体坐标 - 相机坐标} 平移即可。
 * 然后套用相机旋转（广告牌）与 {@code 0.026666672} 缩放，再用"像素单位"画四边形，
 * 数值与原版 RenderDP 一一对应（见 {@link DpConfig}）。
 *
 * <p>DP 数值来自 {@link DpService#dp} / {@link DpService#dpMax}，所以
 * 玩家（武器耐久）与星癌体（吸收值当壳）共用同一套渲染逻辑。
 */
public final class DpBarRenderer {

    private DpBarRenderer() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(DpBarRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        if (!DpConfig.RENDER_BARS) {
            return;
        }
        ClientWorld world = context.world();
        if (world == null) {
            return;
        }
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        float tickDelta = context.tickCounter().getTickDelta(false);

        boolean any = false;
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isInvisible()) {
                continue;
            }
            float max = DpService.dpMax(living);
            if (max <= 0.0F) {
                continue;
            }
            Vec3d pos = living.getLerpedPos(tickDelta);
            if (pos.squaredDistanceTo(cameraPos) > (double) (DpConfig.MAX_RENDER_DISTANCE * DpConfig.MAX_RENDER_DISTANCE)) {
                continue;
            }

            float ratio = Math.max(0.0F, Math.min(1.0F, DpService.dp(living) / max));
            matrices.push();
            matrices.translate(pos.x - cameraPos.x,
                    pos.y + living.getHeight() + DpConfig.HEIGHT_OFFSET - cameraPos.y,
                    pos.z - cameraPos.z);
            matrices.multiply(camera.getRotation());
            // 与原版一致：负缩放把 Y 轴翻正（相机旋转后 +Y 朝下）
            matrices.scale(-DpConfig.HUD_SCALE, -DpConfig.HUD_SCALE, DpConfig.HUD_SCALE);

            // 层选择很关键：
            //  - getDebugFilledBox() 是 TRIANGLE_STRIP 且**开启背面剔除**，按 QUADS 的顶点顺序喂进去
            //    会让两个三角形绕向相反，其中一个被剔除 → 条只剩半个/缺一角（踩过）；
            //  - getDebugQuads() 是 QUADS + DISABLE_CULLING，正好适合这种"两块矩形广告牌"。
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getDebugQuads());
            MatrixStack.Entry entry = matrices.peek();
            float size = DpConfig.HUD_SIZE;
            float barHeight = DpConfig.BAR_HEIGHT;
            float padding = DpConfig.BAR_PADDING;

            // 背景
            quad(buffer, entry,
                    -size - padding, -padding, size + padding, barHeight + padding,
                    0, 0, 0, DpConfig.BAR_BACKGROUND_ALPHA);
            // 前景（按剩余比例截断）
            quad(buffer, entry,
                    -size, 0.0F, -size + 2.0F * size * ratio, barHeight,
                    DpConfig.BAR_COLOR_R, DpConfig.BAR_COLOR_G, DpConfig.BAR_COLOR_B, DpConfig.BAR_ALPHA);

            matrices.pop();
            any = true;
        }

        if (any && consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    /**
     * 画一个面向相机的矩形（顶点按 QUADS 要求绕一圈：左上 → 右上 → 右下 → 左下）。
     *
     * <p>用 {@link RenderLayer#getDebugQuads()} 时必须是 4 个顶点一圈的顺序；
     * 它不是三角带，所以不能用"左上 → 左下 → 右下 → 右上"那种 Z 字顺序。
     */
    private static void quad(VertexConsumer buffer, MatrixStack.Entry entry,
                             float x1, float y1, float x2, float y2,
                             int red, int green, int blue, int alpha) {
        buffer.vertex(entry, x1, y1, 0.0F).color(red, green, blue, alpha);
        buffer.vertex(entry, x2, y1, 0.0F).color(red, green, blue, alpha);
        buffer.vertex(entry, x2, y2, 0.0F).color(red, green, blue, alpha);
        buffer.vertex(entry, x1, y2, 0.0F).color(red, green, blue, alpha);
    }
}
