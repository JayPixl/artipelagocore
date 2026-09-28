package io.jaypixl.artipelagocore.questtracking;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import dev.ftb.mods.ftbquests.net.SyncTeamDataMessage;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import io.jaypixl.artipelagocore.questtracking.mixin.TeamDataProgressAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Decides which progress store a task write lands on. Reads stay native;
 * only submit flows are rerouted, so Team keeps FTB behavior exactly.
 *
 * <p>Kill attribution needs a handoff: {@code KillTask.kill} carries no
 * player, so the kill listener stashes the killer via {@link #pushSubmitter}
 * immediately before the kill loop runs, synchronously on the server thread.
 * The value is overwrite-on-set and only ever consumed by that same loop, so
 * a stale entry can never be misattributed.
 */
public final class QuestTrackingRouter {

    /**
     * Fixed id of the FTB server team backing Global progress. Created lazily
     * on first Global submit; stable across restarts so progress persists.
     */
    public static final UUID GLOBAL_TEAM_ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");

    /**
     * Re-entrancy guard for the reward mirror below. The mirror itself writes
     * the pooled store, and every {@code resetReward} call re-enters through
     * the {@code GlobalFanoutMixin} TAIL handler — without this the pooled
     * write recurses until the server thread overflows (member writes return
     * early on the global check, but the pooled self-write never does).
     * Same {@code ThreadLocal} pattern as the kill-attribution submitter.
     */
    private static final ThreadLocal<Boolean> REWARD_MIRROR_ACTIVE = ThreadLocal.withInitial(() -> false);

    /**
     * Pre-claim snapshot for the Global repeatable ordering fix. The repeat
     * cascade (reset, count, cooldown) all runs inside
     * {@code TeamData.markRewardAsClaimed}, so the before/after comparison in
     * {@link #propagateGlobalRepeatStateAfter} is the only reliable
     * repeat detector — and it guarantees the propagated values are the
     * post-claim ones. Same single-server-thread pattern as the guards above.
     */
    private static final ThreadLocal<long[]> REPEAT_BEFORE = new ThreadLocal<>();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<ServerPlayer> SUBMITTER = new ThreadLocal<>();

    private QuestTrackingRouter() {
    }

    public static TrackingScope effectiveScope(Task task) {
        TaskTracking taskOption = task instanceof TaskTrackingHolder holder
                ? holder.artipelago$getTaskTracking()
                : TaskTracking.INHERIT;
        // Quest is final and gains the holder interface only via mixin, so match through Object.
        Object quest = task.getQuest();
        TrackingScope questScope = quest instanceof QuestTrackingHolder holder
                ? holder.artipelago$getTrackingScope()
                : TrackingScope.TEAM;
        return TrackingScopeResolver.resolveEffectiveScope(taskOption, questScope);
    }

    public static TeamData resolveStore(Task task, TeamData current, ServerPlayer player) {
        return switch (effectiveScope(task)) {
            case TEAM -> current;
            case SOLO -> personalStore(player).orElse(current);
            case GLOBAL -> globalStore(player).orElse(current);
        };
    }

    public static void pushSubmitter(ServerPlayer player) {
        SUBMITTER.set(player);
    }

    public static ServerPlayer currentSubmitter() {
        return SUBMITTER.get();
    }

    public static boolean isGlobalStore(TeamData data) {
        return data != null && GLOBAL_TEAM_ID.equals(data.getTeamId());
    }

    /**
     * Mirrors a Global task's pooled progress from the global store onto every
     * member team. Each member write flows through that team's own
     * {@code setProgress}, so progress bars sync and the completion cascade
     * (quest flags, per-team toasts via each team's online members) runs
     * through the native path. The global store itself has no online members,
     * so without this nobody would see progress or toasts. No-ops for
     * non-Global tasks and client-side files. Cannot recurse: member writes
     * fail the {@link #isGlobalStore} check in the calling mixin.
     */
    public static void mirrorGlobalTask(Task task, TeamData globalStore) {
        if (!isGlobalStore(globalStore) || effectiveScope(task) != TrackingScope.GLOBAL) {
            return;
        }
        BaseQuestFile file = globalStore.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        long progress = globalStore.getProgress(task);
        boolean completed = globalStore.isCompleted(task);
        for (TeamData team : file.getAllTeamData()) {
            if (isGlobalStore(team)) {
                continue;
            }
            if (team.getProgress(task) != progress) {
                team.setProgress(task, progress);
            }
            if (completed && !team.isCompleted(task)) {
                team.markTaskCompleted(task);
            }
        }
    }

    /**
     * Catches a single (usually brand-new) team up with all pooled Global
     * progress. Live teams track the global store through
     * {@link #mirrorGlobalTask}, but a team created after progress happened
     * missed those writes and native party merges only carry what the
     * creator's personal store holds. No-op when no global store exists yet.
     */
    public static void catchUpTeam(TeamData team) {
        if (team == null || isGlobalStore(team)) {
            return;
        }
        BaseQuestFile file = team.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        TeamData global = file.getNullableTeamData(GLOBAL_TEAM_ID);
        if (global == null) {
            return;
        }
        for (Task task : file.getAllTasks()) {
            if (effectiveScope(task) != TrackingScope.GLOBAL) {
                continue;
            }
            long progress = global.getProgress(task);
            if (team.getProgress(task) != progress) {
                team.setProgress(task, progress);
            }
            if (global.isCompleted(task) && !team.isCompleted(task)) {
                team.markTaskCompleted(task);
            }
        }
    }

    /**
     * Routes an editor/command force-progress write (reset or force-complete)
     * onto the pooled Global store. All such writes funnel through
     * {@code Task.forceProgress} on the operator's viewed (member) team, so
     * without this a Global reset lands on one team while the pooled counter
     * — and every other team mirroring it — keeps the old progress (and the
     * next catch-up even restores the resetter's copy). Swapping to the
     * global store lets the existing fan-out mirror the reset to every team,
     * exactly like progress. No-ops for non-Global tasks and client-side
     * files. Never creates the global team: if it does not exist yet nothing
     * is pooled, so the native write is an equivalent no-op.
     */
    public static TeamData routeForceProgressStore(Task task, TeamData current) {
        if (current == null || isGlobalStore(current)) {
            return current;
        }
        if (effectiveScope(task) != TrackingScope.GLOBAL) {
            return current;
        }
        BaseQuestFile file = current.getFile();
        if (file == null || !file.isServerSide()) {
            return current;
        }
        return lookupGlobalStore(file).orElse(current);
    }

    /**
     * Mirrors a Global claim receipt onto the claimer's member team so the
     * quest book updates. The grant itself is recorded on the pooled global
     * store (one claim server-wide for team rewards, per-UUID for the rest),
     * but that store has no player members so its claim-response sync reaches
     * nobody. Marking the same key on the member store only affects display
     * (checkmarks, unclaimed indicators) on clients that already sync that
     * team; the server gate and grant stay on the global store, so a stale
     * button can never double-grant. Skipped for repeatable quests, whose
     * display correctly follows the (never-cleared) member quest state.
     */
    public static void mirrorGlobalClaimReceipt(Quest quest, Reward reward, TeamData claimStore,
            TeamData nativeStore, ServerPlayer player) {
        if (quest == null || reward == null || claimStore == null || nativeStore == null || player == null) {
            return;
        }
        if (claimStore == nativeStore || !isGlobalStore(claimStore)) {
            return;
        }
        if (quest.canBeRepeated()) {
            return;
        }
        BaseQuestFile file = nativeStore.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        nativeStore.markRewardAsClaimed(player.getUUID(), reward, System.currentTimeMillis());
    }

    /**
     * Routes a quest-reset reward write onto the pooled Global store.
     * Quest-level resets recurse through {@code QuestObject.forceProgress}
     * into every task ({@code Task.forceProgress}, already rerouted) and
     * every reward ({@code Reward.forceProgress} calls
     * {@code TeamData.resetReward}, which un-claims). Without this the pooled
     * claim survives a Global quest reset: re-completion could never grant
     * again, unlike Team. Never creates the global team, same rationale as
     * {@link #routeForceProgressStore}.
     */
    public static TeamData routeRewardResetStore(Quest quest, TeamData current) {
        if (current == null || isGlobalStore(current) || quest == null) {
            return current;
        }
        if (questScope(quest) != TrackingScope.GLOBAL) {
            return current;
        }
        BaseQuestFile file = current.getFile();
        if (file == null || !file.isServerSide()) {
            return current;
        }
        return lookupGlobalStore(file).orElse(current);
    }

    /**
     * Mirrors a pooled reward-reset onto every member team so their quest
     * books update. The pooled claim is cleared by {@code Reward.forceProgress}
     * on the global store, but that store has no player members so its reset
     * sync reaches nobody — member stores would keep their (mirrored) claim
     * receipts and their checkmarks would stick. Clearing the same key on
     * each member reuses the native per-team sync; the key already encodes
     * team-vs-per-player semantics, so parity with Team resets is exact.
     * Member writes fail the {@link #isGlobalStore} check, so they terminate
     * there; the pooled self-write below would re-enter through the same
     * handler, so the whole body runs under the re-entrancy guard.
     *
     * <p>Deliberate deviation from native per-clicker clearing: FTB's reset
     * only un-claims the clicking admin's key, but a Global completion fans
     * claims out to every team under every online member's UUID. Clearing
     * only the clicker would leave survivors that block re-grants (the claim
     * record doubles as the grant gate) and stick checkmarks on other
     * members' books. A Global reset therefore clears the union of the
     * clicker and every member of every team, on the pool and all mirrors.
     */
    public static void mirrorGlobalRewardReset(TeamData globalStore, UUID playerId, Reward reward) {
        if (!isGlobalStore(globalStore) || reward == null) {
            return;
        }
        if (questScope(reward.getQuest()) != TrackingScope.GLOBAL) {
            return;
        }
        BaseQuestFile file = globalStore.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        if (REWARD_MIRROR_ACTIVE.get()) {
            return;
        }
        REWARD_MIRROR_ACTIVE.set(true);
        try {
            List<TeamData> members = poolMemberTeams(file);
            Set<UUID> keys = unionResetKeys(playerId, memberIdLists(members));
            for (UUID key : keys) {
                globalStore.resetReward(key, reward);
            }
            for (TeamData team : members) {
                for (UUID key : keys) {
                    team.resetReward(key, reward);
                }
            }
        } finally {
            REWARD_MIRROR_ACTIVE.remove();
        }
    }

    /**
     * Propagates a Solo quest-level reset's progress, flag, claim, and
     * cooldown clears to the personal stores of every member of the viewed
     * team. The native recursion runs on the viewed (party) store, which
     * holds no Solo data — without this the party copy zeroes while each
     * member's personal completion and claim survive, leaving a stuck
     * checkmark that blocks reclaiming after redoing the task. Mirrors the
     * Global approach: let native run, then fan the clearing out. Only tasks
     * whose effective scope is Solo are zeroed here; a Solo quest's Team or
     * Global tasks live on other stores with their own reset paths. Members
     * whose personal record is the invoked store itself (unpartied) are
     * skipped — native already cleared that record. Offline members are still
     * cleared server-side and pick the snapshot up on next login or request.
     */
    public static void propagateSoloQuestReset(QuestObject origin, TeamData invoked, ProgressChange change) {
        if (!(origin instanceof Quest quest)) {
            return;
        }
        if (change == null || !change.shouldReset()) {
            return;
        }
        if (questScope(quest) != TrackingScope.SOLO) {
            return;
        }
        if (invoked == null) {
            return;
        }
        BaseQuestFile file = invoked.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return;
        }
        Set<UUID> keys = unionResetKeys(change.getPlayerId(), List.of(teamMemberIds(invoked)));
        for (UUID memberId : teamMemberIds(invoked)) {
            if (memberId == null) {
                continue;
            }
            Optional<Team> personal = FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(memberId);
            if (personal.isEmpty()) {
                continue;
            }
            TeamData personalStore = file.getOrCreateTeamData(personal.get());
            if (personalStore.getTeamId().equals(invoked.getTeamId())) {
                continue;
            }
            for (Task task : quest.getTasks()) {
                if (effectiveScope(task) != TrackingScope.SOLO) {
                    continue;
                }
                if (personalStore.getProgress(task) != 0L) {
                    personalStore.setProgress(task, 0L);
                }
            }
            clearQuestFlags(personalStore, quest);
            for (Reward reward : quest.getRewards()) {
                for (UUID key : keys) {
                    personalStore.resetReward(key, reward);
                }
            }
            personalStore.clearRepeatCooldown(quest);
            syncTeamData(personalStore);
        }
    }

    /**
     * Pure key union for Global reward-reset clearing: the clicking admin
     * plus every member UUID of every member team. Package-visible for unit
     * tests; the TeamData writes themselves stay in
     * {@link #mirrorGlobalRewardReset}.
     */
    static Set<UUID> unionResetKeys(UUID actor, Collection<? extends Collection<UUID>> memberLists) {
        Set<UUID> keys = new LinkedHashSet<>();
        if (actor != null) {
            keys.add(actor);
        }
        if (memberLists != null) {
            for (Collection<UUID> ids : memberLists) {
                if (ids == null) {
                    continue;
                }
                for (UUID id : ids) {
                    if (id != null) {
                        keys.add(id);
                    }
                }
            }
        }
        return keys;
    }

    /**
     * Pure personal-snapshot decision shared by both ends of the sync. A
     * record is a personal snapshot when it is keyed by the player's personal
     * id (personal teams are keyed by player UUID, the same key
     * {@code getOrCreateTeamData(Team)} uses) while the effective team is
     * something else — i.e. the player is partied. Unpartied the two coincide
     * and the single record travels the native path. Nulls fail closed to
     * native behavior.
     *
     * <p>Public because the client sync mixin lives in a neighboring package;
     * the decision itself stays free of Minecraft and FTB types so it remains
     * unit-testable.
     */
    public static boolean isPersonalSnapshot(UUID incomingId, UUID playerId, UUID effectiveId) {
        return incomingId != null && playerId != null && effectiveId != null
                && incomingId.equals(playerId) && !playerId.equals(effectiveId);
    }

    /**
     * Sends the player's personal TeamData snapshot after the effective-team
     * snapshot went out (team-data request answers and team-change fan-outs
     * address the effective team only, so partied clients never see the store
     * their Solo progress lives on). Skips unpartied players, whose personal
     * and effective records are the same object already sent, and no-ops when
     * the teams manager or quest file is unavailable. Never touches
     * {@code selfTeamData}: the client files the record map-keyed without
     * overwriting its effective view.
     */
    public static void sendPersonalSnapshot(ServerPlayer player, TeamData effectiveStore) {
        if (player == null || effectiveStore == null) {
            return;
        }
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return;
        }
        Optional<ServerQuestFile> file = questFile();
        if (file.isEmpty()) {
            return;
        }
        UUID effectiveId = effectiveStore.getTeamId();
        Optional<Team> personal = FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(player.getUUID());
        if (personal.isEmpty()) {
            return;
        }
        if (!isPersonalSnapshot(personal.get().getId(), player.getUUID(), effectiveId)) {
            return;
        }
        TeamData snapshot = file.get().getOrCreateTeamData(personal.get());
        NetworkManager.sendToPlayer(player, new SyncTeamDataMessage(snapshot));
    }

    private static Set<UUID> teamMemberIds(TeamData team) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return Set.of();
        }
        return FTBTeamsAPI.api().getManager().getTeamByID(team.getTeamId())
                .map(Team::getMembers)
                .orElse(Set.of());
    }

    /**
     * Every non-pooled team record in the file, in no particular order.
     * Includes personal-team records alongside party teams; callers filter by
     * id when that matters.
     */
    private static List<TeamData> poolMemberTeams(BaseQuestFile file) {
        List<TeamData> members = new ArrayList<>();
        for (TeamData team : file.getAllTeamData()) {
            if (isGlobalStore(team)) {
                continue;
            }
            members.add(team);
        }
        return members;
    }

    private static List<Set<UUID>> memberIdLists(List<TeamData> members) {
        List<Set<UUID>> memberLists = new ArrayList<>();
        for (TeamData team : members) {
            memberLists.add(teamMemberIds(team));
        }
        return memberLists;
    }

    /**
     * Propagates a Global quest-level reset's started/completed flag clears
     * to the pooled store and every member team. The recursion clears flags
     * only on the invoked (viewed) team, so without this the pooled quest
     * would stay completed server-side (leaving the claim gate open) and
     * other members would keep showing the quest complete at zero progress.
     * Fires for Quest origins (chapters/files reach each quest through the
     * same recursion, each guarded by its own scope) and for the repeatable
     * claim-reset path, which invokes on the global store directly. Guarded
     * flag reads keep the common already-clear case sync-free.
     *
     * <p>Also re-clears every reward claim on the pool and all members. The
     * reward-recursion mirror normally handles this, but the un-claimed
     * postcondition is what re-grants depend on, so it is asserted here
     * directly; per-key {@code resetReward} calls are side-effect-free when
     * the key is already absent.
     */
    public static void propagateGlobalQuestReset(QuestObject origin, TeamData invoked, ProgressChange change) {
        if (!(origin instanceof Quest quest)) {
            return;
        }
        if (change == null || !change.shouldReset()) {
            return;
        }
        if (questScope(quest) != TrackingScope.GLOBAL) {
            return;
        }
        if (invoked == null) {
            return;
        }
        BaseQuestFile file = invoked.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        TeamData global = isGlobalStore(invoked) ? invoked : lookupGlobalStore(file).orElse(null);
        if (global == null) {
            return;
        }
        List<TeamData> members = poolMemberTeams(file);
        clearQuestFlags(global, quest);
        global.clearRepeatCooldown(quest);
        Set<UUID> keys = unionResetKeys(change.getPlayerId(), memberIdLists(members));
        for (Reward reward : quest.getRewards()) {
            for (UUID key : keys) {
                global.resetReward(key, reward);
            }
        }
        for (TeamData team : members) {
            clearQuestFlags(team, quest);
            // Native admin reset clears the cooldown only on the targeted team;
            // mirrors would otherwise keep stale cooldowns gating redos.
            team.clearRepeatCooldown(quest);
            for (Reward reward : quest.getRewards()) {
                for (UUID key : keys) {
                    team.resetReward(key, reward);
                }
            }
            syncTeamData(team);
        }
    }

    /**
     * Snapshots a Global repeatable quest's cooldown and completion count
     * before a claim lands. Called from the HEAD of
     * {@code TeamData.markRewardAsClaimed}; the TAIL counterpart compares and
     * propagates only on an actual repeat, so ordinary claims (including all
     * non-repeatable ones) stay sync-free. No-ops off the server, for
     * non-Global quests, and for non-repeatable quests.
     */
    public static void captureGlobalRepeatBefore(TeamData source, Quest quest) {
        REPEAT_BEFORE.remove();
        if (source == null || quest == null) {
            return;
        }
        if (questScope(quest) != TrackingScope.GLOBAL || !quest.canBeRepeated()) {
            return;
        }
        BaseQuestFile file = source.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        TeamDataProgressAccessor view = (TeamDataProgressAccessor) source;
        REPEAT_BEFORE.set(new long[]{
                view.artipelago$getRepeatableTime().get(quest.getId()),
                view.artipelago$getCompletionCount().get(quest.getId())});
    }

    /**
     * Propagates a Global repeatable quest's cooldown and completion count
     * when the enclosing claim actually repeated the quest. The repeat
     * cascade writes count and cooldown after
     * {@code Quest.resetProgressIfRepeatable} returns, so observing the whole
     * {@code markRewardAsClaimed} call is the only point where the propagated
     * values are final — reading them any earlier copies pre-claim state and
     * members end up with no cooldown, which is exactly the stuck
     * reset-with-no-timer fault. Unchanged state (claim refused, non-repeat)
     * propagates nothing.
     */
    public static void propagateGlobalRepeatStateAfter(TeamData source, Quest quest) {
        long[] before = REPEAT_BEFORE.get();
        REPEAT_BEFORE.remove();
        if (before == null || source == null || quest == null) {
            return;
        }
        if (questScope(quest) != TrackingScope.GLOBAL) {
            return;
        }
        BaseQuestFile file = source.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        TeamDataProgressAccessor view = (TeamDataProgressAccessor) source;
        long afterCooldown = view.artipelago$getRepeatableTime().get(quest.getId());
        int afterCount = view.artipelago$getCompletionCount().get(quest.getId());
        if (repeatStateChanged(before[0], (int) before[1], afterCooldown, afterCount)) {
            propagateGlobalRepeatState(source, quest);
        }
    }

    /**
     * Pure repeat detector: true when the cooldown instant or the completion
     * count moved across the claim. Package-visible for unit tests; the FTB
     * map reads stay in the capture/propagate pair above.
     */
    static boolean repeatStateChanged(long beforeCooldown, int beforeCount,
            long afterCooldown, int afterCount) {
        return beforeCooldown != afterCooldown || beforeCount != afterCount;
    }

    /**
     * Propagates a Global repeatable quest's cooldown and completion count
     * from the store that recorded the final claim (pool or member) to the
     * pool and every member team. Claims, repeat-resets and their cooldown
     * writes all happen claim-store-locally, and the quest book reads the
     * member store — without this a pool-side repeat leaves members showing
     * a reset quest with no cooldown (and a member-side repeat leaves the
     * pool stale). Repeatable receipts are intentionally not mirrored, so
     * there is nothing to un-mirror here. Callers must invoke only for an
     * actual repeat-reset; admin resets go through
     * {@link #propagateGlobalQuestReset}.
     */
    public static void propagateGlobalRepeatState(TeamData source, Quest quest) {
        if (source == null || quest == null) {
            return;
        }
        if (questScope(quest) != TrackingScope.GLOBAL) {
            return;
        }
        BaseQuestFile file = source.getFile();
        if (file == null || !file.isServerSide()) {
            return;
        }
        TeamData pool = isGlobalStore(source) ? source : lookupGlobalStore(file).orElse(null);
        if (pool == null) {
            return;
        }
        TeamDataProgressAccessor view = (TeamDataProgressAccessor) source;
        long cooldownAt = view.artipelago$getRepeatableTime().get(quest.getId());
        int count = view.artipelago$getCompletionCount().get(quest.getId());
        applyRepeatState(pool, quest, cooldownAt, count);
        for (TeamData team : file.getAllTeamData()) {
            if (isGlobalStore(team)) {
                continue;
            }
            applyRepeatState(team, quest, cooldownAt, count);
            syncTeamData(team);
        }
    }

    private static void applyRepeatState(TeamData team, Quest quest, long cooldownAt, int count) {
        TeamDataProgressAccessor view = (TeamDataProgressAccessor) team;
        if (cooldownAt > 0L) {
            view.artipelago$getRepeatableTime().put(quest.getId(), cooldownAt);
        } else {
            view.artipelago$getRepeatableTime().remove(quest.getId());
        }
        if (count > 0) {
            view.artipelago$getCompletionCount().put(quest.getId(), count);
        } else {
            view.artipelago$getCompletionCount().remove(quest.getId());
        }
        team.markDirty();
    }

    private static void syncTeamData(TeamData team) {
        Collection<ServerPlayer> online = team.getOnlineMembers();
        if (!online.isEmpty()) {
            NetworkManager.sendToPlayers(online, new SyncTeamDataMessage(team));
        }
    }

    private static void clearQuestFlags(TeamData team, Quest quest) {
        if (team.isStarted(quest)) {
            team.setStarted(quest.getId(), null);
        }
        if (team.isCompleted(quest)) {
            team.setCompleted(quest.getId(), null);
        }
    }

    private static Optional<TeamData> lookupGlobalStore(BaseQuestFile file) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return Optional.empty();
        }
        Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamByID(GLOBAL_TEAM_ID);
        if (team.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(file.getOrCreateTeamData(team.get()));
    }

    private static Optional<ServerQuestFile> questFile() {
        return ServerQuestFile.getInstance();
    }

    /**
     * Quest-level Tracking Scope for claims. Rewards belong to quests, not
     * tasks, so unlike progress (per-task effective scope) the claim group
     * follows the quest flag: Solo records on the claimer's personal store
     * (one claim per person), Team keeps native party behavior, Global
     * records on the server team (one claim server-wide). Per-player rewards
     * stay per-UUID on whichever store, so everyone still claims separately.
     */
    public static TrackingScope questScope(Quest quest) {
        // Quest is final and gains the holder interface only via mixin, so match through Object.
        Object questObject = quest;
        if (questObject instanceof QuestTrackingHolder holder) {
            return holder.artipelago$getTrackingScope();
        }
        return TrackingScope.TEAM;
    }

    public static TeamData resolveClaimStore(Quest quest, TeamData current, ServerPlayer player) {
        return switch (questScope(quest)) {
            case TEAM -> current;
            case SOLO -> personalStore(player).orElse(current);
            case GLOBAL -> globalStore(player).orElse(current);
        };
    }

    /**
     * Client-side claim store for the quest-book claim button. Never creates
     * teams or team data; falls back to the current (native) store when the
     * personal record has not synced yet, which degrades to today's behavior
     * instead of fabricating state.
     *
     * <p>Global always reads the viewed (member) store, never the pooled
     * client record: the global server team has no player members, so FTB
     * only ever syncs its progress to members — nobody — and the client copy
     * stays permanently empty. Member stores carry the mirrored Global
     * completion instead, which is what the button gates on. The server gate
     * and grant stay on the pooled store, so this display choice cannot
     * double-grant.
     */
    public static TeamData resolveClaimStoreClient(Quest quest, TeamData current, Player player) {
        // Claim display follows quest display: Solo reads personal, Team and
        // Global read the viewed member store (see resolveQuestDisplayStore).
        return resolveQuestDisplayStore(quest, current, player);
    }

    /**
     * Client-side display store for a task (progress bar, checkmark, tooltips,
     * submit gating). Solo reads the synced personal record; Team and Global
     * read the viewed (member) store — Global completion is mirrored onto
     * members, while the pooled record never syncs to clients. Never creates
     * anything; falls back to the current store when the personal record has
     * not synced yet. Nulls fail closed to the current store.
     */
    public static TeamData resolveTaskDisplayStore(Task task, TeamData current, Player player) {
        if (current == null || player == null || task == null) {
            return current;
        }
        BaseQuestFile file = current.getFile();
        if (file == null || file.isServerSide()) {
            return current;
        }
        return effectiveScope(task) == TrackingScope.SOLO
                ? clientPersonalStore(file, player).orElse(current)
                : current;
    }

    /**
     * Client-side display store for a quest (state, locks, started/completed
     * gates, cooldown timer, dependency checks). Same Solo rule as
     * {@link #resolveTaskDisplayStore}; quest-level scope decides.
     */
    public static TeamData resolveQuestDisplayStore(Quest quest, TeamData current, Player player) {
        if (current == null || player == null || quest == null) {
            return current;
        }
        BaseQuestFile file = current.getFile();
        if (file == null || file.isServerSide()) {
            return current;
        }
        return questScope(quest) == TrackingScope.SOLO
                ? clientPersonalStore(file, player).orElse(current)
                : current;
    }

    /**
     * Dispatches a mixed-scope display read to the task or quest resolver.
     * Non-quest/task objects (chapters, files) keep the current store.
     */
    public static TeamData resolveObjectDisplayStore(QuestObject obj, TeamData current, Player player) {
        if (obj instanceof Quest quest) {
            return resolveQuestDisplayStore(quest, current, player);
        }
        if (obj instanceof Task task) {
            return resolveTaskDisplayStore(task, current, player);
        }
        return current;
    }

    /**
     * Client-side display store for a reward-level read (blocked checks).
     * Rewards belong to quests, so the quest scope decides.
     */
    public static TeamData resolveRewardDisplayStore(Reward reward, TeamData current, Player player) {
        if (reward == null) {
            return current;
        }
        return resolveQuestDisplayStore(reward.getQuest(), current, player);
    }

    private static Optional<TeamData> clientPersonalStore(BaseQuestFile file, Player player) {
        // Personal teams are keyed by player UUID — the same key
        // getOrCreateTeamData(Team) uses, so this finds the record the server
        // wrote. Resolved directly instead of through the teams manager, which
        // is not loaded on clients connected to a remote server.
        return Optional.ofNullable(file.getNullableTeamData(player.getUUID()));
    }

    private static Optional<TeamData> personalStore(Player player) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            LOGGER.warn("[artipelago] FTB Teams manager not loaded; keeping native progress store");
            return Optional.empty();
        }
        Optional<ServerQuestFile> file = questFile();
        if (file.isEmpty()) {
            return Optional.empty();
        }
        Optional<Team> personal = FTBTeamsAPI.api().getManager().getPlayerTeamForPlayerID(player.getUUID());
        if (personal.isEmpty()) {
            LOGGER.warn("[artipelago] No personal team for {}; keeping native progress store", player.getScoreboardName());
            return Optional.empty();
        }
        return Optional.of(file.get().getOrCreateTeamData(personal.get()));
    }

    private static Optional<TeamData> globalStore(ServerPlayer player) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            LOGGER.warn("[artipelago] FTB Teams manager not loaded; keeping native progress store");
            return Optional.empty();
        }
        Optional<ServerQuestFile> file = questFile();
        if (file.isEmpty()) {
            return Optional.empty();
        }
        Team team = FTBTeamsAPI.api().getManager().getTeamByID(GLOBAL_TEAM_ID).orElseGet(() -> {
            try {
                return FTBTeamsAPI.api().getManager().createServerTeam(
                        player.createCommandSourceStack(),
                        "Artipelago Global",
                        "Server-wide quest progress",
                        null,
                        GLOBAL_TEAM_ID);
            } catch (Exception e) {
                LOGGER.warn("[artipelago] Could not create Global server team; keeping native progress store", e);
                return null;
            }
        });
        if (team == null) {
            return Optional.empty();
        }
        return Optional.of(file.get().getOrCreateTeamData(team));
    }
}
