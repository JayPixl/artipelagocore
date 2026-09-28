package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Files the personal TeamData snapshot without overwriting the effective
 * view. The native handler unconditionally assigns every synced record to
 * {@code selfTeamData}, so a personal snapshot arriving after the party
 * snapshot would hijack the quest book (and the reverse order would drop
 * the party view). Personal records are keyed in {@code teamDataMap} by
 * personal UUID either way, which is what Solo display reads resolve
 * against. Falls back to native behavior when the local player or the
 * client team manager is unavailable. Client-only: listed under
 * {@code client} in the mixin config so this class never loads on a
 * dedicated server.
 */
@Mixin(FTBQuestsNetClient.class)
public class PersonalSyncMixin {

    @Inject(method = "syncTeamData", at = @At("HEAD"), cancellable = true)
    private static void artipelago$keepPersonalSnapshot(TeamData data, CallbackInfo ci) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        Player player = Minecraft.getInstance().player;
        if (file == null || data == null || player == null) {
            return;
        }
        if (!FTBTeamsAPI.api().isClientManagerLoaded()) {
            return;
        }
        UUID effectiveId = FTBTeamsAPI.api().getClientManager()
                .getKnownPlayer(player.getUUID())
                .map(knownPlayer -> knownPlayer.teamId())
                .orElse(null);
        if (QuestTrackingRouter.isPersonalSnapshot(data.getTeamId(), player.getUUID(), effectiveId)) {
            file.addData(data, true);
            ci.cancel();
        }
    }
}
