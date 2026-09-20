package de.cndrbrbr.cavecompass;

import org.bukkit.Material;
import org.bukkit.World;

/** Adapts a live Bukkit {@link World} to the {@link BlockLookup} interface. */
public final class BukkitWorldLookup implements BlockLookup {

    private final World world;

    public BukkitWorldLookup(World world) {
        this.world = world;
    }

    @Override
    public boolean isCaveAir(int x, int y, int z) {
        return world.getBlockAt(x, y, z).getType() == Material.CAVE_AIR;
    }

    @Override
    public boolean isAirLike(int x, int y, int z) {
        Material type = world.getBlockAt(x, y, z).getType();
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
