package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.RewardButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * Client-side claim display and gating for the quest-book claim button.
 * The button reads the viewed (effective) team, so without this a Solo
 * reward completed on the personal store computes as unclaimable and the
 * click sends nothing. Global display intentionally stays on the viewed
 * member store: the pooled server team has no player members, so its
 * progress never syncs to clients and its client record is permanently
 * empty; member stores carry the mirrored Global completion instead. Reads
 * resolve without creating anything; a missing synced record falls back to
 * native display. Client-only: listed under {@code client} in the mixin
 * config so this class never loads on a dedicated server.
 */
@Mixin(RewardButton.class)
public class ClaimButtonMixin {

    @Redirect(method = {"onClicked", "draw"},
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;getClaimType(Ljava/util/UUID;Ldev/ftb/mods/ftbquests/quest/reward/Reward;)Ldev/ftb/mods/ftbquests/quest/reward/RewardClaimType;"))
    private RewardClaimType artipelago$claimTypeStore(TeamData self, UUID playerId, Reward reward) {
        TeamData store = QuestTrackingRouter.resolveClaimStoreClient(
                reward.getQuest(), self, FTBQuestsClient.getClientPlayer());
        return store.getClaimType(playerId, reward);
    }

    @Redirect(method = "draw",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;isCompleted(Ldev/ftb/mods/ftbquests/quest/QuestObject;)Z"))
    private boolean artipelago$alertStore(TeamData self, QuestObject quest) {
        if (!(quest instanceof Quest questObject)) {
            return self.isCompleted(quest);
        }
        TeamData store = QuestTrackingRouter.resolveClaimStoreClient(
                questObject, self, FTBQuestsClient.getClientPlayer());
        return store.isCompleted(quest);
    }
}
