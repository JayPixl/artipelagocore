package io.jaypixl.artipelagocore.eggmoves.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EggMovesConfigTest {

    @Test
    void defaultsMatchSpecWindow() {
        EggMovesConfig config = new EggMovesConfig();
        assertEquals(300L, config.minSeconds);
        assertEquals(900L, config.maxSeconds);
        assertEquals(1200, config.checkIntervalTicks);
        assertTrue(config.clamped().isEmpty());
    }

    @Test
    void outOfRangeValuesClampWithMessages() {
        EggMovesConfig config = new EggMovesConfig();
        config.minSeconds = 0;
        config.maxSeconds = -5;
        config.checkIntervalTicks = 1;
        assertEquals(3, config.clamped().size());
        assertEquals(1L, config.minSeconds);
        assertEquals(1L, config.maxSeconds);
        assertEquals(EggMovesConfig.MIN_CHECK_INTERVAL_TICKS, config.checkIntervalTicks);
        assertTrue(config.clamped().isEmpty());
    }

    @Test
    void maxBelowMinRisesToMin() {
        EggMovesConfig config = new EggMovesConfig();
        config.minSeconds = 600;
        config.maxSeconds = 120;
        assertEquals(1, config.clamped().size());
        assertEquals(600L, config.maxSeconds);
    }

    @Test
    void checkIntervalAboveMinWindowWarns() {
        EggMovesConfig config = new EggMovesConfig();
        config.minSeconds = 5;
        config.maxSeconds = 20;
        config.checkIntervalTicks = 1200;
        assertEquals(1, config.clamped().size());
        assertEquals(1200, config.checkIntervalTicks);
    }
}
