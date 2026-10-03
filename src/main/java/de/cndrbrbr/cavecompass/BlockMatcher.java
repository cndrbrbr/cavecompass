package de.cndrbrbr.cavecompass;

/**
 * Decides whether the block at a coordinate is something the compass should
 * point at — e.g. "an open cave" or "a bee nest". {@link CaveFinder} searches
 * for the nearest coordinate for which this returns true.
 */
@FunctionalInterface
public interface BlockMatcher {

    boolean matches(int x, int y, int z);
}
