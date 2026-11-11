package io.jaypixl.artipelagocore.questtasks.block;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * What a Break Block task counts: exactly one block id or one block tag.
 *
 * <p>Pure by design — the caller supplies the broken block's id and the tag ids
 * that contain it, so the matching rules stay unit testable without Minecraft
 * or FTB types. A filter is either valid or empty, and an empty filter never
 * matches: an unfinished task must not complete on any block break.
 */
public final class BlockFilter {

    public enum Mode {
        BLOCK,
        TAG;

        /** Stable id used for serialization and language keys. */
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Mode byId(String id) {
            if (id != null) {
                for (Mode mode : values()) {
                    if (mode.id().equals(id)) {
                        return mode;
                    }
                }
            }
            return BLOCK;
        }
    }

    /**
     * A configured block id or tag id, with an optional leading {@code #}.
     * Shared with the task's config field validation so the editor and the
     * matcher can never drift apart.
     */
    public static final Pattern CONFIG_PATTERN = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+");

    private static final BlockFilter NONE = new BlockFilter(Mode.BLOCK, "");

    private final Mode mode;
    private final String target;

    private BlockFilter(Mode mode, String target) {
        this.mode = mode;
        this.target = target;
    }

    /** The never-matching filter used for blank, malformed or absent configuration. */
    public static BlockFilter none() {
        return NONE;
    }

    /**
     * Builds a filter from a configured value, stripping whitespace and one
     * leading {@code #}. Anything that is not a namespaced id collapses to
     * {@link #none()} rather than to a filter that silently never matches
     * through a typo.
     */
    public static BlockFilter of(Mode mode, String raw) {
        return new BlockFilter(mode == null ? Mode.BLOCK : mode, normalize(raw));
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1).trim();
        }
        return CONFIG_PATTERN.matcher(trimmed).matches() ? trimmed : "";
    }

    public Mode mode() {
        return mode;
    }

    public String target() {
        return target;
    }

    public boolean isEmpty() {
        return target.isEmpty();
    }

    /**
     * @param blockId   registry id of the broken block, never null
     * @param blockTags ids of the block tags containing it, possibly empty
     */
    public boolean matches(String blockId, Collection<String> blockTags) {
        if (isEmpty() || blockId == null) {
            return false;
        }
        if (mode == Mode.TAG) {
            return blockTags != null && blockTags.contains(target);
        }
        return target.equals(blockId);
    }
}
