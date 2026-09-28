package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.FTBQuestsEventHandler;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * Attributes kill credit per task. The kill loop resolves one team store for
 * the killer and then credits every kill task, so the killer is stashed when
 * the store resolves and each {@code KillTask.kill} call is rerouted to its
 * own task's store. Both redirects fire synchronously on the server thread
 * within a single kill event.
 */
@Mixin(FTBQuestsEventHandler.class)
public class KillAttributionMixin {

    @Redirect(method = "playerKill",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/ServerQuestFile;getTeamData(Lnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
    private Optional<TeamData> artipelago$captureSubmitter(ServerQuestFile file, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            QuestTrackingRouter.pushSubmitter(serverPlayer);
        }
        return file.getTeamData(player);
    }

    @Redirect(method = "lambda$playerKill$0",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/task/KillTask;kill(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void artipelago$routeKill(KillTask task, TeamData data, LivingEntity entity) {
        ServerPlayer submitter = QuestTrackingRouter.currentSubmitter();
        TeamData store = submitter == null ? data : QuestTrackingRouter.resolveStore(task, data, submitter);
        task.kill(store, entity);
    }
}
