package de.cndrbrbr.cavecompass;

import java.util.Optional;

/**
 * Finds the nearest cave-air block to a given point.
 *
 * Searches in expanding cubic "shells" (Chebyshev-distance rings) around the
 * origin so that, in the common case where a cave is nearby, only a small
 * fraction of the search radius actually needs to be scanned before
 * stopping. A shell at Chebyshev distance {@code s} contains points whose
 * true Euclidean distance ranges from {@code s} (face centre) up to
 * {@code s * sqrt(3)} (corner), so finding a candidate in shell {@code s}
 * does not by itself guarantee it is the true nearest one — a later shell
 * could still contain a closer, more axis-aligned point. To stay correct,
 * the search keeps expanding until the current shell index is at least the
 * Euclidean distance of the best candidate found so far, at which point no
 * later shell can possibly contain anything closer.
 */
public final class CaveFinder {

    private CaveFinder() {
    }

    public record Result(int x, int y, int z, double distance) {
    }

    public static Optional<Result> findNearestCave(
            BlockLookup lookup,
            int originX,
            int originY,
            int originZ,
            int radius,
            boolean onlyLoadedChunks) {

        Result best = null;

        for (int shell = 0; shell <= radius; shell++) {

            for (int dx = -shell; dx <= shell; dx++) {
                for (int dy = -shell; dy <= shell; dy++) {
                    for (int dz = -shell; dz <= shell; dz++) {

                        // Only the surface of the cube is new at this shell —
                        // its interior was already covered by smaller shells.
                        int chebyshev = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                        if (chebyshev != shell) {
                            continue;
                        }

                        int x = originX + dx;
                        int y = originY + dy;
                        int z = originZ + dz;

                        if (y < lookup.minHeight() || y >= lookup.maxHeight()) {
                            continue;
                        }

                        if (onlyLoadedChunks && !lookup.isChunkLoaded(x >> 4, z >> 4)) {
                            continue;
                        }

                        if (!lookup.isCaveAir(x, y, z)) {
                            continue;
                        }

                        double distance = Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
                        if (best == null || distance < best.distance()) {
                            best = new Result(x, y, z, distance);
                        }
                    }
                }
            }

            if (best != null && shell >= Math.ceil(best.distance())) {
                break;
            }
        }

        return Optional.ofNullable(best);
    }
}
