package io.github.jmarc.morph.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side balance options, stored as JSON in {@code config/morphmod.json}.
 */
public final class MorphConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("morphmod/config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/** If false, every morphable mob is unlocked for every player. */
	public boolean requireUnlock = true;
	/** Killing a mob unlocks its form for the killer. */
	public boolean unlockOnKill = true;
	/** Upper bound for a morph's max health (the Warden has 500 in vanilla). */
	public double maxHealthCap = 500.0;
	/** Multiplier applied to every morph's attack damage. */
	public double damageMultiplier = 1.0;
	/** Multiplier applied to every ability cooldown. */
	public double cooldownMultiplier = 1.0;
	/** Allows flying morphs (bat, ghast...) to fly in survival. */
	public boolean allowFlight = true;
	/** Creeper explosions and ghast fireballs break blocks (still respects the mobGriefing rule). */
	public boolean abilitiesBreakBlocks = true;

	public static MorphConfig load(Path file) {
		MorphConfig config = new MorphConfig();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				MorphConfig read = GSON.fromJson(reader, MorphConfig.class);
				if (read != null) {
					config = read;
				}
			} catch (IOException | JsonParseException e) {
				LOGGER.error("Could not read {}, using defaults", file, e);
			}
		}
		config.sanitize();
		config.save(file);
		return config;
	}

	public void save(Path file) {
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Could not write {}", file, e);
		}
	}

	void sanitize() {
		maxHealthCap = Math.clamp(maxHealthCap, 1.0, 1024.0);
		damageMultiplier = Math.clamp(damageMultiplier, 0.0, 100.0);
		cooldownMultiplier = Math.clamp(cooldownMultiplier, 0.0, 100.0);
	}
}
