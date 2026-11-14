package com.hbr_mc.client.renderer;

import com.hbr_mc.entity.EntityCancer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * 星癌体渲染器基类：模型、贴图、阴影半径、缩放与垂直偏移都由构造参数给出。
 *
 * <p>对应 1.12.2 的 {@code RenderSlasher} / {@code RenderCrestHopper} / {@code RenderSmallHopper} /
 * {@code RenderDoll} / {@code RenderMarionette}。具体参数见 {@link CancerRenderers}。
 */
public abstract class CancerRenderer<T extends EntityCancer, M extends EntityModel<T>>
        extends MobEntityRenderer<T, M> {

    private final Identifier texture;
    private final float modelScale;
    private final float yOffset;

    protected CancerRenderer(EntityRendererFactory.Context context, M model,
                             Identifier texture, float shadowRadius, float modelScale, float yOffset) {
        super(context, model, shadowRadius);
        this.texture = texture;
        this.modelScale = modelScale;
        this.yOffset = yOffset;
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.translate(0.0F, yOffset, 0.0F);
        matrices.scale(modelScale, modelScale, modelScale);
    }
}
