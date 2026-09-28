package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Routes quest-reset reward writes for Global quests onto the pooled server
 * store. Quest-level resets recurse into every reward via the final
 * {@code Reward.forceProgress}, which calls {@code TeamData.resetReward} to
 * un-claim; without this the pooled claim survives a Global quest reset, so
 * re-completion could never grant again, unlike Team. The existing
 * reward-reset fan-out then clears every member's receipt. Solo/Team quests
 * pass through untouched.
 */
@Mixin(Reward.class)
public class RewardResetStoreMixin {

    /**
     * NOTE: the handler takes only the modified variable (same rule as
     * {@code ForceProgressStoreMixin}: extra params must mirror the target
     * args from index 0, and the quest comes from the reward itself).
     */
    @ModifyVariable(
            method = "forceProgress(Ldev/ftb/mods/ftbquests/quest/TeamData;Ldev/ftb/mods/ftbquests/util/ProgressChange;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TeamData artipelago$routeRewardReset(TeamData data) {
        return QuestTrackingRouter.routeRewardResetStore(((Reward) (Object) this).getQuest(), data);
    }
}
