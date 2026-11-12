package io.jaypixl.artipelagocore.questtasks.trade;

import java.util.Locale;

/** Which side of a player-to-player trade a Trade Item task counts. */
public enum TradeItemCounts {

    TRADE_AWAY,
    TRADE_FOR,
    EITHER;

    /** Stable id used for serialization and language keys. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static TradeItemCounts byId(String id) {
        if (id != null) {
            for (TradeItemCounts counts : values()) {
                if (counts.id().equals(id)) {
                    return counts;
                }
            }
        }
        return TRADE_AWAY;
    }
}
