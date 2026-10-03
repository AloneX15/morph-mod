package com.takumistudios.morphmod.character;

import com.google.gson.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Bounded, deterministic resource archive. Parsing never writes archive paths to disk. */
public record CharacterBundle(CharacterDefinition definition, Map<String, byte[]> files, byte[] archive, String hash) {
    public static final int MAX_CHARACTER = 16 * 1024 * 1024;
    public static final int MAX_CATALOG = 64 * 1024 * 1024;
    public static final int FRAGMENT = 32768;
    public static CharacterBundle create(Map<String, byte[]> input) throws IOException {
        TreeMap<String, byte[]> files = new TreeMap<>();
        long total = 0;
        for (var e : input.entrySet()) {
            CharacterDefinition.path(e.getKey());
            total += e.getValue().length;
            if (total > MAX_CHARACTER || files.size() >= 64) throw new IOException("Character resources exceed limit");
            files.put(e.getKey(), e.getValue());
        }
        byte[] manifest = required(files, "character.json");
        JsonObject json = JsonParser.parseString(new String(manifest, StandardCharsets.UTF_8)).getAsJsonObject();
        byte[] model = required(files, CharacterDefinition.path(json.get("model").getAsString()));
        byte[] anim = required(files, CharacterDefinition.path(json.get("animations").getAsString()));
        byte[] png = required(files, CharacterDefinition.path(json.get("texture").getAsString()));
        if (png.length < 24 || ByteBuffer.wrap(png).getLong() != 0x89504e470d0a1a0aL) throw new IOException("Invalid PNG");
        int width = ByteBuffer.wrap(png, 16, 4).getInt(), height = ByteBuffer.wrap(png, 20, 4).getInt();
        if (width < 1 || height < 1 || width > 4096 || height > 4096) throw new IOException("PNG dimensions exceed limit");
        if (javax.imageio.ImageIO.read(new ByteArrayInputStream(png)) == null) throw new IOException("Unreadable PNG");
        CharacterDefinition def = CharacterDefinition.parse(manifest, model, anim);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (var e : files.entrySet()) {
                ZipEntry entry = new ZipEntry(e.getKey()); entry.setTime(0);
                zip.putNextEntry(entry); zip.write(e.getValue()); zip.closeEntry();
            }
        }
        byte[] archive = bytes.toByteArray();
        if (archive.length > MAX_CHARACTER) throw new IOException("Archive exceeds limit");
        return new CharacterBundle(def, Collections.unmodifiableMap(files), archive, digest(archive));
    }
    public static CharacterBundle decode(byte[] archive, String expectedHash) throws IOException {
        if (archive.length > MAX_CHARACTER || !digest(archive).equals(expectedHash)) throw new IOException("Invalid archive hash or size");
        Map<String, byte[]> files = new HashMap<>();
        int total = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = CharacterDefinition.path(entry.getName());
                byte[] data = zip.readNBytes(MAX_CHARACTER - total + 1);
                total += data.length;
                if (total > MAX_CHARACTER || files.size() >= 64 || files.putIfAbsent(name, data) != null) throw new IOException("Invalid archive entries");
            }
        }
        CharacterBundle validated = create(files);
        return new CharacterBundle(validated.definition, validated.files, archive, expectedHash);
    }
    private static byte[] required(Map<String, byte[]> files, String name) throws IOException {
        byte[] bytes = files.get(name); if (bytes == null) throw new IOException("Missing resource: " + name); return bytes;
    }
    public static String digest(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
