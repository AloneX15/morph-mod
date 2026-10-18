package com.takumistudios.morphmod.animation;

import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class BundledProfilesTest {
    private static final String EARLIER = "{\"schemaVersion\":1,\"id\":\"morphmod:dedsafio_4\",\"name\":\"Dedsafio 4\",\"animations\":\"dedsafio_4.animation.json\",\"bindings\":{\"walk\":\"idle\",\"run\":\"idle\",\"sneak_walk\":\"sneaking\"},\"boneAliases\":{}}\n";
    private byte[] bundled(String path) throws Exception {
        try (var input = getClass().getResourceAsStream("/animations/" + path)) { assertNotNull(input); return input.readAllBytes(); }
    }
    @Test void missingProfilesAreCopied(@TempDir Path root) throws Exception {
        AnimationCatalog.install(root.resolve("dedsafio_3"), "dedsafio_3");
        assertArrayEquals(bundled("dedsafio_3/profile.json"), Files.readAllBytes(root.resolve("dedsafio_3/profile.json")));
        assertArrayEquals(bundled("dedsafio_3/dedsafio_3.animation.json"), Files.readAllBytes(root.resolve("dedsafio_3/dedsafio_3.animation.json")));
    }
    @Test void uneditedEarlierReleaseIsRefreshed(@TempDir Path root) throws Exception {
        Path folder = root.resolve("dedsafio_4"); AnimationCatalog.install(folder, "dedsafio_4");
        Files.writeString(folder.resolve("profile.json"), EARLIER);
        AnimationCatalog.install(folder, "dedsafio_4");
        assertArrayEquals(bundled("dedsafio_4/profile.json"), Files.readAllBytes(folder.resolve("profile.json")));
    }
    @Test void editedProfilesAreNeverOverwritten(@TempDir Path root) throws Exception {
        Path folder = root.resolve("dedsafio_4"); AnimationCatalog.install(folder, "dedsafio_4");
        Files.writeString(folder.resolve("profile.json"), EARLIER);
        Files.writeString(folder.resolve("dedsafio_4.animation.json"), "{\"format_version\":\"1.8.0\",\"animations\":{}}");
        AnimationCatalog.install(folder, "dedsafio_4");
        assertEquals(EARLIER, Files.readString(folder.resolve("profile.json")));
    }
}
