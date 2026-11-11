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
 * Minecraft or FTB types. {@link Mode#ANY} is the only mode that matches
 * everything; a blank or malformed {@link Mode#ITEM}/{@link Mode#TAG} target
 * never matches, so an unfinished filtered task cannot complete on any item.
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

    private static final ItemFilter ANY = new ItemFilter(Mode.ANY, "");

    private final Mode mode;
    private final String target;

    private ItemFilter(Mode mode, String target) {
        this.mode = mode;
        this.target = target;
    }

    /** A filter that accepts every item and id. */
    public static ItemFilter any() {
        return ANY;
    }

    /**
     * Builds a filter from a configured value, stripping whitespace and one
     * leading {@code #}. A malformed target becomes empty and therefore never
     * matches.
     */
    public static ItemFilter of(Mode mode, String raw) {
        Mode effective = mode == null ? Mode.ANY : mode;
        if (effective == Mode.ANY) {
            return ANY;
        }
        return new ItemFilter(effective, ResourceIds.normalize(raw));
    }

    public Mode mode() {
        return mode;
    }

    public String target() {
        return target;
    }

    /** True for a filtered mode whose configured target is blank or malformed. */
    public boolean isBlank() {
        return mode != Mode.ANY && target.isEmpty();
    }

    /**
     * True when this filter imposes no constraint: either an explicit "any"
     * mode or a filtered mode left blank. Transaction tasks use this to decide
     * whether a side is a qualifier; the fish and trade-item tasks instead
     * require a real match and use {@link #matches}.
     */
    public boolean isUnconstrained() {
        return mode == Mode.ANY || isBlank();
    }

    /**
     * @param itemId  registry id of the item, never null
     * @param itemTags ids of the item tags containing it, possibly empty
     */
    public boolean matches(String itemId, Collection<String> itemTags) {
        if (mode == Mode.ANY) {
            return true;
        }
        if (isBlank() || itemId == null) {
            return false;
        }
        if (mode == Mode.TAG) {
            return itemTags != null && itemTags.contains(target);
        }
        return target.equals(itemId);
    }
}
