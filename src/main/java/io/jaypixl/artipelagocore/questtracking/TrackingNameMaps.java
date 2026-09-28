package io.jaypixl.artipelagocore.questtracking;

import dev.ftb.mods.ftblibrary.config.NameMap;
import net.minecraft.network.chat.Component;

/**
 * FTB editor dropdown models for the tracking options. Ids are the lowercase
 * wire values from the spec; display names are capitalized literals so the
 * editor shows Solo | Team | Global regardless of lang lookups.
 */
public final class TrackingNameMaps {

    public static final NameMap<TrackingScope> SCOPE =
            NameMap.of(TrackingScope.TEAM, TrackingScope.values())
                    .id(TrackingScope::wireValue)
                    .name(scope -> Component.literal(switch (scope) {
                        case SOLO -> "Solo";
                        case TEAM -> "Team";
                        case GLOBAL -> "Global";
                    }))
                    .create();

    public static final NameMap<TaskTracking> TASK =
            NameMap.of(TaskTracking.INHERIT, TaskTracking.values())
                    .id(TaskTracking::wireValue)
                    .name(tracking -> Component.literal(switch (tracking) {
                        case INHERIT -> "Inherit";
                        case SOLO -> "Solo";
                        case TEAM -> "Team";
                        case GLOBAL -> "Global";
                    }))
                    .create();

    private TrackingNameMaps() {
    }
}
