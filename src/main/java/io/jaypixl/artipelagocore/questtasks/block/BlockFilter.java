package io.jaypixl.artipelagocore.questtasks.block;

import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * What a Break Block task counts: any block, exactly one block id, or one
 * block tag.
 *
 * <p>Pure by design — the caller supplies the broken block's id and the tag
 * ids that contain it, so the matching rules stay unit testable without
 * Minecraft or FTB types. A blank target matches every block; a non-blank but
 * malformed target is a typo and never matches, so a bad id fails closed
 * instead of silently counting everything.
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
    public static final Pattern CONFIG_PATTERN = ResourceIds.PATTERN;

    private static final BlockFilter ANY = new BlockFilter(Mode.BLOCK, "", false);

    private final Mode mode;
    private final String target;
    private final boolean malformed;

    private BlockFilter(Mode mode, String target, boolean malformed) {
        this.mode = mode;
        this.target = target;
        this.malformed = malformed;
    }

    /** The filter used for a blank configuration: it matches every block. */
    public static BlockFilter none() {
        return ANY;
    }

    /**
     * Builds a filter from a configured value, stripping whitespace and one
     * leading {@code #}. A blank or {@code #}-only value means "any block"; a
     * non-blank value that does not normalize is malformed and never matches.
     */
    public static BlockFilter of(Mode mode, String raw) {
        Mode effective = mode == null ? Mode.BLOCK : mode;
        String trimmed = raw == null ? "" : raw.trim();
        boolean blankInput = trimmed.isEmpty() || trimmed.equals("#");
        String normalized = ResourceIds.normalize(raw);
        return new BlockFilter(effective, normalized, !blankInput && normalized.isEmpty());
    }

    public Mode mode() {
        return mode;
    }

    public String target() {
        return target;
    }

    /** True when no target is configured (blank or malformed). */
    public boolean isEmpty() {
        return target.isEmpty();
    }

    /** True when the configured target is non-blank but failed id validation. */
    public boolean isMalformed() {
        return malformed;
    }

    /**
     * @param blockId   registry id of the broken block, never null
     * @param blockTags ids of the block tags containing it, possibly empty
     */
    public boolean matches(String blockId, Collection<String> blockTags) {
        if (malformed) {
            return false;
        }
        if (target.isEmpty()) {
            return true;
        }
        if (blockId == null) {
            return false;
        }
        if (mode == Mode.TAG) {
            return blockTags != null && blockTags.contains(target);
        }
        return target.equals(blockId);
    }
}
