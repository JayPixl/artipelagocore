package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sends the personal TeamData snapshot to a player arriving on a new team.
 * The native team-change fan-out syncs the new effective team to its online
 * members only, so a joiner's Solo store would stay client-side empty until
 * the next team-data request. Runs after FTB's own sync so ordering matches
 * the request path (effective first, personal second). Offline changes carry
 * no player and are skipped.
 */
@Mixin(ServerQuestFile.class)
public class PersonalTeamChangeMixin {

    @Inject(method = "playerChangedTeam", at = @At("TAIL"))
    private void artipelago$sendPersonalSnapshot(PlayerChangedTeamEvent event, CallbackInfo ci) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        ServerQuestFile file = (ServerQuestFile) (Object) this;
        QuestTrackingRouter.sendPersonalSnapshot(player, file.getOrCreateTeamData(event.getTeam()));
    }
}
