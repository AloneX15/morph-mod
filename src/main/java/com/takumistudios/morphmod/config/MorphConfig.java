package com.takumistudios.morphmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server options. Game callers use the IO executor; synchronous load is usable by tests. */
public final class MorphConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("morphmod/config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r, "Morph-IO");
		thread.setDaemon(true);
		return thread;
	});
	public static final int CURRENT_VERSION = 1;
	private static final long MAX_CONFIG_BYTES = 64 * 1024;
	public int schemaVersion = CURRENT_VERSION;
	public boolean requireUnlock = true;
	public boolean unlockOnKill = true;
	public double maxHealthCap = 500.0;
	public double damageMultiplier = 1.0;
	public double cooldownMultiplier = 1.0;
	public boolean allowFlight = true;
	public boolean abilitiesBreakBlocks = false;

	public static CompletableFuture<MorphConfig> loadAsync(Path file) {
		return CompletableFuture.supplyAsync(() -> load(file), IO);
	}
	public static MorphConfig load(Path file) {
		MorphConfig config = new MorphConfig();
		boolean regenerate = !Files.exists(file);
		if (!regenerate) {
			try {
				if (Files.size(file) > MAX_CONFIG_BYTES) throw new JsonParseException("Config is too large");
				JsonObject json = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
				if (json == null) throw new JsonParseException("Config must be an object");
				int schema = json.has("schemaVersion") ? json.get("schemaVersion").getAsInt() : 0;
				if (schema > CURRENT_VERSION) {
					LOGGER.warn("Config {} uses a newer schema; using defaults without overwriting it", file);
					return config;
				}
				if (schema < 0) throw new JsonParseException("Invalid schema version");
				config = GSON.fromJson(json, MorphConfig.class);
				config.schemaVersion = CURRENT_VERSION;
				config.sanitize();
				regenerate = schema < CURRENT_VERSION;
			} catch (IOException | RuntimeException e) {
				config = new MorphConfig();
				LOGGER.warn("Invalid config {}; preserving a backup and using defaults", file, e);
				try {
					Path backup = file.resolveSibling(file.getFileName() + ".bak");
					if (Files.exists(backup)) backup = file.resolveSibling(file.getFileName() + "." + System.nanoTime() + ".bak");
					Files.copy(file, backup);
					regenerate = true;
				} catch (IOException backupError) {
					LOGGER.error("Cannot back up {}; original left untouched", file, backupError);
					return config;
				}
			}
		}
		config.sanitize();
		if (regenerate) config.save(file);
		return config;
	}
	private void save(Path file) {
		Path tmp = null;
		try {
			Path target = file.toAbsolutePath();
			Files.createDirectories(target.getParent());
			tmp = Files.createTempFile(target.getParent(), "morphmod-", ".tmp");
			Files.writeString(tmp, GSON.toJson(this), StandardCharsets.UTF_8);
			// If atomic replacement is unsupported, leave the original intact and log the failure.
			Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			LOGGER.error("Cannot atomically save config {}", file, e);
		} finally {
			if (tmp != null) {
				try { Files.deleteIfExists(tmp); }
				catch (IOException e) { LOGGER.warn("Cannot remove config temporary file {}", tmp, e); }
			}
		}
	}
	void sanitize() {
		maxHealthCap = finiteClamp(maxHealthCap, 1.0, 1024.0, 500.0);
		damageMultiplier = finiteClamp(damageMultiplier, 0.0, 100.0, 1.0);
		cooldownMultiplier = finiteClamp(cooldownMultiplier, 0.0, 100.0, 1.0);
	}
	private static double finiteClamp(double value, double min, double max, double fallback) {
		return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
	}
	public static void shutdown() {
		IO.shutdown();
		try {
			if (!IO.awaitTermination(5, TimeUnit.SECONDS)) LOGGER.warn("Pending Morph IO exceeded shutdown budget");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Interrupted while waiting for Morph IO", e);
		}
	}
}
