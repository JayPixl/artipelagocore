package io.jaypixl.artipelagocore.questtasks.block;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockFilterTest {

    private static final List<String> OAK_LOG_TAGS = List.of("minecraft:logs", "minecraft:oak_logs");

    @Test
    void blockModeMatchesExactIdOnly() {
        BlockFilter filter = BlockFilter.of(BlockFilter.Mode.BLOCK, "minecraft:oak_log");

        assertFalse(filter.isEmpty());
        assertTrue(filter.matches("minecraft:oak_log", List.of()));
        assertFalse(filter.matches("minecraft:oak_planks", List.of()));
    }

    @Test
    void blockModeIgnoresTags() {
        BlockFilter filter = BlockFilter.of(BlockFilter.Mode.BLOCK, "minecraft:oak_log");

        assertTrue(filter.matches("minecraft:oak_log", OAK_LOG_TAGS), "the exact id still matches");
        assertFalse(filter.matches("minecraft:birch_log", OAK_LOG_TAGS),
                "being in the same tag must not make an id filter match");
    }

    @Test
    void tagModeMatchesMembershipNotId() {
        BlockFilter filter = BlockFilter.of(BlockFilter.Mode.TAG, "#minecraft:logs");

        assertTrue(filter.matches("minecraft:stripped_oak_log", OAK_LOG_TAGS));
        assertEquals("minecraft:logs", filter.target());
        assertFalse(filter.matches("minecraft:logs", List.of()), "a tag filter never matches by id");
        assertFalse(filter.matches("minecraft:stone", List.of("minecraft:stone_bricks")));
    }

    @Test
    void tagModeAcceptsBareTagId() {
        BlockFilter filter = BlockFilter.of(BlockFilter.Mode.TAG, "minecraft:logs");

        assertTrue(filter.matches("minecraft:oak_log", OAK_LOG_TAGS));
    }

    @Test
    void whitespaceAndHashPrefixAreNormalized() {
        assertEquals("minecraft:oak_log", BlockFilter.of(BlockFilter.Mode.BLOCK, "  minecraft:oak_log  ").target());
        assertEquals("minecraft:logs", BlockFilter.of(BlockFilter.Mode.TAG, "  #minecraft:logs ").target());
    }

    @Test
    void blankValuesBecomeAnEmptyFilter() {
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, "").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, "   ").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.TAG, "#").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, null).isEmpty());
        assertTrue(BlockFilter.none().isEmpty());
    }

    @Test
    void malformedValuesBecomeAnEmptyFilter() {
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, "oak_log").isEmpty(), "a namespace is required");
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, "minecraft:").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, ":oak_log").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.BLOCK, "minecraft:oak log").isEmpty());
        assertTrue(BlockFilter.of(BlockFilter.Mode.TAG, "minecraft:logs#").isEmpty());
    }

    @Test
    void emptyFilterNeverMatches() {
        assertFalse(BlockFilter.none().matches("minecraft:oak_log", OAK_LOG_TAGS));
        assertFalse(BlockFilter.none().matches("", List.of("")));
        assertFalse(BlockFilter.of(BlockFilter.Mode.BLOCK, "").matches("minecraft:oak_log", OAK_LOG_TAGS));
    }

    @Test
    void nullModeFallsBackToBlockMode() {
        BlockFilter filter = BlockFilter.of(null, "minecraft:oak_log");

        assertEquals(BlockFilter.Mode.BLOCK, filter.mode());
        assertTrue(filter.matches("minecraft:oak_log", List.of()));
    }

    @Test
    void modeIdsAreStableForLanguageKeys() {
        assertEquals("block", BlockFilter.Mode.BLOCK.id());
        assertEquals("tag", BlockFilter.Mode.TAG.id());
        assertEquals(BlockFilter.Mode.BLOCK, BlockFilter.Mode.byId("block"));
        assertEquals(BlockFilter.Mode.TAG, BlockFilter.Mode.byId("tag"));
        assertEquals(BlockFilter.Mode.BLOCK, BlockFilter.Mode.byId("nonsense"));
    }
}
