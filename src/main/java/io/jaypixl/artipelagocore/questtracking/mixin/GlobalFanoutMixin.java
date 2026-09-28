package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Fans Global progress out to every team. All Global writes (submit reroutes,
 * kill attribution, admin force-completes) funnel through
 * {@code TeamData.setProgress} / {@code markTaskCompleted} on the fixed-UUID
 * server team, which has no online members — so its native sync and toasts
 * reach nobody. Mirroring each write onto every member team reuses the native
 * per-team sync, quest cascade, and toast paths exactly once per team.
 *
 * <p>The same memberless-sync problem applies to reward un-claims, so a
 * pooled {@code resetReward} is mirrored the same way (clearing each
 * member's claim receipt with the native sync). Member writes fail the
 * global check and terminate there; the pooled self-write re-enters through
 * this same handler, so the router guards the reward mirror against
 * re-entrancy.
 */
@Mixin(TeamData.class)
public class GlobalFanoutMixin {

    @Inject(method = "setProgress", at = @At("TAIL"))
    private void artipelago$mirrorGlobal(Task task, long progress, CallbackInfo ci) {
        QuestTrackingRouter.mirrorGlobalTask(task, (TeamData) (Object) this);
    }

    @Inject(method = "markTaskCompleted", at = @At("TAIL"))
    private void artipelago$fanOutCompletion(Task task, CallbackInfo ci) {
        QuestTrackingRouter.mirrorGlobalTask(task, (TeamData) (Object) this);
    }

    @Inject(method = "resetReward", at = @At("TAIL"))
    private void artipelago$fanOutRewardReset(UUID playerId, Reward reward,
            CallbackInfoReturnable<Boolean> cir) {
        QuestTrackingRouter.mirrorGlobalRewardReset((TeamData) (Object) this, playerId, reward);
    }
}
