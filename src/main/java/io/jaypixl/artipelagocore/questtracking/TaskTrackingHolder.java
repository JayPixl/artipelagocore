package io.jaypixl.artipelagocore.questtracking;

/**
 * Accessor for the task-level tracking option stored on a task by the
 * quest-tracking mixin. Tasks default to Inherit (defer to parent quest).
 */
public interface TaskTrackingHolder {

    TaskTracking artipelago$getTaskTracking();

    void artipelago$setTaskTracking(TaskTracking tracking);
}
