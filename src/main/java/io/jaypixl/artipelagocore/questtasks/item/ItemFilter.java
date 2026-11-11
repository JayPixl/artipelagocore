package io.jaypixl.artipelagocore.questtasks.item;

import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;

import java.util.Collection;
import java.util.Locale;

/**
 * What an item-based task counts: any item, exactly one item id, or one item
 * tag.
 *
 * <p>Pure by design — the caller supplies the item's registry id and the tag
 * ids that contain it, so the matching rules stay unit testable without
 * Minecraft or FTB types. {@link Mode#ANY} matches everything. A filtered mode
 * left blank is unconstrained — transaction tasks treat that as "no
 * condition" — while a non-blank but malformed target is a typo and never
 * matches, so it fails closed instead of silently qualifying every item.
 */
public final class ItemFilter {

    public enum Mode {
        ANY,
        ITEM,
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
            return ANY;
        }
    }

    private static final ItemFilter ANY = new ItemFilter(Mode.ANY, "", false);

    private final Mode mode;
    private final String target;
    private final boolean malformed;

    private ItemFilter(Mode mode, String target, boolean malformed) {
        this.mode = mode;
        this.target = target;
        this.malformed = malformed;
    }

    /** A filter that accepts every item and id. */
    public static ItemFilter any() {
        return ANY;
    }

    /**
     * Builds a filter from a configured value, stripping whitespace and one
     * leading {@code #}. Blank and {@code #}-only values are an unconstrained
     * filtered mode; a non-blank value that does not normalize is malformed and
     * never matches.
     */
    public static ItemFilter of(Mode mode, String raw) {
        Mode effective = mode == null ? Mode.ANY : mode;
        if (effective == Mode.ANY) {
            return ANY;
        }
        String trimmed = raw == null ? "" : raw.trim();
        boolean blankInput = trimmed.isEmpty() || trimmed.equals("#");
        String normalized = ResourceIds.normalize(raw);
        return new ItemFilter(effective, normalized, !blankInput && normalized.isEmpty());
    }

    public Mode mode() {
        return mode;
    }

    public String target() {
        return target;
    }

    /** True for a filtered mode with no configured target. */
    public boolean isBlank() {
        return mode != Mode.ANY && target.isEmpty();
    }

    /** True when a non-blank configured target failed id validation. */
    public boolean isMalformed() {
        return malformed;
    }

    /**
     * True when this filter imposes no constraint: an explicit "any" mode or a
     * filtered mode left blank. A malformed target is constrained and never
     * matches, so transaction tasks fail closed instead of counting everything.
     */
    public boolean isUnconstrained() {
        return mode == Mode.ANY || (isBlank() && !malformed);
    }

    /**
     * @param itemId  registry id of the item, never null
     * @param itemTags ids of the item tags containing it, possibly empty
     */
    public boolean matches(String itemId, Collection<String> itemTags) {
        if (mode == Mode.ANY) {
            return true;
        }
        if (malformed || isBlank() || itemId == null) {
            return false;
        }
        if (mode == Mode.TAG) {
            return itemTags != null && itemTags.contains(target);
        }
        return target.equals(itemId);
    }
}
