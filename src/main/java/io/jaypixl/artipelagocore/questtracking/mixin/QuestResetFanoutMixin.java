package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.ProgressChange;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Propagates a quest-level reset's clears to the stores the native recursion
 * misses. The {@code QuestObject.forceProgress}
 * recursion clears flags only on the invoked (viewed) team while tasks and
 * rewards fan out globally; without this the pooled quest would stay
 * completed server-side (leaving the claim gate open) and other members
 * would keep showing the quest complete at zero progress. Chapters and files
 * reach each quest through the same recursion, each guarded by its own
 * scope; the repeatable claim-reset path invokes on the global store
 * directly and is covered too. Solo members get the analogous personal-store
 * fan-out; Team quests are untouched by either branch.
 */
@Mixin(QuestObject.class)
public class QuestResetFanoutMixin {

    @Inject(
            method = "forceProgress(Ldev/ftb/mods/ftbquests/quest/TeamData;Ldev/ftb/mods/ftbquests/util/ProgressChange;)V",
            at = @At("TAIL"))
    private void artipelago$propagateQuestReset(TeamData data, ProgressChange change, CallbackInfo ci) {
        QuestTrackingRouter.propagateGlobalQuestReset((QuestObject) (Object) this, data, change);
        QuestTrackingRouter.propagateSoloQuestReset((QuestObject) (Object) this, data, change);
    }
}
