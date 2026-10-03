package de.cndrbrbr.cavecompass;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClusterMatcherTest {

    /** Uses the fake world's cave-air set as "the searched block type". */
    private static BlockMatcher nests(FakeWorld world) {
        return world::isCaveAir;
    }

    @Test
    void singleBlockMatchesWithClusterSizeOne() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        assertTrue(new ClusterMatcher(nests(world), world, 8, 1).matches(0, 0, 0));
    }

    @Test
    void rejectsLoneBlockWhenClusterRequired() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        assertFalse(new ClusterMatcher(nests(world), world, 8, 2).matches(0, 0, 0));
    }

    @Test
    void acceptsAtExactlyTheClusterSize() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        world.addCaveAir(5, 2, -3);
        world.addCaveAir(-8, 0, 8);
        assertTrue(new ClusterMatcher(nests(world), world, 8, 3).matches(0, 0, 0));
        assertFalse(new ClusterMatcher(nests(world), world, 8, 4).matches(0, 0, 0));
    }

    @Test
    void ignoresBlocksOutsideTheClusterRadius() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(0, 0, 0);
        world.addCaveAir(9, 0, 0);
        assertFalse(new ClusterMatcher(nests(world), world, 8, 2).matches(0, 0, 0));
    }

    @Test
    void nonMatchingBlockNeverMatches() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(1, 0, 0);
        world.addCaveAir(2, 0, 0);
        assertFalse(new ClusterMatcher(nests(world), world, 8, 1).matches(0, 0, 0));
    }

    @Test
    void finderSkipsNearLoneNestForFartherGroup() {
        FakeWorld world = new FakeWorld();
        world.addCaveAir(3, 0, 0);       // lone nest, close by
        world.addCaveAir(30, 0, 0);      // group of three, farther away
        world.addCaveAir(32, 1, 0);
        world.addCaveAir(31, 0, 2);

        BlockMatcher matcher = new ClusterMatcher(nests(world), world, 4, 3);
        Optional<CaveFinder.Result> result = CaveFinder.findNearest(world, matcher, 0, 0, 0, 40, true);

        assertTrue(result.isPresent());
        assertEquals(30, result.get().x());
    }
}
