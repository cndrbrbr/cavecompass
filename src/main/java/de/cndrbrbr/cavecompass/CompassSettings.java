package de.cndrbrbr.cavecompass;

import java.util.function.Function;

/**
 * What a single compass searches for: either open caves (the default) or a
 * block type, within a search radius, optionally requiring a cluster of at
 * least {@code clusterSize} such blocks close together.
 *
 * @param target      {@link #CAVE} or a lower-case block key such as "bee_nest"
 * @param radius      search radius in blocks per axis
 * @param clusterSize minimum number of matching blocks close together (block mode only)
 */
public record CompassSettings(String target, int radius, int clusterSize) {

    public static final String CAVE = "cave";

    public boolean isCave() {
        return CAVE.equals(target);
    }

    /**
     * Parses {@code /cavecompass [cave|<block>] [radius] [count]}.
     *
     * @param resolveBlock maps user input to a normalised block key, or null if it isn't a valid block
     * @throws IllegalArgumentException with a player-facing message on invalid input
     */
    public static CompassSettings parse(
            String[] args,
            int defaultRadius,
            int maxRadius,
            int defaultClusterSize,
            Function<String, String> resolveBlock) {

        String target = CAVE;
        if (args.length >= 1 && !args[0].equalsIgnoreCase(CAVE)) {
            target = resolveBlock.apply(args[0]);
            if (target == null) {
                throw new IllegalArgumentException("Unknown block: " + args[0]);
            }
        }

        int radius = args.length >= 2 ? parsePositive(args[1], "radius") : defaultRadius;
        if (radius > maxRadius) {
            throw new IllegalArgumentException("Radius too large (max " + maxRadius + ").");
        }

        int clusterSize = args.length >= 3 ? parsePositive(args[2], "count") : defaultClusterSize;

        if (args.length > 3) {
            throw new IllegalArgumentException("Too many arguments.");
        }

        return new CompassSettings(target, radius, clusterSize);
    }

    private static int parsePositive(String value, String name) {
        try {
            int n = Integer.parseInt(value);
            if (n >= 1) {
                return n;
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        throw new IllegalArgumentException("Invalid " + name + ": " + value + " (must be a positive number)");
    }

    /** Human-readable description of the target, e.g. "bee nest". */
    public String targetName() {
        return isCave() ? "cave" : target.replace('_', ' ');
    }
}
