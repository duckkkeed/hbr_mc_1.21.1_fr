package com.hbr_mc.client.renderer;

import com.hbr_mc.HBR_MC;
import com.hbr_mc.client.model.CrestHopperModel;
import com.hbr_mc.client.model.MarionetteModel;
import com.hbr_mc.client.model.SlasherModel;
import com.hbr_mc.client.model.SmallHopperModel;
import com.hbr_mc.entity.EntityCrestHopper;
import com.hbr_mc.entity.EntityDoll;
import com.hbr_mc.entity.EntityMarionette;
import com.hbr_mc.entity.EntitySlasher;
import com.hbr_mc.entity.EntitySmallHopper;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

/**
 * 五种星癌体变体的渲染器集合，参数照搬 1.12.2：
 *
 * <table>
 *   <caption>模型 / 贴图 / 阴影 / 缩放</caption>
 *   <tr><th>变体</th><th>模型</th><th>阴影</th><th>缩放</th><th>垂直偏移</th></tr>
 *   <tr><td>Slasher</td><td>ModelSlasher</td><td>1.6</td><td>2.0</td><td>-1.5</td></tr>
 *   <tr><td>CrestHopper</td><td>ModelCrestHopper2</td><td>1.6</td><td>1.5</td><td>0</td></tr>
 *   <tr><td>SmallHopper</td><td>ModelSmallHopper</td><td>1.6</td><td>1.8</td><td>0</td></tr>
 *   <tr><td>Doll</td><td>ModelMarionette</td><td>0.7</td><td>1.7</td><td>0</td></tr>
 *   <tr><td>Marionette</td><td>ModelMarionette</td><td>0.5</td><td>1.4</td><td>0</td></tr>
 * </table>
 */
public final class CancerRenderers {

    private static final Identifier SLASHER = HBR_MC.id("textures/entity/cancer/slasher.png");
    private static final Identifier CREST_HOPPER = HBR_MC.id("textures/entity/cancer/crest_hopper_2.png");
    private static final Identifier SMALL_HOPPER = HBR_MC.id("textures/entity/cancer/small_hopper.png");
    private static final Identifier MARIONETTE = HBR_MC.id("textures/entity/cancer/marionette.png");

    private CancerRenderers() {
    }

    /** Slasher（1.12.2 RenderSlasher）。 */
    public static final class Slasher extends CancerRenderer<EntitySlasher, SlasherModel<EntitySlasher>> {
        public Slasher(EntityRendererFactory.Context context) {
            super(context, new SlasherModel<>(SlasherModel.getTexturedModelData().createModel()),
                    SLASHER, 1.6F, 2.0F, -1.5F);
        }
    }

    public static final class CrestHopper extends CancerRenderer<EntityCrestHopper, CrestHopperModel<EntityCrestHopper>> {
        public CrestHopper(EntityRendererFactory.Context context) {
            super(context, new CrestHopperModel<>(CrestHopperModel.getTexturedModelData().createModel()),
                    CREST_HOPPER, 1.6F, 1.5F, 0.0F);
        }
    }

    public static final class SmallHopper extends CancerRenderer<EntitySmallHopper, SmallHopperModel<EntitySmallHopper>> {
        public SmallHopper(EntityRendererFactory.Context context) {
            super(context, new SmallHopperModel<>(SmallHopperModel.getTexturedModelData().createModel()),
                    SMALL_HOPPER, 1.6F, 1.8F, 0.0F);
        }
    }

    public static final class Doll extends CancerRenderer<EntityDoll, MarionetteModel<EntityDoll>> {
        public Doll(EntityRendererFactory.Context context) {
            super(context, new MarionetteModel<>(MarionetteModel.getTexturedModelData().createModel()),
                    MARIONETTE, 0.7F, 1.7F, 0.0F);
        }
    }

    public static final class Marionette extends CancerRenderer<EntityMarionette, MarionetteModel<EntityMarionette>> {
        public Marionette(EntityRendererFactory.Context context) {
            super(context, new MarionetteModel<>(MarionetteModel.getTexturedModelData().createModel()),
                    MARIONETTE, 0.5F, 1.4F, 0.0F);
        }
    }
}
