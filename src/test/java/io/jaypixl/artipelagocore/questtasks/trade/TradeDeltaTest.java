package io.jaypixl.artipelagocore.questtasks.trade;

import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeDeltaTest {

    private static final TradeDelta.ItemAmount OAK = new TradeDelta.ItemAmount(
            "minecraft:oak_log", 64, List.of("minecraft:logs"));
    private static final TradeDelta.ItemAmount STONE = new TradeDelta.ItemAmount(
            "minecraft:stone", 5, List.of("minecraft:stone_bricks"));
    private static final TradeDelta.ItemAmount COBBLE = new TradeDelta.ItemAmount(
            "minecraft:cobblestone", 16, List.of("minecraft:stone_bricks"));

    private static TradeDelta.Record sale(TradeDelta.ItemAmount sold, long coinsReceived) {
        return new TradeDelta.Record(List.of(sold), List.of(), coinsReceived, 0L);
    }

    private static TradeDelta.Record purchase(TradeDelta.ItemAmount bought, long coinsPaid) {
        return new TradeDelta.Record(List.of(), List.of(bought), 0L, coinsPaid);
    }

    @Test
    void soldItemsCountMatchingQuantities() {
        assertEquals(64L, TradeDelta.compute(sale(OAK, 0L), TransactionCounts.SOLD_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(sale(OAK, 0L), TransactionCounts.BOUGHT_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void boughtItemsCountMatchingQuantities() {
        assertEquals(5L, TradeDelta.compute(purchase(STONE, 0L), TransactionCounts.BOUGHT_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(purchase(STONE, 0L), TransactionCounts.SOLD_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void moneyOnlyTradeNeverCounts() {
        TradeDelta.Record moneyOnly = new TradeDelta.Record(List.of(), List.of(), 500L, 0L);

        for (TransactionCounts counts : TransactionCounts.values()) {
            assertEquals(0L, TradeDelta.compute(moneyOnly, counts,
                    ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT), counts.name());
        }
    }

    @Test
    void barterContributesToBothSides() {
        TradeDelta.Record barter = new TradeDelta.Record(List.of(OAK), List.of(COBBLE), 0L, 0L);

        assertEquals(64L, TradeDelta.compute(barter, TransactionCounts.SOLD_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(16L, TradeDelta.compute(barter, TransactionCounts.BOUGHT_ITEMS,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(barter, TransactionCounts.COINS_RECEIVED,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(barter, TransactionCounts.COINS_PAID,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void coinLabelsAreInvertedBetweenCustomerAndMerchant() {
        // A sale: the shop received 300, so the customer paid 300.
        TradeDelta.Record sale = sale(OAK, 300L);
        assertEquals(300L, TradeDelta.compute(sale, TransactionCounts.COINS_RECEIVED,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(300L, TradeDelta.compute(sale, TransactionCounts.COINS_PAID,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.CUSTOMER));
        assertEquals(0L, TradeDelta.compute(sale, TransactionCounts.COINS_PAID,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));

        // A purchase: the shop paid 300, so the customer received 300.
        TradeDelta.Record purchase = purchase(STONE, 300L);
        assertEquals(300L, TradeDelta.compute(purchase, TransactionCounts.COINS_PAID,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(300L, TradeDelta.compute(purchase, TransactionCounts.COINS_RECEIVED,
                ItemFilter.any(), ItemFilter.any(), TradeDelta.Perspective.CUSTOMER));
    }

    @Test
    void coinsStillRequireFilledItemFiltersToMatch() {
        TradeDelta.Record record = sale(OAK, 300L);
        ItemFilter oak = ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:oak_log");
        ItemFilter stone = ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:stone");

        assertEquals(300L, TradeDelta.compute(record, TransactionCounts.COINS_RECEIVED,
                oak, ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(record, TransactionCounts.COINS_RECEIVED,
                stone, ItemFilter.any(), TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void bothFilledFiltersMustMatch() {
        TradeDelta.Record barter = new TradeDelta.Record(List.of(OAK), List.of(STONE), 0L, 0L);
        ItemFilter oak = ItemFilter.of(ItemFilter.Mode.TAG, "#minecraft:logs");
        ItemFilter stone = ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:stone");

        assertEquals(64L, TradeDelta.compute(barter, TransactionCounts.SOLD_ITEMS,
                oak, stone, TradeDelta.Perspective.MERCHANT));
        assertEquals(0L, TradeDelta.compute(barter, TransactionCounts.SOLD_ITEMS,
                oak, ItemFilter.of(ItemFilter.Mode.ITEM, "minecraft:dirt"),
                TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void emptyTransactionFiltersMeanAny() {
        TradeDelta.Record record = sale(OAK, 0L);
        ItemFilter blankItem = ItemFilter.of(ItemFilter.Mode.ITEM, "");
        ItemFilter blankTag = ItemFilter.of(ItemFilter.Mode.TAG, "");

        assertEquals(64L, TradeDelta.compute(record, TransactionCounts.SOLD_ITEMS,
                blankItem, blankTag, TradeDelta.Perspective.MERCHANT));
    }

    @Test
    void itemCountUsesTheSideFilterQuantities() {
        TradeDelta.Record barter = new TradeDelta.Record(List.of(OAK), List.of(COBBLE), 0L, 0L);
        ItemFilter stoneTag = ItemFilter.of(ItemFilter.Mode.TAG, "#minecraft:stone_bricks");

        assertEquals(16L, TradeDelta.compute(barter, TransactionCounts.BOUGHT_ITEMS,
                ItemFilter.any(), stoneTag, TradeDelta.Perspective.MERCHANT));
        // The bought filter is a qualifier for a sold count, not a restriction
        // on which sold stacks are summed: the trade qualifies because the shop
        // bought stone, and the sold count still sums every sold stack.
        assertEquals(64L, TradeDelta.compute(barter, TransactionCounts.SOLD_ITEMS,
                ItemFilter.any(), stoneTag, TradeDelta.Perspective.MERCHANT));
    }
}
