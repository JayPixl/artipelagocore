package io.jaypixl.artipelagocore.regionmarket;

import de.z0rdak.yawp.platform.event.NeoForgeRegionEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Objects;

public class RegionMarketEvents {
    @SubscribeEvent
    public static void onRegionRename(NeoForgeRegionEvent.Rename event) {
        ServerPlayer player = event.getPlayer();

        String oldName = event.getOldName();
        String newName = event.getNewName();

        try {
            RegionMarketSavedData data = RegionMarketSavedData.get(player.serverLevel());
            for (RegionMarketEntry entry : data.getEntries().values()) {
                if (Objects.equals(entry.getId(), oldName)) {
                    data.addListing(newName, entry.getCost(), entry.getOwner(), entry.getIsStarter());
                    data.removeListing(oldName);
                }
            }
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("ERROR renaming region!"));
            event.setCanceled(true);
        }
    }
}
