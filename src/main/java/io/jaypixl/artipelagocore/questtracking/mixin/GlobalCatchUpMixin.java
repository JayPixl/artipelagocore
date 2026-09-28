package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamCreatedEvent;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catches teams up with finished Global quests on arrival. Live teams track
 * the pooled store write-by-write, but a team created (or joined) after a
 * Global completion missed those writes, and native merges only carry what
 * the creator's personal store holds — empty for brand-new players. Each hook
 * runs after FTB's own team-data setup so the TeamData exists.
 */
@Mixin(ServerQuestFile.class)
public class GlobalCatchUpMixin {

    @Inject(method = "teamCreated", at = @At("TAIL"))
    private void artipelago$catchUpNewTeam(TeamCreatedEvent event, CallbackInfo ci) {
        ServerQuestFile file = (ServerQuestFile) (Object) this;
        QuestTrackingRouter.catchUpTeam(file.getOrCreateTeamData(event.getTeam()));
    }

    @Inject(method = "playerChangedTeam", at = @At("TAIL"))
    private void artipelago$catchUpChangedTeam(PlayerChangedTeamEvent event, CallbackInfo ci) {
        ServerQuestFile file = (ServerQuestFile) (Object) this;
        QuestTrackingRouter.catchUpTeam(file.getOrCreateTeamData(event.getTeam()));
    }

    @Inject(method = "playerLoggedIn", at = @At("TAIL"))
    private void artipelago$catchUpOnLogin(PlayerLoggedInAfterTeamEvent event, CallbackInfo ci) {
        ServerQuestFile file = (ServerQuestFile) (Object) this;
        QuestTrackingRouter.catchUpTeam(file.getOrCreateTeamData(event.getTeam()));
    }
}
