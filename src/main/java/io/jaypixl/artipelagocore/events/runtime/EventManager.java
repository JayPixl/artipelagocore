package io.jaypixl.artipelagocore.events.runtime;

import net.minecraft.server.MinecraftServer;
import io.jaypixl.artipelagocore.events.data.EventSavedData;
import io.jaypixl.artipelagocore.events.data.ScheduledEvent;
import io.jaypixl.artipelagocore.events.effect.EventEffect;
import java.util.Collection;
import java.util.List;

/** Central read API for integrations that apply event effects. */
public final class EventManager {
    private EventManager() { }
    public static Collection<ScheduledEvent> getActiveEvents(MinecraftServer server) { return EventSavedData.get(server.overworld()).getActive(System.currentTimeMillis()); }
    public static List<EventEffect> getActiveEffects(MinecraftServer server, String type) { return getActiveEvents(server).stream().flatMap(event -> event.effects().stream()).filter(effect -> effect.type.equals(type)).toList(); }
    public static double getMultiplier(MinecraftServer server, String type, String source) {
        double multiplier = 1.0;
        for (EventEffect effect : getActiveEffects(server, type)) {
            if (effect.multiplier != null) multiplier *= effect.multiplier;
        }
        return multiplier;
    }
    public static void removeExpiredEvents(MinecraftServer server) { EventSavedData.get(server.overworld()).removeExpired(System.currentTimeMillis()); }
}
