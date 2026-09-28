package io.jaypixl.artipelagocore.eggmoves.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EggMoveTeachingTest {

    @Test
    void intersectionOfEggPoolAndTeacherMoves() {
        List<String> eligible = EggMoveTeaching.eligibleMoves(
                Set.of("thunder", "playrough", "surf"),
                Set.of("thunder", "tackle"),
                Set.of());
        assertEquals(List.of("thunder"), eligible);
    }

    @Test
    void alreadyKnownMovesAreExcluded() {
        List<String> eligible = EggMoveTeaching.eligibleMoves(
                Set.of("thunder", "playrough"),
                Set.of("thunder", "playrough"),
                Set.of("thunder", "surf"));
        assertEquals(List.of("playrough"), eligible);
    }

    @Test
    void emptyWhenNoOverlapOrAllKnown() {
        assertTrue(EggMoveTeaching.eligibleMoves(Set.of("thunder"), Set.of("surf"), Set.of()).isEmpty());
        assertTrue(EggMoveTeaching.eligibleMoves(Set.of("thunder"), Set.of("thunder"), Set.of("thunder")).isEmpty());
        assertTrue(EggMoveTeaching.eligibleMoves(Set.of(), Set.of("thunder"), Set.of()).isEmpty());
    }

    @Test
    void nullInputsYieldEmpty() {
        assertTrue(EggMoveTeaching.eligibleMoves(null, Set.of("thunder"), Set.of()).isEmpty());
        assertTrue(EggMoveTeaching.eligibleMoves(Set.of("thunder"), null, Set.of()).isEmpty());
        assertEquals(List.of("thunder"), EggMoveTeaching.eligibleMoves(Set.of("thunder"), Set.of("thunder"), null));
    }

    @Test
    void eligibleComesOutSorted() {
        List<String> eligible = EggMoveTeaching.eligibleMoves(
                Set.of("surf", "thunder", "playrough"),
                Set.of("thunder", "surf", "playrough"),
                Set.of());
        assertEquals(List.of("playrough", "surf", "thunder"), eligible);
    }

    @Test
    void chooseCandidateIsDeterministic() {
        List<String> eligible = List.of("playrough", "surf", "thunder");
        assertEquals("playrough", EggMoveTeaching.chooseCandidate(eligible, 0.0));
        assertEquals("surf", EggMoveTeaching.chooseCandidate(eligible, 0.5));
        assertEquals("thunder", EggMoveTeaching.chooseCandidate(eligible, 0.999));
        assertNull(EggMoveTeaching.chooseCandidate(List.of(), 0.5));
        assertNull(EggMoveTeaching.chooseCandidate(null, 0.5));
    }

    @Test
    void procDelayStaysInsideWindow() {
        assertEquals(300_000L, EggMoveTeaching.nextProcDelayMillis(300_000L, 900_000L, 0.0));
        assertEquals(900_000L, EggMoveTeaching.nextProcDelayMillis(300_000L, 900_000L, 1.0));
        long mid = EggMoveTeaching.nextProcDelayMillis(300_000L, 900_000L, 0.5);
        assertTrue(mid >= 300_000L && mid <= 900_000L);
        assertEquals(600_000L, EggMoveTeaching.nextProcDelayMillis(600_000L, 600_000L, 0.37));
    }
}
