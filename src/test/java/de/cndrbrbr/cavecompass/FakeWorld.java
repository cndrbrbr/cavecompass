package de.cndrbrbr.cavecompass;

import java.util.HashSet;
import java.util.Set;

/**
 * A fake world for tests: every coordinate is loaded and within height
 * limits by default. Cave-air blocks are added explicitly with
 * {@link #addCaveAir}; plain (non-cave) air blocks can additionally be added
 * with {@link #addAir} for tests that need to shape an "open" area around a
 * cave-air candidate without it all counting as cave air itself.
 */
final class FakeWorld implements BlockLookup {

    private final Set<Long> caveAir = new HashSet<>();
    private final Set<Long> air = new HashSet<>();

    void addCaveAir(int x, int y, int z) {
        caveAir.add(key(x, y, z));
    }

    void addAir(int x, int y, int z) {
        air.add(key(x, y, z));
    }

    private static long key(int x, int y, int z) {
        return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
    }

    @Override
    public boolean isCaveAir(int x, int y, int z) {
        return caveAir.contains(key(x, y, z));
    }

    @Override
    public boolean isAirLike(int x, int y, int z) {
        long k = key(x, y, z);
        return caveAir.contains(k) || air.contains(k);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public int minHeight() {
        return -64;
    }

    @Override
    public int maxHeight() {
        return 320;
    }
}
