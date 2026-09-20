package de.cndrbrbr.cavecompass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerticalHintTest {

    @Test
    void reportsUpWhenTargetIsAbove() {
        String hint = VerticalHint.describe(14);
        assertTrue(hint.contains("14"));
        assertTrue(hint.contains("up"));
    }

    @Test
    void reportsDownWhenTargetIsBelow() {
        String hint = VerticalHint.describe(-9);
        assertTrue(hint.contains("9"));
        assertTrue(hint.contains("down"));
        assertFalse(hint.contains("-9"), "should show a plain magnitude, not a negative number");
    }

    @Test
    void reportsLevelWithinThreshold() {
        assertTrue(VerticalHint.describe(0).contains("level"));
        assertTrue(VerticalHint.describe(VerticalHint.LEVEL_THRESHOLD).contains("level"));
        assertTrue(VerticalHint.describe(-VerticalHint.LEVEL_THRESHOLD).contains("level"));
    }

    @Test
    void justOutsideThresholdIsNotLevel() {
        assertFalse(VerticalHint.describe(VerticalHint.LEVEL_THRESHOLD + 1).contains("level"));
        assertFalse(VerticalHint.describe(-VerticalHint.LEVEL_THRESHOLD - 1).contains("level"));
    }
}
