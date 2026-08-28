package io.jaypixl.artipelagocore.events.runtime;

import io.jaypixl.artipelagocore.events.config.EventConfigManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EventModuleEvents {
    private static int cleanupTicks;
    private EventModuleEvents() { }
    @SubscribeEvent public static void onServerStarted(ServerStartedEvent event) { EventConfigManager.load(); EventScheduler.refresh(event.getServer()); }
    @SubscribeEvent public static void onServerTick(ServerTickEvent.Post event) { if (++cleanupTicks >= 1200) { cleanupTicks = 0; EventScheduler.refresh(event.getServer()); } }
}
