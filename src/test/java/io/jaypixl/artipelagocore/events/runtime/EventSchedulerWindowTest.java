package io.jaypixl.artipelagocore.events.runtime;

import io.jaypixl.artipelagocore.events.config.EventConfig;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventSchedulerWindowTest {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("uuuu-dd-MM'@'HH:mm");

    @Test
    void onceWindowParsesUtcTimestamps() {
        EventConfig.Schedule schedule = new EventConfig.Schedule();
        schedule.type = "once";
        schedule.start = "2026-26-08@15:00";
        schedule.end = "2026-26-08@17:00";
        schedule.preset = "weekend";

        long now = LocalDateTime.parse("2026-26-08@16:00", FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli();
        long[] window = EventScheduler.window(schedule, now);

        assertNotNull(window);
        assertEquals(LocalDateTime.parse("2026-26-08@15:00", FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli(), window[0]);
        assertEquals(LocalDateTime.parse("2026-26-08@17:00", FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli(), window[1]);
    }

    @Test
    void unknownTypeReturnsNull() {
        EventConfig.Schedule schedule = new EventConfig.Schedule();
        schedule.type = "daily";
        schedule.preset = "weekend";

        assertNull(EventScheduler.window(schedule, System.currentTimeMillis()));
    }

    @Test
    void invalidOnceDateReturnsNull() {
        EventConfig.Schedule schedule = new EventConfig.Schedule();
        schedule.type = "once";
        schedule.start = "not-a-date";
        schedule.end = "also-bad";
        schedule.preset = "weekend";

        assertNull(EventScheduler.window(schedule, System.currentTimeMillis()));
    }

    @Test
    void weeklyWindowContainsNow() {
        EventConfig.Schedule schedule = new EventConfig.Schedule();
        schedule.type = "weekly";
        schedule.day = "monday";
        schedule.start = "00:00";
        schedule.end = "23:59";
        schedule.preset = "weekend";

        // A Monday in UTC.
        long mondayNoon = LocalDateTime.parse("2026-24-08@12:00", FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli();
        long[] window = EventScheduler.window(schedule, mondayNoon);

        assertNotNull(window);
        assertTrue(mondayNoon >= window[0] && mondayNoon < window[1]);
    }
}
