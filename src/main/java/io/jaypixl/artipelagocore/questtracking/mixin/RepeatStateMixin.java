package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Propagates Global repeatable-quest cooldown/count after the claim that
 * repeated the quest. The whole repeat cascade — progress/flag reset, then
 * completion-count increment, then cooldown write — runs inside
 * {@code TeamData.markRewardAsClaimed}, so this observes the full call:
 * HEAD snapshots the pre-claim state, TAIL propagates only when the state
 * moved. Reading the state any earlier (e.g. at the TAIL of
 * {@code Quest.resetProgressIfRepeatable}, which runs before the count and
 * cooldown are written) copies pre-claim values, leaving members with no
 * cooldown: the stuck reset-with-no-timer fault. Fires for single and
 * claim-all paths alike, since both funnel through
 * {@code markRewardAsClaimed}; admin resets never pass through here and stay
 * on {@code propagateGlobalQuestReset}.
 */
@Mixin(TeamData.class)
public class RepeatStateMixin {

    @Inject(
            method = "markRewardAsClaimed(Ljava/util/UUID;Ldev/ftb/mods/ftbquests/quest/reward/Reward;J)Z",
            at = @At("HEAD"))
    private void artipelago$captureRepeatBefore(UUID player, Reward reward, long time,
            CallbackInfoReturnable<Boolean> cir) {
        QuestTrackingRouter.captureGlobalRepeatBefore(
                (TeamData) (Object) this, reward == null ? null : reward.getQuest());
    }

    @Inject(
            method = "markRewardAsClaimed(Ljava/util/UUID;Ldev/ftb/mods/ftbquests/quest/reward/Reward;J)Z",
            at = @At("TAIL"))
    private void artipelago$propagateRepeatAfter(UUID player, Reward reward, long time,
            CallbackInfoReturnable<Boolean> cir) {
        QuestTrackingRouter.propagateGlobalRepeatStateAfter(
                (TeamData) (Object) this, reward == null ? null : reward.getQuest());
    }
}
