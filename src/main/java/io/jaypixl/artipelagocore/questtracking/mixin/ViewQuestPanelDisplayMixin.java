package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Client-side quest-detail display. Unlike the single-object buttons, this
 * panel mixes scopes: the task list iterates tasks that may override the
 * quest scope, so each read resolves its own store instead of swapping the
 * whole panel. Covered reads: per-task completion in the task list (task
 * scope), reward-blocked checks (reward's quest scope), quest gates and the
 * repeatable cooldown timer (quest scope), and the searchable check (object
 * scope). Task progress bars and claim buttons inside the panel are separate
 * widgets with their own reroutes. Team and Global read the viewed store
 * unchanged. Client-only.
 */
@Mixin(ViewQuestPanel.class)
public class ViewQuestPanelDisplayMixin {

    @Redirect(method = "*",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;isCompleted(Ldev/ftb/mods/ftbquests/quest/QuestObject;)Z"))
    private boolean artipelago$completionDisplayStore(TeamData self, QuestObject quest) {
        return QuestTrackingRouter.resolveObjectDisplayStore(
                quest, self, FTBQuestsClient.getClientPlayer()).isCompleted(quest);
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;isRewardBlocked(Ldev/ftb/mods/ftbquests/quest/reward/Reward;)Z"))
    private boolean artipelago$blockedDisplayStore(TeamData self, Reward reward) {
        return QuestTrackingRouter.resolveRewardDisplayStore(
                reward, self, FTBQuestsClient.getClientPlayer()).isRewardBlocked(reward);
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;canStartTasks(Ldev/ftb/mods/ftbquests/quest/Quest;)Z"))
    private boolean artipelago$gatesDisplayStore(TeamData self, Quest quest) {
        return QuestTrackingRouter.resolveQuestDisplayStore(
                quest, self, FTBQuestsClient.getClientPlayer()).canStartTasks(quest);
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;getMilliSecondsUntilRepeatable(Ldev/ftb/mods/ftbquests/quest/Quest;)J"))
    private long artipelago$cooldownDisplayStore(TeamData self, Quest quest) {
        return QuestTrackingRouter.resolveQuestDisplayStore(
                quest, self, FTBQuestsClient.getClientPlayer()).getMilliSecondsUntilRepeatable(quest);
    }

    @Redirect(method = "*",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/QuestObject;isSearchable(Ldev/ftb/mods/ftbquests/quest/TeamData;)Z"))
    private boolean artipelago$searchableDisplayStore(QuestObject obj, TeamData self) {
        return obj.isSearchable(QuestTrackingRouter.resolveObjectDisplayStore(
                obj, self, FTBQuestsClient.getClientPlayer()));
    }
}
