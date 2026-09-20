package de.cndrbrbr.cavecompass;

/**
 * Wraps another {@link BlockLookup} so a cave-air block only counts as a
 * cave if enough of the space around it is also open — filtering out thin
 * crevices and single-block pockets in favour of real, "airy" caverns.
 *
 * The expensive neighbourhood scan only runs for blocks that already pass
 * the cheap {@code isCaveAir} check on the underlying lookup, so most of a
 * search (which mostly walks through solid stone) is unaffected.
 */
public final class OpenCaveLookup implements BlockLookup {

    private final BlockLookup delegate;
    private final int radius;
    private final int minOpenBlocks;

    public OpenCaveLookup(BlockLookup delegate, int radius, int minOpenBlocks) {
        this.delegate = delegate;
        this.radius = radius;
        this.minOpenBlocks = minOpenBlocks;
    }

    @Override
    public boolean isCaveAir(int x, int y, int z) {
        if (!delegate.isCaveAir(x, y, z)) {
            return false;
        }

        int open = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }

                    int ny = y + dy;
                    if (ny < delegate.minHeight() || ny >= delegate.maxHeight()) {
                        continue;
                    }

                    if (delegate.isAirLike(x + dx, ny, z + dz)) {
                        open++;
                        if (open >= minOpenBlocks) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isAirLike(int x, int y, int z) {
        return delegate.isAirLike(x, y, z);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return delegate.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public int minHeight() {
        return delegate.minHeight();
    }

    @Override
    public int maxHeight() {
        return delegate.maxHeight();
    }
}
