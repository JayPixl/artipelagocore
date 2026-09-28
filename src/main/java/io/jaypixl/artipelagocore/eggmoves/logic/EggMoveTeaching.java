package io.jaypixl.artipelagocore.eggmoves.logic;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure Pasture Teaching math: no Minecraft or Cobblemon types on any signature.
 */
public final class EggMoveTeaching {
    private EggMoveTeaching() { }

    /**
     * Moves teachable right now: in the Student egg pool, known by a Teacher's
     * selected moves, and not already known by the Student. Sorted for stable picks.
     */
    public static List<String> eligibleMoves(Set<String> eggPool, Set<String> teacherMoves, Set<String> knownMoves) {
        if (eggPool == null || teacherMoves == null) return List.of();
        Set<String> known = knownMoves == null ? Set.of() : knownMoves;
        TreeSet<String> eligible = new TreeSet<>();
        for (String move : teacherMoves) {
            if (move != null && eggPool.contains(move) && !known.contains(move)) eligible.add(move);
        }
        return List.copyOf(eligible);
    }

    /** Uniform pick from a sorted eligible list. Returns null when empty. */
    public static String chooseCandidate(List<String> sortedEligible, double random01) {
        if (sortedEligible == null || sortedEligible.isEmpty()) return null;
        double clamped = Math.min(Math.max(random01, 0.0), 0.999999999);
        return sortedEligible.get((int) (clamped * sortedEligible.size()));
    }

    /** Randomized proc delay inside [minMillis, maxMillis]. */
    public static long nextProcDelayMillis(long minMillis, long maxMillis, double random01) {
        long min = Math.max(0L, minMillis);
        long max = Math.max(min, maxMillis);
        double clamped = Math.min(Math.max(random01, 0.0), 1.0);
        return min + (long) (clamped * (max - min));
    }
}
