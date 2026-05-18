package io.jaypixl.artipelagocore.yawp;

import de.z0rdak.yawp.api.MessageSender;
import de.z0rdak.yawp.api.events.flag.FlagCheckResult;
import de.z0rdak.yawp.api.permission.Permissions;
import de.z0rdak.yawp.core.flag.FlagState;
import de.z0rdak.yawp.core.flag.RegionFlag;
import de.z0rdak.yawp.platform.event.NeoForgeFlagCheckResult;
import de.z0rdak.yawp.util.text.messages.FlagMessageBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class YawpHoeTillFixEvents {

    private YawpHoeTillFixEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onFlagResult(NeoForgeFlagCheckResult event) {
        if (event.getFlagCheck().getRegionFlag() != RegionFlag.HOE_TILL) {
            return;
        }

        if (!(event.getFlagCheck().getPlayer() instanceof ServerPlayer player) || event.getResponsible() == null) {
            return;
        }

        if (event.getFlagState() == FlagState.DENIED && Permissions.playerHasBypassPermission(event.getResponsible(), player)) {
            event.setFlagState(FlagState.DISABLED);
            return;
        }

        if (event.getFlagState() != FlagState.DENIED || event.getFlag() == null) {
            return;
        }

        if (event.getFlag().getFlagMsg().isMuted() || event.getResponsible().isMuted()) {
            return;
        }

        FlagCheckResult result = NeoForgeFlagCheckResult.asNonEvent(event);
        MessageSender.sendNotification(player, FlagMessageBuilder.buildFrom(result, null));
    }
}
