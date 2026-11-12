package io.jaypixl.artipelagocore.questtasks;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Optional;
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

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG = Boolean.getBoolean("artipelago.questtasks.debug");

    protected ScopedProgressTask(long id, Quest quest) {
        super(id, quest);
    }

    /**
     * Adds {@code delta} progress on the store this task resolves for the
     * submitting player. No-ops for a null player or a task whose dependencies
     * are not complete yet on the resolved store, which is the same gate the
     * built-in tasks apply. The gate runs after routing because a Solo task on
     * a partied player must check the personal store, not the party store.
     */
    protected void submitProgress(TeamData data, ServerPlayer player, long delta) {
        if (data == null || player == null || delta <= 0L) {
            return;
        }
        TeamData store = QuestTrackingRouter.resolveStore(this, data, player);
        if (store == null || !checkTaskSequence(store)) {
            return;
        }
        store.addProgress(this, delta);
    }

    /**
     * Adds {@code delta} progress on the store belonging to {@code playerId},
     * which may be offline. Used by owner credit (Merchant Transaction) and by
     * customer credit: both resolve the target by UUID instead of trusting the
     * event's team store. No-ops when no store resolves, which is the correct
     * failure mode — falling back to the executor's store would credit the
     * wrong player.
     */
    protected void submitProgressForPlayer(UUID playerId, long delta) {
        if (playerId == null || delta <= 0L) {
            return;
        }
        Optional<TeamData> resolved = QuestTrackingRouter.resolveStoreForOwner(this, playerId);
        if (DEBUG) {
            LOGGER.info("[artipelago] credit task={} player={} delta={} store={}",
                    getId(), playerId, delta, resolved.map(TeamData::getTeamId).orElse(null));
        }
        resolved.ifPresent(data -> {
            boolean sequenced = checkTaskSequence(data);
            if (DEBUG) {
                LOGGER.info("[artipelago] credit task={} sequenceGate={}", getId(), sequenced);
            }
            if (sequenced) {
                data.addProgress(this, delta);
            }
        });
    }
}
