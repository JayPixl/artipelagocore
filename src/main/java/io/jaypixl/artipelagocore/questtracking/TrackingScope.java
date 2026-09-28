package io.jaypixl.artipelagocore.questtracking;

import java.util.Locale;

/**
 * Which progress store a quest writes to.
 *
 * <p>Solo progress lives on the actor's personal team, Team progress on the
 * effective party team (FTB native behavior), Global progress pooled on a
 * fixed server team.
 */
public enum TrackingScope {
    SOLO,
    TEAM,
    GLOBAL;

    public static TrackingScope defaultScope() {
        return TEAM;
    }

    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a quest-level wire value. Absent, blank, or unknown values fall
     * back to the default scope so old or hand-edited quest files keep native
     * behavior instead of failing.
     */
    public static TrackingScope parseQuest(String raw) {
        if (raw == null || raw.isBlank()) {
            return defaultScope();
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return defaultScope();
        }
    }
}
