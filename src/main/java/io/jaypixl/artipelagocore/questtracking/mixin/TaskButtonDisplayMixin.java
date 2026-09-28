package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Client-side progress display for the per-task button. The button reads the
 * viewed (effective) team, so a Solo task completed on the personal store
 * renders at party progress (usually zero) with no checkmark, even though the
 * personal record syncs correctly underneath. Swapping the single
 * {@code selfTeamData} read to the task's display store fixes every read in
 * this widget at once (progress bar, checkmark, tooltips, submit gating);
 * the swap is scope-correct because the widget centers on exactly one task
 * and resolves that task's effective scope (quest default plus task
 * override). Team and Global read the viewed store unchanged. Client-only.
 */
@Mixin(TaskButton.class)
public class TaskButtonDisplayMixin {

    @Shadow
    Task task;

    // 180 is GETFIELD (org.spongepowered.asm.lib.Opcodes is runtime-only,
    // so the literal is used to stay compilable).
    @Redirect(method = "*",
            at = @At(value = "FIELD",
                    target = "Ldev/ftb/mods/ftbquests/client/ClientQuestFile;selfTeamData:Ldev/ftb/mods/ftbquests/quest/TeamData;",
                    opcode = 180))
    private TeamData artipelago$taskDisplayStore(ClientQuestFile file) {
        TeamData self = file.selfTeamData;
        if (self == null) {
            return null;
        }
        return QuestTrackingRouter.resolveTaskDisplayStore(task, self, FTBQuestsClient.getClientPlayer());
    }
}
