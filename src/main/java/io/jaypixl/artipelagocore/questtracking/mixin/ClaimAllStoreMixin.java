package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.net.ClaimAllRewardsMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Same claim-store rerouting as {@link ClaimStoreMixin} for the claim-all
 * path. Without it, claim-all would skip Solo rewards (native completion
 * gate fails) and hand every party its own copy of Global team rewards.
 * The gate handler carries the enclosing lambda's leading args because
 * Mixin requires captured args to mirror the target args from index 0.
 */
@Mixin(ClaimAllRewardsMessage.class)
public class ClaimAllStoreMixin {

    @Redirect(method = "lambda$handle$1",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;isCompleted(Ldev/ftb/mods/ftbquests/quest/QuestObject;)Z"))
    private static boolean artipelago$gateClaimAllStore(TeamData nativeStore, QuestObject quest,
            TeamData ignored, ClaimAllRewardsMessage message, ServerPlayer player) {
        if (!(quest instanceof Quest questObject)) {
            return nativeStore.isCompleted(quest);
        }
        TeamData store = QuestTrackingRouter.resolveClaimStore(questObject, nativeStore, player);
        return store.isCompleted(quest);
    }

    @Redirect(method = "lambda$handle$0",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;claimReward(Lnet/minecraft/server/level/ServerPlayer;Ldev/ftb/mods/ftbquests/quest/reward/Reward;Z)V"))
    private static void artipelago$recordClaimAllStore(TeamData nativeStore, ServerPlayer player,
            Reward reward, boolean notify) {
        TeamData store = QuestTrackingRouter.resolveClaimStore(reward.getQuest(), nativeStore, player);
        store.claimReward(player, reward, notify);
        QuestTrackingRouter.mirrorGlobalClaimReceipt(reward.getQuest(), reward, store, nativeStore, player);
    }
}
