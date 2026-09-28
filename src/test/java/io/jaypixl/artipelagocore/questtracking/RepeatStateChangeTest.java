package io.jaypixl.artipelagocore.questtracking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepeatStateChangeTest {

    @Test
    void unchangedStateDoesNotRepeat() {
        assertFalse(QuestTrackingRouter.repeatStateChanged(0L, 0, 0L, 0));
        assertFalse(QuestTrackingRouter.repeatStateChanged(123L, 2, 123L, 2));
    }

    @Test
    void cooldownWriteCountsAsRepeat() {
        assertTrue(QuestTrackingRouter.repeatStateChanged(0L, 1, 999L, 1));
    }

    @Test
    void countBumpCountsAsRepeat() {
        assertTrue(QuestTrackingRouter.repeatStateChanged(0L, 1, 0L, 2));
    }

    @Test
    void bothMovingCountsAsRepeat() {
        assertTrue(QuestTrackingRouter.repeatStateChanged(0L, 0, 999L, 1));
    }
}
