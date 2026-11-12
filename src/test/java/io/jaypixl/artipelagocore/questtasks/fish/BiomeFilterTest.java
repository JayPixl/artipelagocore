package io.jaypixl.artipelagocore.questtasks.fish;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeFilterTest {

    private static final List<String> OCEAN_TAGS = List.of("minecraft:is_ocean", "c:is_ocean");

    @Test
    void disabledFilterMatchesAnyBiome() {
        BiomeFilter filter = BiomeFilter.of(false, "minecraft:ocean");

        assertFalse(filter.isEnabled());
        assertTrue(filter.matches("minecraft:plains", List.of()));
        assertTrue(filter.matches("minecraft:ocean", OCEAN_TAGS));
    }

    @Test
    void idModeMatchesExactBiomeOnly() {
        BiomeFilter filter = BiomeFilter.of(true, "minecraft:ocean");

        assertTrue(filter.matches("minecraft:ocean", List.of()));
        assertFalse(filter.matches("minecraft:deep_ocean", OCEAN_TAGS));
    }

    @Test
    void tagModeMatchesMembershipNotId() {
        BiomeFilter filter = BiomeFilter.of(true, "#minecraft:is_ocean");

        assertTrue(filter.matches("minecraft:deep_ocean", OCEAN_TAGS));
        assertFalse(filter.matches("minecraft:ocean", List.of()), "a tag filter never matches by id");
    }

    @Test
    void enabledBlankFilterNeverMatches() {
        BiomeFilter filter = BiomeFilter.of(true, "");

        assertTrue(filter.isBlank());
        assertFalse(filter.matches("minecraft:ocean", OCEAN_TAGS));
        assertFalse(BiomeFilter.of(true, "ocean").matches("minecraft:ocean", OCEAN_TAGS));
        assertFalse(BiomeFilter.of(true, "#").matches("minecraft:ocean", OCEAN_TAGS));
    }
}
