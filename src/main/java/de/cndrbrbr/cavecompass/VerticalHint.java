package de.cndrbrbr.cavecompass;

/**
 * A compass needle only ever shows a horizontal (yaw) direction — it cannot
 * indicate "up" or "down", so a cave directly below or above the player
 * would otherwise be invisible to it. This produces a short text hint for
 * the vertical component so the compass conveys full 3D guidance without
 * needing a resource pack.
 */
public final class VerticalHint {

    /** Vertical differences within this many blocks are treated as "level". */
    static final int LEVEL_THRESHOLD = 2;

    private VerticalHint() {
    }

    /**
     * @param deltaY target Y minus player Y (positive = target is above)
     */
    public static String describe(int deltaY) {
        if (deltaY > LEVEL_THRESHOLD) {
            return "§b▲ " + deltaY + " blocks up";
        }
        if (deltaY < -LEVEL_THRESHOLD) {
            return "§b▼ " + (-deltaY) + " blocks down";
        }
        return "§b● roughly your level";
    }
}
