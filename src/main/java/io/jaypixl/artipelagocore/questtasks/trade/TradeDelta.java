package io.jaypixl.artipelagocore.questtasks.trade;

import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;

import java.util.Collection;
import java.util.List;

/**
 * Pure trade-delta computation shared by the Customer and Merchant
 * Transaction task types.
 *
 * <p>One trader trade is normalized once into what left the shop, what entered
 * it, and the coins the shop received and paid; this class then answers how
 * much one task should count, given its {@link TransactionCounts} mode and its
 * two optional item filters. A trade that moved no items is rejected before
 * counting, which is the single rule that makes money-only trades never count.
 */
public final class TradeDelta {

    /** Which party's point of view the coin labels use. */
    public enum Perspective {
        CUSTOMER,
        MERCHANT
    }

    /**
     * One moved stack, reduced to what filtering needs. {@code tagIds} may be
     * empty when the caller only cares about id filters.
     */
    public record ItemAmount(String itemId, int count, Collection<String> tagIds) {

        public ItemAmount {
            itemId = itemId == null ? "" : itemId;
            tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
        }
    }

    /**
     * A normalized trade, always from the shop's perspective: {@code sold} is
     * what left the shop, {@code bought} is what entered it, and the two coin
     * values are what the shop received and paid (gross, taxes ignored).
     */
    public record Trade(List<ItemAmount> sold, List<ItemAmount> bought,
            long coinsReceivedByShop, long coinsPaidByShop) {

        public Trade {
            sold = sold == null ? List.of() : List.copyOf(sold);
            bought = bought == null ? List.of() : List.copyOf(bought);
            coinsReceivedByShop = Math.max(0L, coinsReceivedByShop);
            coinsPaidByShop = Math.max(0L, coinsPaidByShop);
        }

        public boolean hasItems() {
            return !sold.isEmpty() || !bought.isEmpty();
        }
    }

    private TradeDelta() {
    }

    /**
     * Amount of progress one transaction adds to a task. Zero means the task
     * does not count this trade at all.
     */
    public static long compute(Trade trade, TransactionCounts counts,
            ItemFilter soldFilter, ItemFilter boughtFilter, Perspective perspective) {
        if (trade == null || counts == null || !trade.hasItems()) {
            return 0L;
        }
        // Both filters are conjoined qualifiers: a filled side must contain a
        // matching stack, even when the counted quantity is coins.
        if (!qualifies(trade.sold(), soldFilter) || !qualifies(trade.bought(), boughtFilter)) {
            return 0L;
        }
        Perspective effective = perspective == null ? Perspective.MERCHANT : perspective;
        long amount = switch (counts) {
            case SOLD_ITEMS -> sumMatching(trade.sold(), soldFilter);
            case BOUGHT_ITEMS -> sumMatching(trade.bought(), boughtFilter);
            case COINS_RECEIVED -> receivedBy(effective, trade);
            case COINS_PAID -> paidBy(effective, trade);
        };
        return Math.max(0L, amount);
    }

    private static boolean qualifies(List<ItemAmount> side, ItemFilter filter) {
        if (filter == null || filter.isUnconstrained()) {
            return true;
        }
        for (ItemAmount item : side) {
            if (filter.matches(item.itemId(), item.tagIds())) {
                return true;
            }
        }
        return false;
    }

    private static long sumMatching(List<ItemAmount> side, ItemFilter filter) {
        long total = 0L;
        for (ItemAmount item : side) {
            if (filter == null || filter.isUnconstrained()
                    || filter.matches(item.itemId(), item.tagIds())) {
                total += item.count();
            }
        }
        return total;
    }

    private static long receivedBy(Perspective perspective, Trade trade) {
        return perspective == Perspective.MERCHANT
                ? trade.coinsReceivedByShop()
                : trade.coinsPaidByShop();
    }

    private static long paidBy(Perspective perspective, Trade trade) {
        return perspective == Perspective.MERCHANT
                ? trade.coinsPaidByShop()
                : trade.coinsReceivedByShop();
    }
}
