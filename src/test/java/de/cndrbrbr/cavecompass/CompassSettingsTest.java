package de.cndrbrbr.cavecompass;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CompassSettingsTest {

    private static final Function<String, String> BLOCKS =
            s -> s.equalsIgnoreCase("bee_nest") ? "bee_nest" : null;

    private static CompassSettings parse(String... args) {
        return CompassSettings.parse(args, 48, 128, 3, BLOCKS);
    }

    @Test
    void noArgumentsMeansCaveWithDefaults() {
        CompassSettings s = parse();
        assertTrue(s.isCave());
        assertEquals(48, s.radius());
    }

    @Test
    void caveWithRadius() {
        CompassSettings s = parse("CAVE", "80");
        assertTrue(s.isCave());
        assertEquals(80, s.radius());
    }

    @Test
    void blockWithRadiusAndCount() {
        CompassSettings s = parse("BEE_NEST", "100", "5");
        assertEquals("bee_nest", s.target());
        assertEquals(100, s.radius());
        assertEquals(5, s.clusterSize());
        assertEquals("bee nest", s.targetName());
    }

    @Test
    void blockUsesDefaultClusterSize() {
        assertEquals(3, parse("bee_nest").clusterSize());
    }

    @Test
    void rejectsUnknownBlock() {
        assertThrows(IllegalArgumentException.class, () -> parse("bienennest"));
    }

    @Test
    void rejectsBadRadius() {
        assertThrows(IllegalArgumentException.class, () -> parse("cave", "abc"));
        assertThrows(IllegalArgumentException.class, () -> parse("cave", "0"));
        assertThrows(IllegalArgumentException.class, () -> parse("cave", "129"));
    }

    @Test
    void rejectsTooManyArguments() {
        assertThrows(IllegalArgumentException.class, () -> parse("bee_nest", "10", "2", "x"));
    }
}
