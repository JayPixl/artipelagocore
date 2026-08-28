package io.jaypixl.artipelagocore.events.runtime;

import io.jaypixl.artipelagocore.events.data.ScheduledEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/** Sends consistent, styled lifecycle announcements to every connected player. */
public final class EventAnnouncements {
    private EventAnnouncements() { }

    public static void announceStart(MinecraftServer server, ScheduledEvent event) {
        broadcast(server, Component.literal("✦ Event Started: ").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .append(Component.literal(event.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)), event);
    }

    public static void announceEnd(MinecraftServer server, ScheduledEvent event) {
        broadcast(server, Component.literal("✦ Event Ended: ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(Component.literal(event.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)), event);
    }

    private static void broadcast(MinecraftServer server, Component title, ScheduledEvent event) {
        server.getPlayerList().broadcastSystemMessage(title, false);
        for (String line : event.description()) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("  " + line).withStyle(ChatFormatting.GRAY), false);
        }
    }
}
