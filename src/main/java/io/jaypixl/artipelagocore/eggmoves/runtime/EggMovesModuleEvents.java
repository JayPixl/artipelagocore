package io.jaypixl.artipelagocore.eggmoves.runtime;

import io.jaypixl.artipelagocore.eggmoves.config.EggMovesConfigManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EggMovesModuleEvents {
    private static int teachingTicks;

    private EggMovesModuleEvents() { }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        EggMovesConfigManager.load();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++teachingTicks >= EggMovesConfigManager.get().checkIntervalTicks) {
            teachingTicks = 0;
            PastureTeachingService.tick(event.getServer());
        }
    }
}
