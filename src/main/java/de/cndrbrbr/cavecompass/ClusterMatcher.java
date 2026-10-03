package de.cndrbrbr.cavecompass;

/**
 * Wraps another {@link BlockMatcher} so a block only counts if it is part
 * of a cluster: at least {@code minCount} matching blocks (including itself)
 * inside the cube of half-width {@code radius} around it. This lets the
 * compass look for an accumulation of e.g. bee nests instead of any single
 * one.
 *
 * Like {@link OpenCaveLookup}, the neighbourhood scan only runs for blocks
 * that already match, so the bulk of a search is unaffected.
 */
public final class ClusterMatcher implements BlockMatcher {

    private final BlockMatcher delegate;
    private final BlockLookup bounds;
    private final int radius;
    private final int minCount;

    public ClusterMatcher(BlockMatcher delegate, BlockLookup bounds, int radius, int minCount) {
        this.delegate = delegate;
        this.bounds = bounds;
        this.radius = radius;
        this.minCount = minCount;
    }

    @Override
    public boolean matches(int x, int y, int z) {
        if (!delegate.matches(x, y, z)) {
            return false;
        }
        if (minCount <= 1) {
            return true;
        }

        int count = 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }

                    int ny = y + dy;
                    if (ny < bounds.minHeight() || ny >= bounds.maxHeight()) {
                        continue;
                    }

                    if (delegate.matches(x + dx, ny, z + dz)) {
                        count++;
                        if (count >= minCount) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
