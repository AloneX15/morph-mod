package com.takumistudios.morphmod.character;

import java.io.IOException;
import java.util.BitSet;

/** Validates offsets, duplicates and truncation before accepting an asset transfer. */
public final class FragmentAssembly {
    private final byte[] bytes;
    private final BitSet received = new BitSet();
    private final int count;
    public FragmentAssembly(int size) {
        if (size < 1 || size > CharacterBundle.MAX_CHARACTER) throw new IllegalArgumentException("Invalid transfer size");
        bytes = new byte[size]; count = (size + CharacterBundle.FRAGMENT - 1) / CharacterBundle.FRAGMENT;
    }
    public void accept(int index, byte[] data) throws IOException {
        if (index < 0 || index >= count || received.get(index)) throw new IOException("Invalid or duplicate fragment");
        int offset = index * CharacterBundle.FRAGMENT;
        if (data.length != Math.min(CharacterBundle.FRAGMENT, bytes.length - offset)) throw new IOException("Truncated fragment");
        System.arraycopy(data, 0, bytes, offset, data.length); received.set(index);
    }
    public boolean complete() { return received.cardinality() == count; }
    public byte[] finish() throws IOException { if (!complete()) throw new IOException("Incomplete transfer"); return bytes; }
}
