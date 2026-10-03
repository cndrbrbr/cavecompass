package de.cndrbrbr.cavecompass;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts a live Bukkit {@link World} to the {@link BlockLookup} interface.
 *
 * Block types are read from {@link ChunkSnapshot}s cached per chunk, which is
 * much cheaper than {@code world.getBlockAt(...)} for the many thousands of
 * blocks a search with a large radius touches. An instance is meant to be
 * used for a single search and then discarded, so the snapshots never go
 * stale. With {@code onlyLoadedChunks}, blocks in unloaded chunks read as
 * solid stone instead of forcing the chunk to load.
 */
public final class BukkitWorldLookup implements BlockLookup {

    private final World world;
    private final boolean onlyLoadedChunks;
    private final Map<Long, ChunkSnapshot> snapshots = new HashMap<>();

    public BukkitWorldLookup(World world, boolean onlyLoadedChunks) {
        this.world = world;
        this.onlyLoadedChunks = onlyLoadedChunks;
    }

    public Material typeAt(int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);

        ChunkSnapshot snapshot = snapshots.get(key);
        if (snapshot == null) {
            if (onlyLoadedChunks && !world.isChunkLoaded(chunkX, chunkZ)) {
                return Material.STONE;
            }
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            snapshot = chunk.getChunkSnapshot(false, false, false);
            snapshots.put(key, snapshot);
        }
        return snapshot.getBlockType(x & 15, y, z & 15);
    }

    /** A matcher for one specific block type, e.g. {@link Material#BEE_NEST}. */
    public BlockMatcher matching(Material material) {
        return (x, y, z) -> typeAt(x, y, z) == material;
    }

    @Override
    public boolean isCaveAir(int x, int y, int z) {
        return typeAt(x, y, z) == Material.CAVE_AIR;
    }

    @Override
    public boolean isAirLike(int x, int y, int z) {
        Material type = typeAt(x, y, z);
        return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return world.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public int minHeight() {
        return world.getMinHeight();
    }

    @Override
    public int maxHeight() {
        return world.getMaxHeight();
    }
}
