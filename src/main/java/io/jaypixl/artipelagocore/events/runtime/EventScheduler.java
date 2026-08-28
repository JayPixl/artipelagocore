package io.jaypixl.artipelagocore.events.runtime;

import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.events.config.EventConfig;
import io.jaypixl.artipelagocore.events.config.EventConfigManager;
import io.jaypixl.artipelagocore.events.data.EventSavedData;
import io.jaypixl.artipelagocore.events.data.ScheduledEvent;
import net.minecraft.server.MinecraftServer;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Materializes configured schedules as saved events, while keeping manual events persistent too. */
public final class EventScheduler {
    private static final DateTimeFormatter ONCE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-dd-MM'@'HH:mm");
    private static boolean enabled = true;
    private EventScheduler() { }
    public static void refresh(MinecraftServer server) {
        if (!enabled) return;
        EventSavedData data = EventSavedData.get(server.overworld());
        long now = System.currentTimeMillis();
        for (int index = 0; index < EventConfigManager.get().schedules.size(); index++) {
            EventConfig.Schedule schedule = EventConfigManager.get().schedules.get(index);
            EventConfig.Preset preset = EventConfigManager.getPreset(schedule.preset);
            if (preset == null) { ArtipelagoCoreMod.LOGGER.warn("Event schedule {} references missing preset {}", index, schedule.preset); continue; }
            long[] window = getWindow(schedule, now);
            if (window == null || now < window[0] || now >= window[1]) continue;
            String id = "schedule-" + index + "-" + window[0];
            if (!data.contains(id)) data.add(new ScheduledEvent(id, preset.name, preset.description, window[0], window[1], preset.effects, schedule.preset));
        }
        data.removeExpired(now);
        SpawnEffectOverlay.refresh(server);
    }
    private static long[] getWindow(EventConfig.Schedule schedule, long now) {
        try {
            if ("once".equals(schedule.type)) return new long[] {
                    LocalDateTime.parse(schedule.start, ONCE_DATE_TIME_FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli(),
                    LocalDateTime.parse(schedule.end, ONCE_DATE_TIME_FORMAT).toInstant(ZoneOffset.UTC).toEpochMilli()
            };
            if (!"weekly".equals(schedule.type)) return null;
            DayOfWeek day = DayOfWeek.valueOf(schedule.day.toUpperCase());
            ZonedDateTime current = java.time.Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC);
            ZonedDateTime start = current.with(day).with(LocalTime.parse(schedule.start));
            if (start.isAfter(current)) start = start.minusWeeks(1);
            ZonedDateTime end = current.with(day).with(LocalTime.parse(schedule.end));
            if (!end.isAfter(start)) end = end.plusWeeks(1);
            return new long[] { start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli() };
        } catch (RuntimeException exception) { ArtipelagoCoreMod.LOGGER.error("Invalid event schedule for preset {}", schedule.preset, exception); return null; }
    }
    public static void shutdown(MinecraftServer server) { enabled = false; EventSavedData.get(server.overworld()).clear(); SpawnEffectOverlay.refresh(server); }
    public static void reload(MinecraftServer server) { enabled = true; EventConfigManager.load(); refresh(server); }
}
