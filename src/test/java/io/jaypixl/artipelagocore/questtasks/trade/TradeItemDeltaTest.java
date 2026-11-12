package io.jaypixl.artipelagocore.questtasks.trade;

import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeItemDeltaTest {

    private static final TradeDelta.ItemAmount OAK = new TradeDelta.ItemAmount(
            "minecraft:oak_log", 16, List.of("minecraft:logs"));
    private static final TradeDelta.ItemAmount COBBLE = new TradeDelta.ItemAmount(
            "minecraft:cobblestone", 32, List.of("minecraft:stone_bricks"));

    @Test
    void countsWhatWasTradedAway() {
        assertEquals(16L, TradeItemDelta.compute(List.of(OAK), List.of(COBBLE),
                TradeItemCounts.TRADE_AWAY, ItemFilter.any()));
    }

    @Test
    void countsWhatWasTradedFor() {
        assertEquals(32L, TradeItemDelta.compute(List.of(OAK), List.of(COBBLE),
                TradeItemCounts.TRADE_FOR, ItemFilter.any()));
    }

    @Test
    void eitherCountsBothSides() {
        assertEquals(48L, TradeItemDelta.compute(List.of(OAK), List.of(COBBLE),
                TradeItemCounts.EITHER, ItemFilter.any()));
    }

    @Test
    void tagFilterRestrictsBothSides() {
        ItemFilter logs = ItemFilter.of(ItemFilter.Mode.TAG, "#minecraft:logs");

        assertEquals(16L, TradeItemDelta.compute(List.of(OAK), List.of(COBBLE),
                TradeItemCounts.EITHER, logs));
    }

    @Test
    void blankFilterNeverMatches() {
        ItemFilter blank = ItemFilter.of(ItemFilter.Mode.ITEM, "");

        assertEquals(0L, TradeItemDelta.compute(List.of(OAK), List.of(COBBLE),
                TradeItemCounts.EITHER, blank));
    }

    @Test
    void quantitiesAreItemCountsNotStackCounts() {
        TradeDelta.ItemAmount fullStack = new TradeDelta.ItemAmount("minecraft:oak_log", 64, List.of());

        assertEquals(64L, TradeItemDelta.compute(List.of(fullStack), List.of(),
                TradeItemCounts.TRADE_AWAY, ItemFilter.any()));
    }
}
