package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.StatTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.XPTask;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Reroutes the progress store for every task type that submits through
 * {@code submitTask}. These five overrides are the complete set in FTB
 * Quests 2101.1.36: all other task types inherit from them, and the final
 * two-arg {@code submitTask} (auto-submit tick, manual submit packet, stage
 * checks, inventory detection) delegates into the three-arg override.
 */
@Mixin({AbstractBooleanTask.class, CustomTask.class, ItemTask.class, StatTask.class, XPTask.class})
public class SubmitStoreMixin {

    /**
     * NOTE: Mixin validates extra {@code @ModifyVariable} handler params
     * against the target args starting from index 0, so capturing
     * {@code player} (target arg 1) requires repeating the {@code TeamData}
     * head of the arg list first. The middle param is the same value as
     * {@code data} and is intentionally unused.
     */
    @ModifyVariable(
            method = "submitTask(Ldev/ftb/mods/ftbquests/quest/TeamData;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TeamData artipelago$rerouteStore(TeamData data, TeamData ignored, ServerPlayer player) {
        return QuestTrackingRouter.resolveStore((Task) (Object) this, data, player);
    }
}
