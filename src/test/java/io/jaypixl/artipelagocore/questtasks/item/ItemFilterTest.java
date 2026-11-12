package io.jaypixl.artipelagocore.questtasks.item;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemFilterTest {

    private static final List<String> COD_TAGS = List.of("minecraft:fishes", "minecraft:cod");

    @Test
    void anyModeMatchesEveryItem() {
        ItemFilter filter = ItemFilter.any();

        assertTrue(filter.isUnconstrained());
        assertTrue(filter.matches("minecraft:cod", List.of()));
        assertTrue(filter.matches("minecraft:stone", COD_TAGS));
        assertEquals(ItemFilter.Mode.ANY, ItemFilter.of(ItemFilter.Mode.ANY, "").mode());
    }

    @Test
    void itemModeMatchesExactIdOnly() {
        ItemFilter filter = ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:cod");

        assertFalse(filter.isBlank());
        assertTrue(filter.matches("minecraft:cod", List.of()));
        assertFalse(filter.matches("minecraft:salmon", COD_TAGS), "being in the same tag must not match an id filter");
    }

    @Test
    void tagModeMatchesMembershipNotId() {
        ItemFilter filter = ItemFilter.of(ItemFilter.Mode.TAG, "#minecraft:fishes");

        assertTrue(filter.matches("minecraft:salmon", COD_TAGS));
        assertFalse(filter.matches("minecraft:stone", List.of("minecraft:stone_bricks")));
        assertEquals("minecraft:fishes", filter.target());
    }

    @Test
    void blankOrMalformedTargetNeverMatches() {
        for (String raw : new String[]{"", "   ", "#", "cod", "minecraft:", ":cod", "minecraft:co d"}) {
            ItemFilter filter = ItemFilter.of(ItemFilter.Mode.ITEM, raw);
            assertTrue(filter.isBlank(), raw);
            assertFalse(filter.matches("minecraft:cod", COD_TAGS), raw);
        }
    }

    @Test
    void blankFilteredModeIsUnconstrainedForTransactions() {
        assertTrue(ItemFilter.of(ItemFilter.Mode.ITEM, "").isUnconstrained());
        assertTrue(ItemFilter.of(ItemFilter.Mode.TAG, "").isUnconstrained());
        assertFalse(ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:cod").isUnconstrained());
        assertFalse(ItemFilter.of(ItemFilter.Mode.TAG, "#minecraft:fishes").isUnconstrained());
    }

    @Test
    void malformedFilteredModeIsConstrainedAndNeverMatches() {
        ItemFilter malformed = ItemFilter.of(ItemFilter.Mode.ITEM, "cod");

        assertTrue(malformed.isMalformed());
        assertFalse(malformed.isUnconstrained(), "a typo must not behave like an empty filter");
        assertFalse(malformed.matches("minecraft:cod", COD_TAGS));
    }

    @Test
    void whitespaceAndHashPrefixAreNormalized() {
        assertEquals("minecraft:cod", ItemFilter.of(ItemFilter.Mode.ITEM, "  minecraft:cod  ").target());
        assertEquals("minecraft:fishes", ItemFilter.of(ItemFilter.Mode.TAG, "  #minecraft:fishes ").target());
    }

    @Test
    void modeIdsAreStableForLanguageKeys() {
        assertEquals("any", ItemFilter.Mode.ANY.id());
        assertEquals("item", ItemFilter.Mode.ITEM.id());
        assertEquals("tag", ItemFilter.Mode.TAG.id());
        assertEquals(ItemFilter.Mode.ANY, ItemFilter.Mode.byId("any"));
        assertEquals(ItemFilter.Mode.ITEM, ItemFilter.Mode.byId("item"));
        assertEquals(ItemFilter.Mode.TAG, ItemFilter.Mode.byId("tag"));
        assertEquals(ItemFilter.Mode.ANY, ItemFilter.Mode.byId("nonsense"));
    }
}
