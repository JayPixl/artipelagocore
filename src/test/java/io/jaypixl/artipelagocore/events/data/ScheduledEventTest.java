package io.jaypixl.artipelagocore.events.data;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledEventTest {

    @Test
    void activeWindowIsStartInclusiveEndExclusive() {
        ScheduledEvent event = new ScheduledEvent("id", "Name", List.of(), 1000L, 2000L, List.of(), "");
        assertFalse(event.isActive(999L));
        assertTrue(event.isActive(1000L));
        assertTrue(event.isActive(1999L));
        assertFalse(event.isActive(2000L));
    }

    @Test
    void nullCollectionsDefaultToEmpty() {
        ScheduledEvent event = new ScheduledEvent("id", "Name", null, 1000L, 2000L, null, null);
        assertEquals(List.of(), event.description());
        assertEquals(List.of(), event.effects());
        assertEquals("", event.recurringId());
    }

    @Test
    void blankIdOrNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledEvent("  ", "Name", List.of(), 1000L, 2000L, List.of(), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledEvent("id", "", List.of(), 1000L, 2000L, List.of(), ""));
    }

    @Test
    void endMustBeAfterStart() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledEvent("id", "Name", List.of(), 2000L, 2000L, List.of(), ""));
        assertThrows(IllegalArgumentException.class,
                () -> new ScheduledEvent("id", "Name", List.of(), 3000L, 2000L, List.of(), ""));
    }
}
