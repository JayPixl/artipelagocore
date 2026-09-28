package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.net.RequestTeamDataMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sends the personal TeamData snapshot alongside the team-data request
 * answer. The request handler resolves the effective party team only, so a
 * partied client never receives the store its Solo progress lives on (Solo
 * submits land on the personal team while the quest book reads the party).
 * The extra send is skipped unpartied, where both records are the same object
 * already sent, and the client files it without overwriting its effective
 * view — see {@code PersonalSyncMixin}.
 */
@Mixin(RequestTeamDataMessage.class)
public class PersonalSnapshotMixin {

    @Inject(method = "lambda$handle$0", at = @At("TAIL"))
    private static void artipelago$sendPersonalSnapshot(ServerPlayer player, TeamData data, CallbackInfo ci) {
        QuestTrackingRouter.sendPersonalSnapshot(player, data);
    }
}
