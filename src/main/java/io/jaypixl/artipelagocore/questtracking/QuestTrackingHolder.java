package io.jaypixl.artipelagocore.questtracking;

/**
 * Accessor for the Tracking Scope stored on a quest by the quest-tracking
 * mixin. Quests default to Team (FTB native behavior).
 */
public interface QuestTrackingHolder {

    TrackingScope artipelago$getTrackingScope();

    void artipelago$setTrackingScope(TrackingScope scope);
}
