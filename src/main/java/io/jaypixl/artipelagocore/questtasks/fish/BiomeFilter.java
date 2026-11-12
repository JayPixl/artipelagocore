package io.jaypixl.artipelagocore.questtasks.fish;

import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;

import java.util.Collection;

/**
 * The optional biome restriction on a Fish Up Item task.
 *
 * <p>Pure by design — the caller supplies the bobber biome's registry id and
 * the biome tag ids that contain it. The toggle is part of the filter so
 * "disabled" matches every biome and cannot be confused with a blank target,
 * which fails closed instead.
 */
public final class BiomeFilter {

    private static final BiomeFilter DISABLED = new BiomeFilter(false, false, "");

    private final boolean enabled;
    private final boolean tag;
    private final String target;

    private BiomeFilter(boolean enabled, boolean tag, String target) {
        this.enabled = enabled;
        this.tag = tag;
        this.target = target;
    }

    public static BiomeFilter disabled() {
        return DISABLED;
    }

    /**
     * Builds a filter from the task's toggle and configured biome-or-tag
     * value. The value may carry a leading {@code #} to force tag matching;
     * otherwise an id is matched as an id.
     */
    public static BiomeFilter of(boolean enabled, String raw) {
        if (!enabled) {
            return DISABLED;
        }
        boolean tag = ResourceIds.isTag(raw);
        return new BiomeFilter(true, tag, ResourceIds.normalize(raw));
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** True for an enabled filter whose configured biome is blank or malformed. */
    public boolean isBlank() {
        return enabled && target.isEmpty();
    }

    /**
     * @param biomeId   registry id of the bobber's biome, never null
     * @param biomeTags ids of the biome tags containing it, possibly empty
     */
    public boolean matches(String biomeId, Collection<String> biomeTags) {
        if (!enabled) {
            return true;
        }
        if (isBlank() || biomeId == null) {
            return false;
        }
        if (tag) {
            return biomeTags != null && biomeTags.contains(target);
        }
        return target.equals(biomeId);
    }
}
