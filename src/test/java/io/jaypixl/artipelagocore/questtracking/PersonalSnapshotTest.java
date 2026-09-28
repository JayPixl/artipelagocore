package io.jaypixl.artipelagocore.questtracking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PersonalSnapshotTest {

    private static final UUID PLAYER = UUID.fromString("21cc0000-0000-0000-0000-000000000001");
    private static final UUID PARTY = UUID.fromString("44030000-0000-0000-0000-000000000002");
    private static final UUID OTHER = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");

    @Test
    void personalRecordWhilePartiedIsPersonalSnapshot() {
        assertTrue(QuestTrackingRouter.isPersonalSnapshot(PLAYER, PLAYER, PARTY));
    }

    @Test
    void sameRecordUnpartiedIsNotPersonalSnapshot() {
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(PLAYER, PLAYER, PLAYER));
    }

    @Test
    void effectiveRecordWhilePartiedIsNotPersonalSnapshot() {
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(PARTY, PLAYER, PARTY));
    }

    @Test
    void unrelatedRecordIsNotPersonalSnapshot() {
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(OTHER, PLAYER, PARTY));
    }

    @Test
    void nullsFailClosed() {
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(null, PLAYER, PARTY));
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(PLAYER, null, PARTY));
        assertFalse(QuestTrackingRouter.isPersonalSnapshot(PLAYER, PLAYER, null));
    }
}
