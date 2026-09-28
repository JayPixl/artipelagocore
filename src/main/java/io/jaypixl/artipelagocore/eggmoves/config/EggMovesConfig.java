package io.jaypixl.artipelagocore.eggmoves.config;

import java.util.ArrayList;
import java.util.List;

public final class EggMovesConfig {
    public static final long DEFAULT_MIN_SECONDS = 300L;
    public static final long DEFAULT_MAX_SECONDS = 900L;
    public static final int DEFAULT_CHECK_INTERVAL_TICKS = 1200;
    public static final int MIN_CHECK_INTERVAL_TICKS = 20;

    public long minSeconds = DEFAULT_MIN_SECONDS;
    public long maxSeconds = DEFAULT_MAX_SECONDS;
    public int checkIntervalTicks = DEFAULT_CHECK_INTERVAL_TICKS;

    /** Clamps out-of-range values in place, returning one message per correction. */
    public List<String> clamped() {
        List<String> corrections = new ArrayList<>();
        if (minSeconds < 1) {
            corrections.add("minSeconds " + minSeconds + " < 1, clamped to 1");
            minSeconds = 1;
        }
        if (maxSeconds < minSeconds) {
            corrections.add("maxSeconds " + maxSeconds + " < minSeconds " + minSeconds + ", clamped to minSeconds");
            maxSeconds = minSeconds;
        }
        if (checkIntervalTicks < MIN_CHECK_INTERVAL_TICKS) {
            corrections.add("checkIntervalTicks " + checkIntervalTicks + " < " + MIN_CHECK_INTERVAL_TICKS + ", clamped to " + MIN_CHECK_INTERVAL_TICKS);
            checkIntervalTicks = MIN_CHECK_INTERVAL_TICKS;
        }
        if (checkIntervalTicks / 20.0 > minSeconds) {
            corrections.add("checkIntervalTicks " + checkIntervalTicks + " (~" + checkIntervalTicks / 20
                    + "s) exceeds minSeconds " + minSeconds + "; windows shorter than the check interval cannot be honored");
        }
        return List.copyOf(corrections);
    }
}
