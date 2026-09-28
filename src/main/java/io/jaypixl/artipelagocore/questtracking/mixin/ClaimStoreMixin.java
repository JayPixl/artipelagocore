package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reroutes single-reward claims onto the quest's claim store. The handler
 * resolves the player's native team, but for Solo/Global quests completion
 * lives elsewhere: the native {@code isCompleted} gate would refuse the
 * claim, and recording on the native team would give every party its own
 * copy of a Global team reward. Both the gate and the record follow the
 * quest-level Tracking Scope; Team behavior is byte-for-byte native.
 */
@Mixin(ClaimRewardMessage.class)
public class ClaimStoreMixin {

    @Redirect(method = "lambda$handle$0",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;isCompleted(Ldev/ftb/mods/ftbquests/quest/QuestObject;)Z"))
    private static boolean artipelago$gateClaimStore(TeamData nativeStore, QuestObject quest,
            Reward reward, ServerPlayer player) {
        TeamData store = QuestTrackingRouter.resolveClaimStore(reward.getQuest(), nativeStore, player);
        return store.isCompleted(quest);
    }

    @Redirect(method = "lambda$handle$0",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftbquests/quest/TeamData;claimReward(Lnet/minecraft/server/level/ServerPlayer;Ldev/ftb/mods/ftbquests/quest/reward/Reward;Z)V"))
    private static void artipelago$recordClaimStore(TeamData nativeStore, ServerPlayer player,
            Reward reward, boolean notify) {
        TeamData store = QuestTrackingRouter.resolveClaimStore(reward.getQuest(), nativeStore, player);
        store.claimReward(player, reward, notify);
        QuestTrackingRouter.mirrorGlobalClaimReceipt(reward.getQuest(), reward, store, nativeStore, player);
    }
}
