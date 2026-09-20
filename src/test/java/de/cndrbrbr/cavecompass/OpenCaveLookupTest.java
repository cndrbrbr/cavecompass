package de.cndrbrbr.cavecompass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenCaveLookupTest {

    @Test
    void rejectsNonCaveAirRegardlessOfNeighbors() {
        FakeWorld world = new FakeWorld();
        // Not cave air at all, even though everything around it is open.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.addAir(dx, dy, dz);
                }
            }
        }
        OpenCaveLookup lookup = new OpenCaveLookup(world, 1, 1);
        assertFalse(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void rejectsIsolatedCaveAirPocket() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0); // a single air block surrounded by solid stone

        OpenCaveLookup lookup = new OpenCaveLookup(world, 1, 5);
        assertFalse(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void acceptsAtExactlyTheThreshold() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        // Exactly 5 open neighbours out of 26 in the 3x3x3 cube.
        world.addAir(1, 0, 0);
        world.addAir(-1, 0, 0);
        world.addAir(0, 1, 0);
        world.addAir(0, -1, 0);
        world.addAir(0, 0, 1);

        OpenCaveLookup lookup = new OpenCaveLookup(world, 1, 5);
        assertTrue(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void rejectsOneBelowTheThreshold() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        // Only 4 open neighbours this time.
        world.addAir(1, 0, 0);
        world.addAir(-1, 0, 0);
        world.addAir(0, 1, 0);
        world.addAir(0, -1, 0);

        OpenCaveLookup lookup = new OpenCaveLookup(world, 1, 5);
        assertFalse(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void acceptsACenterOfALargeOpenCavern() {
        FakeWorld world = new FakeWorld();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    world.addCaveAir(dx, dy, dz);
                }
            }
        }

        OpenCaveLookup lookup = new OpenCaveLookup(world, 2, 20);
        assertTrue(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void doesNotCountNeighborsBeyondHeightLimits() {
        // World's usable height is only y in [0, 1): anything at y=1 or
        // beyond must never be counted as an open neighbour, even if it's
        // marked as air in the fake world.
        BlockLookup restricted = new BlockLookup() {
            private final FakeWorld inner = new FakeWorld();

            {
                inner.addCaveAir(0, 0, 0);
                inner.addAir(1, 0, 0);
                inner.addAir(0, 1, 0); // outside height range below
            }

            @Override
            public boolean isCaveAir(int x, int y, int z) {
                return inner.isCaveAir(x, y, z);
            }

            @Override
            public boolean isAirLike(int x, int y, int z) {
                return inner.isAirLike(x, y, z);
            }

            @Override
            public boolean isChunkLoaded(int chunkX, int chunkZ) {
                return true;
            }

            @Override
            public int minHeight() {
                return 0;
            }

            @Override
            public int maxHeight() {
                return 1;
            }
        };

        OpenCaveLookup lookup = new OpenCaveLookup(restricted, 1, 2);
        // Only (1,0,0) is a countable open neighbour; (0,1,0) is out of range.
        assertFalse(lookup.isCaveAir(0, 0, 0));
    }

    @Test
    void delegatesPassThroughMethods() {
        FakeWorld world = new FakeWorld();
        world.addAir(5, 5, 5);
        OpenCaveLookup lookup = new OpenCaveLookup(world, 1, 1);

        assertTrue(lookup.isAirLike(5, 5, 5));
        assertTrue(lookup.isChunkLoaded(0, 0));
        assertEquals(world.minHeight(), lookup.minHeight());
        assertEquals(world.maxHeight(), lookup.maxHeight());
    }
}
