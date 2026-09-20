package de.cndrbrbr.cavecompass;

/**
 * Abstracts "is there cave air at this coordinate" away from the Bukkit World
 * API so the search algorithm in {@link CaveFinder} can be unit-tested
 * without a running server.
 */
public interface BlockLookup {

    boolean isCaveAir(int x, int y, int z);

    boolean isChunkLoaded(int chunkX, int chunkZ);

    int minHeight();

    int maxHeight();
}
