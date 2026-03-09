package io.jaypixl.artipelagocore.regionmarket;

import de.z0rdak.yawp.api.events.region.NeoForgeRegionEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Objects;

public class RegionMarketEvents {
    @SubscribeEvent
    public static void onRegionRename(NeoForgeRegionEvent.Rename event) {
        Player player = event.getPlayer();

        if (!(player instanceof ServerPlayer)) return;

        String oldName = event.getOldName();
        String newName = event.getNewName();

        try {
            RegionMarketSavedData data = RegionMarketSavedData.get(((ServerPlayer) player).serverLevel());
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
