package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.jaypixl.artipelagocore.questtasks.QuestTaskTypes;

import java.util.UUID;

/**
 * Credits the player owner of the shop a customer just used.
 *
 * <p>Sold and bought follow the shop's side, coins are counted from the
 * merchant's point of view, and credit is written to the owner's Tracking
 * Scope store rather than the customer's. That store can be resolved while
 * the owner is offline, so progress accumulates across all of the owner's
 * traders and is visible the next time they log in. A trade the owner makes
 * with their own shop never counts.
 */
public class MerchantTransactionTask extends AbstractTransactionTask {

    public MerchantTransactionTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return QuestTaskTypes.MERCHANT_TRANSACTION;
    }

    /** Adds owner credit on the owner's store, creating it if they are offline. */
    public void submitOwnerTrade(UUID ownerId, long delta) {
        submitProgressForPlayer(ownerId, delta);
    }
}
