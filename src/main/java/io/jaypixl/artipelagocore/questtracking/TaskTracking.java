package io.jaypixl.artipelagocore.questtracking;

import java.util.Locale;

/**
 * Task-level tracking option. {@code INHERIT} defers to the parent quest's
 * scope; any explicit value overrides it.
 */
public enum TaskTracking {
    INHERIT,
    SOLO,
    TEAM,
    GLOBAL;

    public static TaskTracking defaultTaskTracking() {
        return INHERIT;
    }

    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a task-level wire value. Absent, blank, or unknown values fall
     * back to inherit so tasks pick up whatever their quest declares.
     */
    public static TaskTracking parseTask(String raw) {
        if (raw == null || raw.isBlank()) {
            return defaultTaskTracking();
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return defaultTaskTracking();
        }
    }
}
