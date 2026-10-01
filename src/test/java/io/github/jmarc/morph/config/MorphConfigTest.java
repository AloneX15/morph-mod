package io.github.jmarc.morph.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MorphConfigTest {
	@TempDir
	Path dir;

	@Test
	void missingFileCreatesDefaults() {
		Path file = dir.resolve("morphmod.json");
		MorphConfig config = MorphConfig.load(file);
		assertTrue(config.requireUnlock);
		assertEquals(500.0, config.maxHealthCap);
		assertTrue(Files.exists(file));
	}

	@Test
	void readsAndClampsValues() throws IOException {
		Path file = dir.resolve("morphmod.json");
		Files.writeString(file, "{\"requireUnlock\": false, \"maxHealthCap\": 99999, \"cooldownMultiplier\": -3}");
		MorphConfig config = MorphConfig.load(file);
		assertFalse(config.requireUnlock);
		assertEquals(1024.0, config.maxHealthCap);
		assertEquals(0.0, config.cooldownMultiplier);
	}

	@Test
	void brokenJsonFallsBackToDefaults() throws IOException {
		Path file = dir.resolve("morphmod.json");
		Files.writeString(file, "{ not json");
		MorphConfig config = MorphConfig.load(file);
		assertEquals(1.0, config.damageMultiplier);
	}
}
