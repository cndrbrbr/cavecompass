package de.cndrbrbr.cavecompass;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CaveFinderTest {

    /**
     * A fake world: every coordinate is loaded and within height limits by
     * default, and "cave air" exists only at the coordinates explicitly
     * added.
     */
    private static final class FakeWorld implements BlockLookup {
        private final Set<Long> caveAir = new HashSet<>();

        void addCave(int x, int y, int z) {
            caveAir.add(key(x, y, z));
        }

        private static long key(int x, int y, int z) {
            return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
        }

        @Override
        public boolean isCaveAir(int x, int y, int z) {
            return caveAir.contains(key(x, y, z));
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

    @Test
    void findsNothingWhenNoCaveInRange() {
        FakeWorld world = new FakeWorld();
        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, 0, 0, 8, true);
        assertTrue(result.isEmpty());
    }

    @Test
    void findsAnAdjacentCave() {
        FakeWorld world = new FakeWorld();
        world.addCave(1, 0, 0);
        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, 0, 0, 8, true);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().x());
        assertEquals(0, result.get().y());
        assertEquals(0, result.get().z());
        assertEquals(1.0, result.get().distance(), 1e-9);
    }

    /**
     * Regression test for the shell-termination logic: a diagonal point in
     * an earlier shell can have a larger true (Euclidean) distance than an
     * axis-aligned point in a later shell. (3,3,3) is Chebyshev-shell 3 but
     * Euclidean distance sqrt(27) ≈ 5.196; (4,0,0) is Chebyshev-shell 4 but
     * Euclidean distance 4.0 — genuinely closer. A naive "stop at first
     * shell containing a hit" implementation would wrongly return (3,3,3).
     */
    @Test
    void prefersTrueEuclideanNearestOverEarlierShellHit() {
        FakeWorld world = new FakeWorld();
        world.addCave(3, 3, 3);
        world.addCave(4, 0, 0);

        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, 0, 0, 8, true);

        assertTrue(result.isPresent());
        assertEquals(4, result.get().x());
        assertEquals(0, result.get().y());
        assertEquals(0, result.get().z());
        assertEquals(4.0, result.get().distance(), 1e-9);
    }

    @Test
    void respectsHeightLimits() {
        FakeWorld world = new FakeWorld();
        world.addCave(0, -70, 0); // below minHeight(-64)
        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, -60, 0, 16, true);
        assertTrue(result.isEmpty());
    }

    @Test
    void skipsUnloadedChunksWhenRequested() {
        BlockLookup world = new BlockLookup() {
            @Override
            public boolean isCaveAir(int x, int y, int z) {
                return x == 5 && y == 0 && z == 0;
            }

            @Override
            public boolean isChunkLoaded(int chunkX, int chunkZ) {
                return false; // pretend nothing is loaded
            }

            @Override
            public int minHeight() {
                return -64;
            }

            @Override
            public int maxHeight() {
                return 320;
            }
        };

        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, 0, 0, 8, true);
        assertTrue(result.isEmpty());
    }

    @Test
    void doesNotSearchBeyondRadius() {
        FakeWorld world = new FakeWorld();
        world.addCave(100, 0, 0);
        Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(world, 0, 0, 0, 8, true);
        assertTrue(result.isEmpty());
    }
}
