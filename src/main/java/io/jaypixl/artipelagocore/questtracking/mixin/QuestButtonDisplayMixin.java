package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Client-side state display for the quest-board button. Same gap as the task
 * button: the widget reads the viewed team, so a Solo quest completed on the
 * personal store renders with party state (wrong tint, locks, alerts, pins).
 * Swapped to the quest's display store; correct for every read centered on
 * this quest. Known limitation: dependency overlays resolve the viewed
 * quest's scope, so a Solo quest gated behind a Team-scope (or reverse)
 * dependency renders the dependency edge natively — quest completion itself
 * is unaffected. Team and Global read the viewed store unchanged.
 * Client-only.
 */
@Mixin(QuestButton.class)
public class QuestButtonDisplayMixin {

    @Shadow
    Quest quest;

    // 180 is GETFIELD (org.spongepowered.asm.lib.Opcodes is runtime-only,
    // so the literal is used to stay compilable).
    @Redirect(method = "*",
            at = @At(value = "FIELD",
                    target = "Ldev/ftb/mods/ftbquests/client/ClientQuestFile;selfTeamData:Ldev/ftb/mods/ftbquests/quest/TeamData;",
                    opcode = 180))
    private TeamData artipelago$questDisplayStore(ClientQuestFile file) {
        TeamData self = file.selfTeamData;
        if (self == null) {
            return null;
        }
        return QuestTrackingRouter.resolveQuestDisplayStore(quest, self, FTBQuestsClient.getClientPlayer());
    }
}
