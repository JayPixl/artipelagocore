package io.jaypixl.artipelagocore.questtracking;

/**
 * Pure scope resolution: effective Tracking Scope from a task option and its
 * parent quest scope. No Minecraft or FTB types on this signature so it stays
 * unit-testable.
 */
public final class TrackingScopeResolver {

    private TrackingScopeResolver() {
    }

    public static TrackingScope resolveEffectiveScope(TaskTracking task, TrackingScope quest) {
        TaskTracking effectiveTask = task == null ? TaskTracking.defaultTaskTracking() : task;
        TrackingScope effectiveQuest = quest == null ? TrackingScope.defaultScope() : quest;
        if (effectiveTask == TaskTracking.INHERIT) {
            return effectiveQuest;
        }
        return TrackingScope.valueOf(effectiveTask.name());
    }
}
