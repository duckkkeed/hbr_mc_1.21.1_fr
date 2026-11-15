package com.hbr_mc.mixin;

import com.hbr_mc.attribute.ModAttributes;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 给每个 {@link AttributeContainer} 追加本 mod 的属性（对应 1.12.2 {@code RegisterAttr#onConstruct}）。
 *
 * <p>1.12.2 在 {@code EntityConstructing} 事件里对所有 {@code EntityLivingBase} 调用
 * {@code getAttributeMap().registerAttribute(attr)}。1.21.1 没有这个事件，Fabric 也没有
 * "给所有生物追加属性" 的 API（{@code FabricDefaultAttributeRegistry} 只能整体替换某个实体类型的属性表）。
 *
 * <p>{@code AttributeContainer} 内部是<b>惰性</b>的：{@code getCustomInstance(attr)} 会先查自己的
 * {@code custom} 表，查不到就去 {@code fallback}（{@code DefaultAttributeContainer}）里找；
 * 如果一个属性既不在 {@code custom} 也不在 {@code fallback} 里，{@code createOverride} 直接返回
 * {@code null}——这就是"未登记的自定义属性永远取不到实例"的原因。所以这里在构造末尾把它们塞进
 * {@code custom}，之后手持武器的属性修饰符就能正常叠加。
 *
 * <p>语义上的注意点：
 * <ul>
 *   <li>基值来自属性自身的默认值（0），与原版 {@code getNewAttrNonPercentNonNegative(name, 0)} 一致；</li>
 *   <li>第二个构造参数是"实例变化回调"，原版用它把实例标进 {@code tracked} / {@code pendingUpdate}
 *       以便同步给客户端；本 mod 的属性设成 {@code setTracked(false)}，服务端算伤害足够，
 *       因此传空回调即可（不产生额外网络包）。</li>
 * </ul>
 */
@Mixin(AttributeContainer.class)
public abstract class AttributeContainerMixin {

    @Shadow
    @Final
    private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hbr$appendSeraphAttributes(DefaultAttributeContainer fallback, CallbackInfo ci) {
        for (RegistryEntry<EntityAttribute> attribute : ModAttributes.all()) {
            this.custom.putIfAbsent(attribute, new EntityAttributeInstance(attribute, instance -> {
            }));
        }
    }
}
