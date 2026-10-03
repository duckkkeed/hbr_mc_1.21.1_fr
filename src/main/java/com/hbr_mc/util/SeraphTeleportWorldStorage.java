package com.hbr_mc.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * World-save-backed storage for Seraph teleport toggles.
 * Stores per-world file at <world>/hbr_mc/seraph_tp.json with JSON mapping {"uuid": true, ...}.
 */
public final class SeraphTeleportWorldStorage {
    private static final Map<String, Map<UUID, Boolean>> WORLD_STATES = new ConcurrentHashMap<>();
    private static final Pattern ENTRY_PATTERN = Pattern.compile("\"([0-9a-fA-F\\-]{36})\"\s*:\s*(true|false)");

    private SeraphTeleportWorldStorage() {}

    private static Path getSaveFile(ServerWorld world) {
        String worldName = world.getRegistryKey().getValue().getPath();
        Path savesRoot = FabricLoader.getInstance().getGameDir().resolve("saves");
        Path dir = savesRoot.resolve(worldName).resolve("hbr_mc");
        return dir.resolve("seraph_tp.json");
    }

    private static synchronized void loadIfNeeded(ServerWorld world) {
        String key = world.getRegistryKey().getValue().toString();
        if (WORLD_STATES.containsKey(key)) return;

        Path saveFile = getSaveFile(world);
        Map<UUID, Boolean> map = new HashMap<>();
        try {
            if (Files.exists(saveFile)) {
                String content = new String(Files.readAllBytes(saveFile), StandardCharsets.UTF_8);
                Matcher m = ENTRY_PATTERN.matcher(content);
                while (m.find()) {
                    try {
                        UUID id = UUID.fromString(m.group(1));
                        boolean val = Boolean.parseBoolean(m.group(2));
                        if (val) map.put(id, true);
                    } catch (IllegalArgumentException ignored) {}
                }
            } else {
                Path parent = saveFile.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.createFile(saveFile);
                // initialize empty JSON
                Files.write(saveFile, "{}\n".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WORLD_STATES.put(key, map);
    }

    private static synchronized void save(ServerWorld world) {
        String key = world.getRegistryKey().getValue().toString();
        Map<UUID, Boolean> map = WORLD_STATES.getOrDefault(key, new HashMap<>());
        Path saveFile = getSaveFile(world);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            boolean first = true;
            for (Map.Entry<UUID, Boolean> e : map.entrySet()) {
                if (!first) sb.append(',').append('\n');
                first = false;
                sb.append("  \"").append(e.getKey().toString()).append("\": ").append(e.getValue());
            }
            if (!first) sb.append('\n');
            sb.append("}\n");
            Files.write(saveFile, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean get(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        loadIfNeeded(world);
        Map<UUID, Boolean> map = WORLD_STATES.get(world.getRegistryKey().getValue().toString());
        return map.getOrDefault(player.getUuid(), false);
    }

    public static void set(ServerPlayerEntity player, boolean value) {
        ServerWorld world = (ServerWorld) player.getWorld();
        loadIfNeeded(world);
        Map<UUID, Boolean> map = WORLD_STATES.get(world.getRegistryKey().getValue().toString());
        if (value) map.put(player.getUuid(), true);
        else map.remove(player.getUuid());
        save(world);
    }

    // convenience overloads
    public static boolean get(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) return get((ServerPlayerEntity) player);
        return false;
    }

    public static void set(PlayerEntity player, boolean value) {
        if (player instanceof ServerPlayerEntity) set((ServerPlayerEntity) player, value);
    }
}
