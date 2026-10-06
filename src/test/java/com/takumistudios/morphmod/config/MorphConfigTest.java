package com.takumistudios.morphmod.config;

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
		assertEquals("{ not json", Files.readString(file.resolveSibling("morphmod.json.bak")));
		assertEquals(MorphConfig.CURRENT_VERSION, MorphConfig.load(file).schemaVersion);
	}

	@Test void nonFiniteNumbersUseSafeDefaults() throws IOException {
		Path file = dir.resolve("morphmod.json");
		Files.writeString(file, "{\"maxHealthCap\":1e999,\"damageMultiplier\":\"NaN\",\"cooldownMultiplier\":\"Infinity\"}");
		MorphConfig config = MorphConfig.load(file);
		assertEquals(500.0, config.maxHealthCap);
		assertEquals(1.0, config.damageMultiplier);
		assertEquals(1.0, config.cooldownMultiplier);
	}
	@Test void schemaOneGainsCreeperOptionAndIsRewritten() throws IOException {
		Path file = dir.resolve("morphmod.json");
		Files.writeString(file, "{\"schemaVersion\":1,\"abilitiesBreakBlocks\":false}");
		MorphConfig config = MorphConfig.load(file);
		assertTrue(config.creeperBreaksBlocks);
		assertFalse(config.abilitiesBreakBlocks);
		assertTrue(Files.readString(file).contains("\"creeperBreaksBlocks\": true"));
		assertEquals(MorphConfig.CURRENT_VERSION, MorphConfig.load(file).schemaVersion);
	}
	@Test void futureSchemaIsNotOverwritten() throws IOException {
		Path file = dir.resolve("morphmod.json");
		String original = "{\"schemaVersion\":99,\"maxHealthCap\":30}";
		Files.writeString(file, original);
		assertEquals(500.0, MorphConfig.load(file).maxHealthCap);
		assertEquals(original, Files.readString(file));
	}
	@Test void nullJsonIsBackedUpAndNoTemporaryFileRemains() throws IOException {
		Path file = dir.resolve("morphmod.json");
		Files.writeString(file, "null");
		MorphConfig.load(file);
		assertEquals("null", Files.readString(file.resolveSibling("morphmod.json.bak")));
		try (var files = Files.list(dir)) { assertTrue(files.noneMatch(path -> path.toString().endsWith(".tmp"))); }
	}
}
