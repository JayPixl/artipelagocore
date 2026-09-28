package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Routes editor/command force-progress writes for Global tasks onto the
 * pooled server store. The quest editor's reset and force-complete buttons
 * plus {@code /ftbquests change_progress} all funnel through the final
 * {@code Task.forceProgress} on the operator's viewed (member) team; without
 * this a Global reset clears one team while the pooled counter — and every
 * team mirroring it — keeps the old value. The existing fan-out then mirrors
 * the write to every team. Solo/Team tasks pass through untouched.
 */
@Mixin(Task.class)
public class ForceProgressStoreMixin {

    /**
     * NOTE: the handler takes only the modified variable. Capturing the
     * trailing {@code ProgressChange} arg would violate Mixin's rule (extra
     * {@code @ModifyVariable} handler params must mirror the target args
     * from index 0), and the reroute decision does not need it.
     */
    @ModifyVariable(
            method = "forceProgress(Ldev/ftb/mods/ftbquests/quest/TeamData;Ldev/ftb/mods/ftbquests/util/ProgressChange;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private TeamData artipelago$routeForceProgress(TeamData data) {
        return QuestTrackingRouter.routeForceProgressStore((Task) (Object) this, data);
    }
}
