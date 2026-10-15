# 物品提示框背景 / 边框自定义（Mixin 实现文档）

> 适用版本：**Minecraft 1.21.1 + Fabric Loader 0.19.5 + Fabric API 0.116.17**
> 涉及代码：`src/client/java/com/hbr_mc/client/` 下的 `mixin/` 与 `tooltip/`

---

## 1. 效果与功能

| 功能 | 说明 |
|---|---|
| 自定义背景色 | 支持任意 **ARGB** 颜色，`AA` 通道即透明度（`00` 全透明 ~ `FF` 不透明） |
| 自定义边框色 | 可以是**单一纯色**，也可以是**上→下渐变**（两个颜色） |
| **只改某件物品** | 按物品类型单独配色，未指定的物品沿用全局配色（见 §4） |
| 运行时切换 | 可在任意时刻改配色（配置界面实时预览、按稀有度分色都可行） |
| 零侵入几何 | 只替换颜色参数，提示框尺寸、内边距、渲染层级 `z` 完全沿用原版 |
| 冲突保护 | 检测到 `customtooltips`(Custom Tooltip API) 时自动让位，不崩溃 |

不改变提示框大小、文字、图标、位置等任何其它表现。

---

## 2. 快速上手：改颜色

只需要改**一个地方**：

📄 `src/client/java/com/hbr_mc/client/HBR_MCClient.java`

```java
private static final TooltipStyle TOOLTIP_STYLE = TooltipStyle.ofSolidBorder(
        0xCC0B1A2A, // ① 背景色：80% 不透明度的深藏青
        0xFF35D6FF  // ② 边框色：不透明青色
);

@Override
public void onInitializeClient() {
    TooltipStyleManager.setStyle(TOOLTIP_STYLE);
}
```

颜色写法是 `0xAARRGGBB`：

```
0x CC 0B1A2A
   └┬┘ └──┬─┘
    │     └── RGB 分量（红绿蓝）
    └──────── Alpha 透明度：00=全透明, 80=半透明, FF=不透明
```

常用示例：

| 想要的视觉效果 | 写法 |
|---|---|
| 原版深紫黑、半透明 | `0xF0100010` |
| 纯黑 50% 透明 | `0x80000000` |
| 深灰、完全不透明 | `0xFF1E1E1E` |
| 藏青、80% 不透明 | `0xCC0B1A2A` |
| 青色边框（不透明） | `0xFF35D6FF` |
| 金色边框（不透明） | `0xFFFFC800` |
| 半透明红边框 | `0x80FF2020` |

---

## 3. 完整 API

### 3.1 构造配色 `TooltipStyle`

📄 `src/client/java/com/hbr_mc/client/tooltip/TooltipStyle.java`

```java
// 单一纯色边框（最常用；左右竖条与上下横线同色）
TooltipStyle.ofSolidBorder(int background, int border);

// 上下渐变边框：左右竖条从 borderTop 渐变到 borderBottom，上横线=top，下横线=bottom
TooltipStyle.ofGradientBorder(int background, int borderTop, int borderBottom);

// 完整构造：duplicateBorderGradient=false 时全部边框使用 borderTop 的纯色
new TooltipStyle(int background, int borderTop, int borderBottom, boolean duplicateBorderGradient);

// 预置
TooltipStyle.VANILLA;                       // 原版配色

// 基于已有配色派生的便捷方法
style.withBackgroundColor(int background);
style.withBorderColor(int border);
style.withBorderGradient(int borderTop, int borderBottom);
TooltipStyle.withBackground(int background);  // 静态版：只换背景，边框保持原版
```

字段含义：

| 字段 | 影响范围 |
|---|---|
| `background` | 提示框内部填充 + 背景外扩 1px 的上/下横线与左/右竖条（原版它们都用 `0xF0100010`） |
| `borderTop` | 边框左/右竖条渐变的**起点**、边框**上**横线（原版 `0x505000FF`） |
| `borderBottom` | 边框左/右竖条渐变的**终点**、边框**下**横线（原版 `0x5028007F`） |
| `duplicateBorderGradient` | `true`=左右竖条上下渐变（原版观感）；`false`=左右竖条用纯 `borderTop` 色（更“硬”的实线边框） |

### 3.2 应用配色 `TooltipStyleManager`

📄 `src/client/java/com/hbr_mc/client/tooltip/TooltipStyleManager.java`

```java
// 全局
TooltipStyleManager.setStyle(TooltipStyle style);  // 设置全局配色（传 null 等价于恢复原版）
TooltipStyleManager.getStyle();                    // 读取全局配色，永不为 null
TooltipStyleManager.reset();                       // 全局恢复原版

// 按物品（优先于全局）
TooltipStyleManager.setStyle(Item item, TooltipStyle style);  // 给单件物品配色
TooltipStyleManager.getItemStyle(Item item);                  // 读取某物品的配色，可为 null
TooltipStyleManager.hasItemStyle(Item item);                  // 是否设过
TooltipStyleManager.clearStyle(Item item);                    // 取消单件物品的配色
TooltipStyleManager.clearItemStyles();                        // 取消所有物品级配色

// 内部使用（Mixin 调用）：优先物品级、否则全局
TooltipStyleManager.resolveStyle();
```

> 提示框在**每帧渲染时**才读取当前配色，所以 `setStyle` 可以随时调用，立即生效，无需重载资源。
> 全局与物品级可以同时存在：命中物品级配色时**完全**使用该物品的配色，不会与全局配色混合。

---

## 4. 只改某一件物品的提示框

### 4.1 用法

```java
// 全局：其它物品都用这套
TooltipStyleManager.setStyle(TooltipStyle.ofSolidBorder(0xCC0B1A2A, 0xFF35D6FF));

// 只给钻石换成自己的配色
TooltipStyleManager.setStyle(Items.DIAMOND, TooltipStyle.ofSolidBorder(0xCC001B33, 0xFF55FFFF));

// 只给本 mod 的 rapid_fire_ss 换成金色渐变边框
TooltipStyleManager.setStyle(ModItems.RAPID_FIRE_SS,
        TooltipStyle.ofGradientBorder(0xE62A1400, 0xFFFFC800, 0xFFB35C00));

// 取消钻石的专属配色（回落到全局）
TooltipStyleManager.clearStyle(Items.DIAMOND);
```

实际可运行的示例见 📄 [HBR_MCClient.java](../src/client/java/com/hbr_mc/client/HBR_MCClient.java)：
其中 `ModItems.RAPID_FIRE_SS` 使用了独立的金色渐变配色。

调试便利：开发环境下可用系统属性直接给任意物品套用示例配色，不必改代码：

```
gradlew runClient -DhbrMc.testItemStyle=minecraft:stone
```

（该分支仅在 `FabricLoader#isDevelopmentEnvironment()` 为真时生效，正式打包不影响。）

### 4.2 原理与实现难点

难点在于：真正画方框的

```java
TooltipBackgroundRenderer.render(DrawContext context, int x, int y, int width, int height, int z)
```

**参数里没有 `ItemStack`**，拿不到"当前是哪个物品"。因此需要在更上层的、仍然持有物品身份的绘制入口把物品记录下来。

物品提示框在 1.21.1 中只有两个带 `ItemStack` 的入口：

| 入口 | 覆盖场景 | 物品来源 |
|---|---|---|
| `DrawContext#drawItemTooltip(TextRenderer, ItemStack, int, int)` | 商人交易格子等直接调用 | 参数 |
| `HandledScreen#drawMouseoverTooltip(DrawContext, int, int)` | 背包 / 箱子 / 创造物品栏……所有容器界面 | `this.focusedSlot.getStack()`（**局部变量**） |

两者最终都会在**同一调用栈内**同步走到 `TooltipBackgroundRenderer.render(...)`（渲染线程），因此用 `ThreadLocal<Item>` 传递身份即可，不需要改任何方法签名：

```
HandledScreen#drawMouseoverTooltip  ──┐
                                      ├─► setCurrentItem(...)  ──► DrawContext#drawTooltip
DrawContext#drawItemTooltip        ──┘                                │（同一调用栈，同步）
                                                                      ▼
                                   TooltipBackgroundRenderer.render(...)
                                                                      │
                                                     TooltipStyleManager.resolveStyle()
                                                     └─ 命中物品级配色 → 用它；否则用全局
```

物品上下文在入口写入、在出口（`@At("RETURN")`）立即清理，因此不会跨帧或跨物品残留；异常路径由 Mixin 生成的 try/finally 兜底。

对应的两个 Mixin：

| Mixin | 目标 | 注入 |
|---|---|---|
| [DrawContextItemTooltipMixin.java](../src/client/java/com/hbr_mc/client/mixin/DrawContextItemTooltipMixin.java) | `DrawContext` | `drawItemTooltip` 的 `HEAD` 记录 / `RETURN` 清理 |
| [HandledScreenTooltipContextMixin.java](../src/client/java/com/hbr_mc/client/mixin/HandledScreenTooltipContextMixin.java) | `HandledScreen` | `drawMouseoverTooltip` 的 `HEAD` 记录 / `RETURN` 清理，物品取自 `@Shadow focusedSlot` |

### 4.3 实现这条路径时踩到的坑（都已修正）

1. **不能用 `@ModifyArg` 拿物品**。`HandledScreen` 里 `ItemStack itemStack` 只是**局部变量**，
   `DrawContext#drawTooltip` 的参数表是 `(TextRenderer, List, Optional, int, int)`，**根本没有物品参数**，
   所以只能在方法体里读取。最初按"第 2 个参数"改写导致
   `handler arg ItemStack != invoked arg #2 (Optional)` 注入失败。
2. **`@Shadow` 字段必须与目标字段同名**。写成 `hbr_mc$focusedSlot` 会报
   `@Shadow field hbr_mc$focusedSlot was not located in the target class`（这套 Loom 不生成 refmap，
   字段名没有重映射机会）。影子字段的作用域是单个 Mixin，不必担心与其它 Mixin 重名。
3. **`@Inject` 回调签名 = 目标方法参数 + `CallbackInfo`**，多一个少一个都会注入失败。

---

## 5. 原理：1.21.1 提示框是怎么画的

方框由 `net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer` 绘制。关键点是——**`render()` 自己不调用 `DrawContext.fill`**，它把写死的颜色作为参数传给三个私有静态辅助方法：

```
render(context, x, y, width, height, z)          // 提示框左上角 + 内容宽高
  │
  ├─ #0 renderHorizontalLine(..., z, 0xF0100010)  // 背景上边
  ├─ #1 renderHorizontalLine(..., z, 0xF0100010)  // 背景下边
  ├─ #2 renderRectangle     (..., z, 0xF0100010)  // 背景主体矩形
  ├─ #3 renderVerticalLine  (..., z, 0xF0100010)  // 背景左边
  ├─ #4 renderVerticalLine  (..., z, 0xF0100010)  // 背景右边
  └─ #5 renderBorder(..., z, 0x505000FF, 0x5028007F)   // 边框：起始色 / 结束色
        │
        ├─ renderVerticalLine(..., 起始色, 结束色)   → DrawContext.fillGradient  // 左右竖边框
        ├─ renderVerticalLine(..., 起始色, 结束色)   → DrawContext.fillGradient
        ├─ renderHorizontalLine(..., 起始色)          → DrawContext.fill          // 上横边框
        └─ renderHorizontalLine(..., 结束色)          → DrawContext.fill          // 下横边框
```

原版三个颜色常量（十进制 / 十六进制）：

| 常量 | 十进制 | 十六进制 | 含义 |
|---|---|---|---|
| `BACKGROUND_COLOR` | `-267386864` | `0xF0100010` | 背景（半透明深紫黑） |
| `START_Y_BORDER_COLOR` | `1347420415` | `0x505000FF` | 边框起始色 |
| `END_Y_BORDER_COLOR` | `1344798847` | `0x5028007F` | 边框结束色 |

---

## 6. 实现方式与取舍

### 6.1 为什么用 `@ModifyArg` 改调用点，而不是在辅助方法里 `@Redirect`

最初的做法是在 `renderHorizontalLine` / `renderRectangle` / `renderVerticalLine` 内部对 `DrawContext.fill` / `fillGradient` 做 `@Redirect`。**实测不可行**：

- 这些辅助方法里的调用点会被别的提示框 mod 先改写，`@Redirect` 会报
  `Critical injection failure ... Scanned 0 target(s)`（`require = 1` 下直接崩溃）；
- 而且辅助方法被多处共用（`renderHorizontalLine` 同时服务于背景边和边框横线），语义要靠入参推算。

改为在 `render` 方法体内的 6 个调用点上做 `@ModifyArg` 后：

- 注入位置最稳定，全部落在一个方法里；
- 与其它 mod 的 `@ModifyArg` 可以**串联叠加**，不像 `@Redirect` 会互斥；
- 只替换颜色参数，几何尺寸天然不变。

### 6.2 按原版常量映射颜色

```java
private static int hbr_mc$mapColor(int vanillaColor) {
    TooltipStyle style = TooltipStyleManager.getStyle();
    return switch (vanillaColor) {
        case HBR_VANILLA_BACKGROUND   -> style.background();
        case HBR_VANILLA_BORDER_START -> style.borderTop();
        case HBR_VANILLA_BORDER_END   -> style.duplicateBorderGradient()
                                            ? style.borderBottom() : style.borderTop();
        default -> vanillaColor;   // 未识别颜色原样透传
    };
}
```

用「原版常量 → 自定义颜色」的映射，而不是「第 N 个参数 = 边框色」的假设，好处是：

- 共用方法（如 `renderHorizontalLine`）无需区分调用来源，映射天然正确；
- 其它 mod 若传入别的颜色，会被安全透传，不会被误改。

### 6.3 注入点一览

| 序 | `at.target`（1.21.1 命名） | `ordinal` | `index` | 替换内容 |
|---|---|---|---|---|
| 1 | `TooltipBackgroundRenderer;renderHorizontalLine(...IIIII)V` | 0 | 5 | 背景上边颜色 |
| 2 | 同上 | 1 | 5 | 背景下边颜色 |
| 3 | `TooltipBackgroundRenderer;renderRectangle(...IIIIII)V` | — | 6 | 背景主体颜色 |
| 4 | `TooltipBackgroundRenderer;renderVerticalLine(...IIIII)V` | 0 | 5 | 背景左边颜色 |
| 5 | 同上 | 1 | 5 | 背景右边颜色 |
| 6 | `TooltipBackgroundRenderer;renderBorder(...IIIIIII)V` | — | 6 | 边框起始色（左/右竖条渐变起点 + 上横线） |
| 7 | 同上 | — | 7 | 边框结束色（渐变终点 + 下横线） |

> 注意签名差异（很多资料写错）：
> `DrawContext.fill(IIIIII)V` 是 **6** 个 int（末尾 color）；
> `DrawContext.fillGradient(IIIIIII)V` 是 **7** 个 int（`z` 在 `colorStart/colorEnd` 之前）。

所有注入都带 `require = 1`：一旦 Mojang 改动这段结构，游戏会在 Mixin 应用阶段**立刻报错**而不是静默失效，便于第一时间发现。

---

## 7. 与其它提示框 mod 的兼容性

你的 `run/mods/` 里装了 **Custom Tooltip API**（mod id `customtooltips`）。它的 `TooltipBackgroundRendererMixin` 同样改写这个类的同一批调用点，且：

- 对 `renderRectangle` 做 `@Redirect` → 支持自定义背景贴图/渐变/纯色；
- 对 `renderHorizontalLine` / `renderVerticalLine` / `renderBorder` 做 `@ModifyArg` → 缩放缓停时透明度、覆盖边框颜色，并读它自己的配置文件。

两套逻辑同时接管会互相覆盖、配置也难以预期，因此 📄 [HbrMixinPlugin.java](../src/client/java/com/hbr_mc/client/mixin/HbrMixinPlugin.java) 会在检测到 `customtooltips` 时**跳过本 Mixin**，并打印一条警告：

```
[hbr_mc/mixin] 检测到 customtooltips(Custom Tooltip API)，已跳过 TooltipBackgroundMixin；
提示框背景/边框请使用该 mod 的配置（hbr_mc 的 TooltipStyleManager 此时不生效）。
```

此时 `TooltipStyleManager` 的设置不会生效。两种选择：

| 想要的方案 | 做法 |
|---|---|
| 用**本套**配色（Mixin） | 删除 `run/mods/custom_tooltip_api-fabric-*.jar`（它依赖的 `s-lib` 可一并删除） |
| 用 Custom Tooltip API | 保留现状，本 Mixin 自动让位，改配色请用它自己的配置界面/`run/config/custom_tooltip_api/config.json5` |

若将来换掉那个 mod，`HbrMixinPlugin` 会自动恢复本 Mixin，无需改代码。

---

## 8. 验证记录

| 验证项 | 方法 | 结果 |
|---|---|---|
| 源码结构 | 读取本机 loom 缓存的官方反编译源码（Yarn 1.21.1+build.3） | 确认 6 个调用点顺序、常量取值、方法描述符 |
| 注入点（配色） | 自写 class 文件解析器反汇编 `TooltipBackgroundRenderer` 字节码 | 7 个注入点的 `ordinal`/`index`/描述符逐条对应 |
| 注入点（物品上下文） | 同上，对 `DrawContext` / `HandledScreen` | 4 个 `@Inject` 的回调签名与目标参数逐一匹配 |
| 编译 | `gradlew compileClientJava` | BUILD SUCCESSFUL |
| 真机运行（无冲突 mod，全局配色） | `gradlew runClient`，进创造物品栏触发提示框 | `Mixing TooltipBackgroundMixin ...`，提示框正常渲染，无崩溃 |
| 真机运行（无冲突 mod，**按物品配色**） | `-DhbrMc.testItemStyle=minecraft:stone`，悬停石头 | 日志 1604 次 `minecraft:stone 使用专属提示框配色`，解析到的配色正是设置的金色渐变（`background=-433449984, borderTop=-14336, borderBottom=-5022720`） |
| 真机运行（有 customtooltips） | `gradlew runClient` | 打印降级警告、跳过三个提示框 Mixin，无崩溃 |
| 产物 remap | 解析 `build/libs/hbr_mc-1.0.0.jar` 内注解与调用 | 注解已重映射为 intermediary（`method_47946` / `index=5..7`），无未重映射的 Minecraft 引用 |

> 这套 Loom（`fabric-loom-remap 1.17`）在 `remapJar` 阶段直接重映射注解，**不生成 refmap**，属于正常现象，不影响生产环境加载。
>
> 验证脚本（`@Shadow`、`@Inject` 等编译期错误只能靠真机运行发现）见 `build/tools/`：
> `classfile.py`（class 文件解析）、`verify_mixin.py`（注入点/签名静态校验）。

---

## 9. 文件清单

| 文件 | 职责 |
|---|---|
| 📄 [TooltipBackgroundMixin.java](../src/client/java/com/hbr_mc/client/mixin/TooltipBackgroundMixin.java) | 核心 Mixin：在 `render` 的 6 个调用点上替换颜色 |
| 📄 [DrawContextItemTooltipMixin.java](../src/client/java/com/hbr_mc/client/mixin/DrawContextItemTooltipMixin.java) | 物品上下文（`drawItemTooltip` 路径） |
| 📄 [HandledScreenTooltipContextMixin.java](../src/client/java/com/hbr_mc/client/mixin/HandledScreenTooltipContextMixin.java) | 物品上下文（容器界面路径，`@Shadow focusedSlot`） |
| 📄 [HbrMixinPlugin.java](../src/client/java/com/hbr_mc/client/mixin/HbrMixinPlugin.java) | Mixin 配置插件：与 Custom Tooltip API 冲突时跳过上述三个 Mixin |
| 📄 [TooltipStyle.java](../src/client/java/com/hbr_mc/client/tooltip/TooltipStyle.java) | 配色数据模型（record）+ 构造/派生方法 |
| 📄 [TooltipStyleManager.java](../src/client/java/com/hbr_mc/client/tooltip/TooltipStyleManager.java) | 配色入口：全局 / 按物品 / 上下文 / `resolveStyle` |
| 📄 [HBR_MCClient.java](../src/client/java/com/hbr_mc/client/HBR_MCClient.java) | 客户端入口：在这里设置颜色（含按物品示例） |
| 📄 [hbr_mc.client.mixins.json](../src/client/resources/hbr_mc.client.mixins.json) | 注册三个 Mixin 与插件 |

---

## 10. 常见问题

**Q：改了颜色没变化？**
1. 确认 `run/mods/` 里没有 `custom_tooltip_api`（会接管，见 §7）；启动日志里搜 `hbr_mc/mixin` 能看到提示；
2. 确认 `TooltipStyleManager.setStyle(...)` 真的被调用（在 `HBR_MCClient#onInitializeClient` 中）；
3. 若填了 `0x00......`（Alpha=00）背景就是全透明，看起来“没背景”。

**Q：能给不同物品不同颜色吗？**
能，见 §4：`TooltipStyleManager.setStyle(Items.DIAMOND, 配色)`。物品提示框的两条绘制入口（`DrawContext#drawItemTooltip` 与 `HandledScreen#drawMouseoverTooltip`）都已接入，命中物品级配色时优先于全局配色。
注意：这里按**物品类型**（`Item`）区分，同一物品的所有实例共用一套配色；若需要更细的粒度（附魔、自定义模型数据、名字等），可以把 key 从 `Item` 换成对 `ItemStack` 的判断，改动集中在 `TooltipStyleManager` 与两个上下文 Mixin。

**Q：想让提示框背景带渐变？**
本实现按需求只做了纯色背景。若需要上下渐变背景，可把 `render` 内的背景调用点改用 `fillGradient`（或对我说一声，我可以加一个 `backgroundBottom` 颜色字段）。

**Q：边框想要纯色而不是（原版默认的）左右竖条渐变？**
用 `TooltipStyle.ofSolidBorder(背景色, 边框色)`，此时 `duplicateBorderGradient=false`，四条边框全部为同一纯色。

**Q：只改背景、边框保持原版？**
`TooltipStyleManager.setStyle(TooltipStyle.withBackground(0xCC0B1A2A));`

**Q：怎么快速调试某件物品的配色？**
开发环境下用 `gradlew runClient -DhbrMc.testItemStyle=minecraft:stone`，该物品会直接套用示例物品配色；日志里搜 `hbr_mc/tooltip` 能看到每次渲染解析到的配色。
