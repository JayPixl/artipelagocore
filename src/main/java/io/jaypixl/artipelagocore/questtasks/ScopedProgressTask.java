package io.jaypixl.artipelagocore.questtasks;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Base for this mod's counting task types.
 *
 * <p>Progress is written here rather than straight to the store FTB hands to
 * {@code submitTask}: FTB's submit reroute only targets its own task classes,
 * so a custom task has to resolve its Tracking Scope store itself. Centralising
 * the write keeps that resolution — and the sequence gate the built-in counting
 * tasks apply — identical for every type this mod adds.
 */
public abstract class ScopedProgressTask extends Task {

    protected ScopedProgressTask(long id, Quest quest) {
        super(id, quest);
    }

    /**
     * Adds {@code delta} progress on the store this task resolves for the
     * submitting player. No-ops for a null player or a task whose dependencies
     * are not complete yet, which is the same gate the built-in tasks use.
     */
    protected void submitProgress(TeamData data, ServerPlayer player, long delta) {
        if (data == null || player == null || delta <= 0L) {
            return;
        }
        if (!checkTaskSequence(data)) {
            return;
        }
        QuestTrackingRouter.resolveStore(this, data, player).addProgress(this, delta);
    }

    /**
     * Adds {@code delta} progress on the store belonging to {@code ownerId},
     * who may be offline. Used only by owner-credited tasks (Merchant
     * Transaction): the store cannot come from the event because the event's
     * store belongs to the customer. No-ops when the owner has no resolvable
     * store, which is the correct failure mode — falling back to the executor's
     * store would credit the wrong player.
     */
    protected void submitOwnerProgress(UUID ownerId, long delta) {
        if (ownerId == null || delta <= 0L) {
            return;
        }
        QuestTrackingRouter.resolveStoreForOwner(this, ownerId).ifPresent(data -> {
            if (checkTaskSequence(data)) {
                data.addProgress(this, delta);
            }
        });
    }
}
