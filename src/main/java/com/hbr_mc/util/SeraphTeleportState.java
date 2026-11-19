package com.hbr_mc.util;

import com.hbr_mc.HBR_MC;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PersistentState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Seraph 传送开关的世界存档状态。
 * <p>World-save-backed state for the Seraph teleport toggle.
 *
 * <p>存储位置由 {@link net.minecraft.world.PersistentStateManager} 决定：固定取<b>主世界</b>
 * 的存档数据目录，因此整份存档（主世界 / 下界 / 末地）共用一条记录，文件为
 * {@code <world>/data/hbr_mc_seraph_tp.dat}。这会随世界保存、随世界复制，也会被世界备份带走。
 *
 * <p>NBT 结构（无历史版本，纯自定义数据）：
 * <pre>{@code
 * { Enabled: [ "uuid-1", "uuid-2", ... ] }
 * }</pre>
 * 只记录"已开启"的玩家；关掉即从列表移除，因此不存在 {@code false} 条目。
 */
public final class SeraphTeleportState extends PersistentState {
    /** 存档数据 id：决定文件名 {@code <world>/data/<id>.dat}。 */
    private static final String DATA_ID = "hbr_mc_seraph_tp";
    /** UUID 字符串列表的键名。 */
    private static final String KEY_ENABLED = "Enabled";

    /**
     * {@link DataFixTypes} 只决定读档时走哪套 DFU 修复；自定义数据没有历史结构，
     * 任意 {@code SAVED_DATA_*} 都等价，这里挑一个语义最接近"世界存档数据的"。
     */
    private static final PersistentState.Type<SeraphTeleportState> TYPE = new PersistentState.Type<>(
            SeraphTeleportState::new, SeraphTeleportState::fromNbt, DataFixTypes.SAVED_DATA_MAP_DATA);

    // ------------------------------------------------------------ 旧版 JSON 迁移 --

    /** 1.12.2 风格的旧文件：本工程上一版写在存档目录之外的 JSON。 */
    private static final String LEGACY_FILE_NAME = "seraph_tp.json";
    /** 旧版按维度分文件，这三个目录名是 {@code RegistryKey#getValue()#getPath()} 的产物。 */
    private static final List<String> LEGACY_DIMENSION_DIRS = List.of("overworld", "the_nether", "the_end");
    private static final Pattern LEGACY_ENTRY_PATTERN = Pattern.compile("\"([0-9a-fA-F\\-]{36})\"\\s*:\\s*(true|false)");

    /** 已开启传送的玩家 UUID。 */
    private final Set<UUID> enabled = new HashSet<>();
    /** 本会话是否已经尝试过旧版迁移（不写入 NBT）。 */
    private boolean legacyChecked;

    private SeraphTeleportState() {}

    private static SeraphTeleportState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SeraphTeleportState state = new SeraphTeleportState();
        NbtList list = nbt.getList(KEY_ENABLED, NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) {
            String raw = list.getString(i);
            try {
                state.enabled.add(UUID.fromString(raw));
            } catch (IllegalArgumentException e) {
                HBR_MC.LOGGER.warn("[Seraph] 传送开关存档里有非法 UUID：{}", raw);
            }
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (UUID id : this.enabled) {
            list.add(NbtString.of(id.toString()));
        }
        nbt.put(KEY_ENABLED, list);
        return nbt;
    }

    // ---------------------------------------------------------------- 存取入口 --

    /** 取（必要时创建）本存档唯一的状态实例；只会用主世界的数据目录。 */
    public static SeraphTeleportState get(MinecraftServer server) {
        SeraphTeleportState state = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, DATA_ID);
        state.tryImportLegacy(server);
        return state;
    }

    /** 该玩家是否开启了传送。 */
    public boolean isEnabled(UUID playerId) {
        return this.enabled.contains(playerId);
    }

    /** 写入开关；返回是否真的发生了变化（用于决定要不要 {@link #markDirty()}）。 */
    public boolean setEnabled(UUID playerId, boolean value) {
        return value ? this.enabled.add(playerId) : this.enabled.remove(playerId);
    }

    // ------------------------------------------------------------ 便捷静态入口 --

    /** 服务端玩家：读开关。 */
    public static boolean isTeleportEnabled(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        return server != null && get(server).isEnabled(player.getUuid());
    }

    /** 服务端玩家：写开关并标脏（下次世界保存时落盘）。 */
    public static void setTeleportEnabled(ServerPlayerEntity player, boolean value) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        SeraphTeleportState state = get(server);
        if (state.setEnabled(player.getUuid(), value)) {
            state.markDirty();
        }
    }

    /** 客户端安全版本：客户端从不持有存档状态，恒为未开启。 */
    public static boolean isTeleportEnabled(PlayerEntity player) {
        return player instanceof ServerPlayerEntity serverPlayer && isTeleportEnabled(serverPlayer);
    }

    /** 客户端安全版本：客户端不写存档。 */
    public static void setTeleportEnabled(PlayerEntity player, boolean value) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            setTeleportEnabled(serverPlayer, value);
        }
    }

    // ------------------------------------------------------------- 旧版数据迁移 --

    /**
     * 首次读档时尝试导入上一版写在存档目录之外的 JSON。
     *
     * <p>只在"本存档还没有任何记录"时执行，导入成功后把旧文件改名为 {@code *.imported}，
     * 因此重复启动不会反复导入。旧文件缺失/损坏都只是跳过，不会影响传送功能。
     */
    private void tryImportLegacy(MinecraftServer server) {
        if (this.legacyChecked) return;
        this.legacyChecked = true;
        if (!this.enabled.isEmpty()) return;

        int imported = 0;
        for (Path file : legacyFiles(server)) {
            imported += importLegacyFile(file);
        }
        if (imported > 0) {
            this.markDirty();
            HBR_MC.LOGGER.info("[Seraph] 已从旧版 JSON 迁移 {} 条传送开关记录", imported);
        }
    }

    private static List<Path> legacyFiles(MinecraftServer server) {
        List<Path> files = new ArrayList<>();
        // 旧版把文件写在 <world>/hbr_mc/ 的设想路径（兼容手动搬过文件的情况）
        files.add(server.getSavePath(WorldSavePath.ROOT).resolve("hbr_mc").resolve(LEGACY_FILE_NAME));
        // 旧版实际落盘的位置：<gameDir>/saves/<维度>/hbr_mc/seraph_tp.json
        Path saves = FabricLoader.getInstance().getGameDir().resolve("saves");
        for (String dimension : LEGACY_DIMENSION_DIRS) {
            files.add(saves.resolve(dimension).resolve("hbr_mc").resolve(LEGACY_FILE_NAME));
        }
        return files;
    }

    private int importLegacyFile(Path file) {
        if (!Files.isRegularFile(file)) return 0;

        int before = this.enabled.size();
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            Matcher matcher = LEGACY_ENTRY_PATTERN.matcher(content);
            while (matcher.find()) {
                if (!"true".equalsIgnoreCase(matcher.group(2))) continue;
                try {
                    this.enabled.add(UUID.fromString(matcher.group(1)));
                } catch (IllegalArgumentException ignored) {
                    // 旧文件里的坏行直接跳过
                }
            }
            Files.move(file, file.resolveSibling(file.getFileName() + ".imported"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            HBR_MC.LOGGER.warn("[Seraph] 迁移旧版传送开关失败：{}", file, e);
            return 0;
        }
        return this.enabled.size() - before;
    }
}
