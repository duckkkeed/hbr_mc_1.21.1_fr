package com.hbr_mc.client.renderer;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.entity.EntityCancer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * 星癌体「壳被打破」后的破碎/发光贴图，移植自 1.12.2 的 {@code client/layer/LayerBreak}。
 *
 * <h2>原版逻辑</h2>
 * {@code LayerBreak} 被挂在所有生物渲染器上，但只对 {@link EntityCancer} 生效，且只在
 * <b>DP（壳）已经耗尽</b>时（{@code DShieldUtil.isDPDepleted}）才绘制：
 * <pre>
 * bindTexture(textures/entity/cancer_break.png)
 * enableBlend + blendFunc(ONE, ONE)      // 加色混合
 * disableLighting + 强制满亮度(61680)     // 不受环境光影响
 * renderer.getMainModel().render(...)    // 用同一套模型再画一遍
 * </pre>
 * 视觉上就是"壳碎掉后，怪物身上透出一层裂纹/发光"。
 *
 * <h2>1.21.1 的对应写法</h2>
 * 写成 {@link FeatureRenderer} 并挂到 {@link CancerRenderer} 上（五个变体共用基类，加一次全都有）：
 * <ul>
 *   <li>加色混合 + 满亮度 = {@link RenderLayer#getEyes(Identifier)}（原版眼睛/蜘蛛眼那一层，
 *       正是 {@code SRC_ALPHA, ONE} 加色且不吃光照）；</li>
 *   <li>亮度用 {@link LightmapTextureManager#MAX_LIGHT_COORDINATE}，等价原版的 61680；</li>
 *   <li>触发条件：<b>本来有壳</b>（{@code MAX_ABSORPTION > 0} 或 {@code DP_MAX > 0}）且
 *       <b>当前壳为 0</b>；用这两个属性而不是自定义组件，保证客户端能同步到。</li>
 * </ul>
 */
public class CancerBreakLayer<T extends EntityCancer, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    /** 破碎贴图（1.12.2 的 {@code textures/entity/cancer_break.png}）。 */
    private static final Identifier TEXTURE = HBR_MC.id("textures/entity/cancer_break.png");

    public CancerBreakLayer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        // 壳还在 → 不画；没有壳上限的变体 → 也不画
        //
        // 注意：这里不能用 getAbsorptionAmount()。1.21.1 里只有玩家把吸收值放进了
        // DataTracker，怪物的吸收值不同步，客户端恒为 0 —— 一生成就会误判成"已破壳"。
        // EntityCancer 专门加了 tracked 的 SHELL_DP，服务端与客户端都能读到真实壳量。
        if (entity.getShellMax() <= 0.0F || entity.getShellDp() > 0.0F) {
            return;
        }

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEyes(TEXTURE));
        this.getContextModel().render(matrices, buffer,
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
    }
}
