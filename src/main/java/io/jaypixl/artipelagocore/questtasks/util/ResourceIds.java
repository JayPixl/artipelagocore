package io.jaypixl.artipelagocore.questtasks.util;

import java.util.regex.Pattern;

/**
 * Validation for the namespaced registry ids this mod's task filters accept.
 *
 * <p>Kept in one place so every filter field rejects the same malformed input.
 * A value that does not pass {@link #PATTERN} collapses to an empty target,
 * which every caller treats as "never match" rather than silently matching
 * everything.
 */
public final class ResourceIds {

    /** A namespaced id, optionally prefixed with a single {@code #} for tags. */
    public static final Pattern PATTERN = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+");

    private ResourceIds() {
    }

    /**
     * Trims whitespace, strips one leading {@code #} and returns the bare id,
     * or {@code ""} when the input is null, blank or malformed.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1).trim();
        }
        return PATTERN.matcher(trimmed).matches() ? trimmed : "";
    }

    /** True when {@code raw} carries an explicit tag marker. */
    public static boolean isTag(String raw) {
        return raw != null && raw.trim().startsWith("#");
    }
}
