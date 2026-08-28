package io.jaypixl.artipelagocore.events.config;

import io.jaypixl.artipelagocore.events.effect.EventEffect;

import java.util.List;

/** Schema for {@code config/artipelago/events.json}. All date and time values are UTC. */
public final class EventConfig {
    public Settings settings = new Settings();
    public List<Preset> presets = List.of();
    public List<Schedule> schedules = List.of();

    public static final class Settings { }
    public static final class Preset {
        public String id = "";
        public String name = "";
        public List<String> description = List.of();
        public List<EventEffect> effects = List.of();
    }
    /** A schedule with type {@code weekly} uses day and HH:mm start/end; {@code once} uses UTC {@code YYYY-DD-MM@HH:MM}. */
    public static final class Schedule {
        public String type = "";
        public String day;
        public String start = "";
        public String end = "";
        public String preset = "";
    }
}
