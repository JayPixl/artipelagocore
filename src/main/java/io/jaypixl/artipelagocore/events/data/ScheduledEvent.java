package io.jaypixl.artipelagocore.events.data;

import io.jaypixl.artipelagocore.events.effect.EventEffect;

import java.util.List;

public record ScheduledEvent(String id, String name, String description, long startTime, long endTime,
                             List<EventEffect> effects, String recurringId) {

    public ScheduledEvent {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event id and name cannot be blank");
        }
        if (endTime <= startTime) {
            throw new IllegalArgumentException("Event end time must be after its start time");
        }
        description = description == null ? "" : description;
        effects = effects == null ? List.of() : List.copyOf(effects);
        recurringId = recurringId == null ? "" : recurringId;
    }

    public boolean isActive(long currentTime) {
        return currentTime >= startTime && currentTime < endTime;
    }
}
