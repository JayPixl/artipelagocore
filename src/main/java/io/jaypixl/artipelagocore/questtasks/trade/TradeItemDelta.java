package io.jaypixl.artipelagocore.questtasks.trade;

import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;

import java.util.List;

/**
 * Pure counted-quantity logic for the Trade Item task.
 *
 * <p>Both sides are already expressed from the credited player's perspective:
 * {@code tradedAway} is what that player put into the trade, {@code tradedFor}
 * is what they received. A blank item filter matches nothing, while an
 * explicit "any" filter matches every stack.
 */
public final class TradeItemDelta {

    private TradeItemDelta() {
    }

    public static long compute(List<TradeDelta.ItemAmount> tradedAway,
            List<TradeDelta.ItemAmount> tradedFor, TradeItemCounts counts, ItemFilter filter) {
        if (counts == null || filter == null) {
            return 0L;
        }
        long amount = switch (counts) {
            case TRADE_AWAY -> sum(tradedAway, filter);
            case TRADE_FOR -> sum(tradedFor, filter);
            case EITHER -> sum(tradedAway, filter) + sum(tradedFor, filter);
        };
        return Math.max(0L, amount);
    }

    private static long sum(List<TradeDelta.ItemAmount> stacks, ItemFilter filter) {
        if (stacks == null) {
            return 0L;
        }
        long total = 0L;
        for (TradeDelta.ItemAmount stack : stacks) {
            if (filter.matches(stack.itemId(), stack.tagIds())) {
                total += stack.count();
            }
        }
        return total;
    }
}
