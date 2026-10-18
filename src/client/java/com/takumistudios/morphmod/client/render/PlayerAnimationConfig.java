package com.takumistudios.morphmod.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.takumistudios.morphmod.MorphMod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.Executors;
import net.fabricmc.loader.api.FabricLoader;

/** Client-local preference, read once off the game thread. Restart to apply edits. */
public final class PlayerAnimationConfig {
    private static volatile boolean enabled = true;
    private PlayerAnimationConfig() { }
    public static boolean enabled() { return enabled; }
    public static void init() {
        var io = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "Morph-ClientConfig-IO"); thread.setDaemon(true); return thread;
        });
        io.execute(() -> {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("morphmod-client.json");
            try {
                if (Files.exists(file)) {
                    try {
                        JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                        if (!json.has("schemaVersion") || !json.get("schemaVersion").toString().equals("1")
                            || !json.has("playerWalkAnimation") || !json.get("playerWalkAnimation").isJsonPrimitive()
                            || !json.getAsJsonPrimitive("playerWalkAnimation").isBoolean()) throw new IllegalArgumentException("Invalid client animation configuration");
                        enabled = json.get("playerWalkAnimation").getAsBoolean();
                        return;
                    } catch (RuntimeException invalid) {
                        Files.copy(file, file.resolveSibling(file.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                        MorphMod.LOGGER.warn("Invalid client animation config; saved .bak and restored defaults", invalid);
                    }
                }
                Files.createDirectories(file.getParent());
                Path temporary = Files.createTempFile(file.getParent(), "morphmod-client-", ".tmp");
                try {
                    Files.writeString(temporary, "{\n  \"schemaVersion\": 1,\n  \"playerWalkAnimation\": true\n}\n", StandardCharsets.UTF_8);
                    try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
                    catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
                } finally { Files.deleteIfExists(temporary); }
            } catch (IOException | RuntimeException error) {
                MorphMod.LOGGER.warn("Could not load player animation configuration; using defaults", error);
            }
        });
        io.shutdown();
    }
}
