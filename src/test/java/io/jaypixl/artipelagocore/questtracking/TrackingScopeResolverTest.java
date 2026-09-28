package io.jaypixl.artipelagocore.questtracking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackingScopeResolverTest {

    @Test
    void unsetQuestDefaultsToTeam() {
        assertEquals(TrackingScope.TEAM, TrackingScope.parseQuest(null));
        assertEquals(TrackingScope.TEAM, TrackingScope.parseQuest(""));
        assertEquals(TrackingScope.TEAM, TrackingScope.parseQuest("nonsense"));
    }

    @Test
    void unsetTaskDefaultsToInherit() {
        assertEquals(TaskTracking.INHERIT, TaskTracking.parseTask(null));
        assertEquals(TaskTracking.INHERIT, TaskTracking.parseTask(""));
        assertEquals(TaskTracking.INHERIT, TaskTracking.parseTask("nonsense"));
    }

    @Test
    void inheritFollowsQuest() {
        assertEquals(TrackingScope.SOLO, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.INHERIT, TrackingScope.SOLO));
        assertEquals(TrackingScope.TEAM, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.INHERIT, TrackingScope.TEAM));
        assertEquals(TrackingScope.GLOBAL, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.INHERIT, TrackingScope.GLOBAL));
    }

    @Test
    void explicitTaskOverridesQuest() {
        for (TrackingScope quest : TrackingScope.values()) {
            assertEquals(TrackingScope.SOLO, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.SOLO, quest));
            assertEquals(TrackingScope.TEAM, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.TEAM, quest));
            assertEquals(TrackingScope.GLOBAL, TrackingScopeResolver.resolveEffectiveScope(TaskTracking.GLOBAL, quest));
        }
    }

    @Test
    void nullInputsFallBackToDefaults() {
        assertEquals(TrackingScope.TEAM, TrackingScopeResolver.resolveEffectiveScope(null, null));
        assertEquals(TrackingScope.GLOBAL, TrackingScopeResolver.resolveEffectiveScope(null, TrackingScope.GLOBAL));
    }

    @Test
    void wireValuesRoundTrip() {
        for (TrackingScope scope : TrackingScope.values()) {
            assertEquals(scope, TrackingScope.parseQuest(scope.wireValue()));
        }
        for (TaskTracking tracking : TaskTracking.values()) {
            assertEquals(tracking, TaskTracking.parseTask(tracking.wireValue()));
        }
    }
}
