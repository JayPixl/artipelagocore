package io.jaypixl.artipelagocore.questtasks.trade;

import java.util.Locale;

/**
 * What a shop transaction task counts.
 *
 * <p>Sold and bought always describe the shop's side, the same convention as
 * Lightman's {@code SALE}/{@code PURCHASE}. The coin options are relative to
 * the credited party and are inverted by {@link TradeDelta.Perspective}.
 */
public enum TransactionCounts {

    SOLD_ITEMS,
    BOUGHT_ITEMS,
    COINS_RECEIVED,
    COINS_PAID;

    /** Stable id used for serialization and language keys. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TransactionCounts byId(String id) {
        if (id != null) {
            for (TransactionCounts counts : values()) {
                if (counts.id().equals(id)) {
                    return counts;
                }
            }
        }
        return SOLD_ITEMS;
    }
}
