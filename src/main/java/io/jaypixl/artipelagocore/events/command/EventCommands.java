package io.jaypixl.artipelagocore.events.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.jaypixl.artipelagocore.events.config.EventConfig;
import io.jaypixl.artipelagocore.events.config.EventConfigManager;
import io.jaypixl.artipelagocore.events.data.EventSavedData;
import io.jaypixl.artipelagocore.events.data.ScheduledEvent;
import io.jaypixl.artipelagocore.events.runtime.EventManager;
import io.jaypixl.artipelagocore.events.runtime.EventScheduler;
import io.jaypixl.artipelagocore.events.runtime.SpawnEffectOverlay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;

public final class EventCommands {
    private EventCommands() { }
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("events")
                .executes(EventCommands::list)
                .then(Commands.literal("list").executes(EventCommands::list)
                        .then(Commands.literal("detail").requires(source -> source.hasPermission(2)).executes(EventCommands::detail)))
                .then(Commands.literal("start").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("preset", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        EventConfigManager.get().presets.stream().map(preset -> preset.id).toList(), builder))
                                .then(Commands.argument("minutes", IntegerArgumentType.integer(1)).executes(EventCommands::start))))
                .then(Commands.literal("stop").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("preset", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        EventConfigManager.get().presets.stream().map(preset -> preset.id).toList(), builder))
                                .executes(EventCommands::stop)))
                .then(Commands.literal("shutdown").requires(source -> source.hasPermission(2)).executes(EventCommands::shutdown))
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(EventCommands::reload)));
    }
    private static int list(CommandContext<CommandSourceStack> context) {
        List<ScheduledEvent> events = EventManager.getActiveEvents(context.getSource().getServer()).stream().toList();
        if (events.isEmpty()) { context.getSource().sendSuccess(() -> Component.literal("No active events."), false); return 1; }
        for (ScheduledEvent event : events) sendEvent(context.getSource(), event, false);
        return events.size();
    }
    private static int detail(CommandContext<CommandSourceStack> context) {
        List<ScheduledEvent> events = EventManager.getActiveEvents(context.getSource().getServer()).stream().toList();
        if (events.isEmpty()) { context.getSource().sendSuccess(() -> Component.literal("No active events."), false); return 1; }
        for (ScheduledEvent event : events) sendEvent(context.getSource(), event, true);
        return events.size();
    }
    private static int start(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "preset");
        EventConfig.Preset preset = EventConfigManager.getPreset(presetId);
        if (preset == null) { context.getSource().sendFailure(Component.literal("Unknown event preset '" + presetId + "'.")); return 0; }
        long now = System.currentTimeMillis();
        int minutes = IntegerArgumentType.getInteger(context, "minutes");
        ScheduledEvent event = new ScheduledEvent("manual-" + presetId + "-" + now, preset.name, preset.description,
                now, now + minutes * 60_000L, preset.effects, presetId);
        EventSavedData.get(context.getSource().getServer().overworld()).add(event);
        SpawnEffectOverlay.refresh(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Started '" + preset.name + "' for " + minutes + " minutes."), true);
        return 1;
    }
    private static int stop(CommandContext<CommandSourceStack> context) {
        String presetId = StringArgumentType.getString(context, "preset");
        int stopped = EventSavedData.get(context.getSource().getServer().overworld()).removeByPreset(presetId);
        SpawnEffectOverlay.refresh(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal(stopped == 0 ? "No active instances of '" + presetId + "'." : "Stopped " + stopped + " instance(s) of '" + presetId + "'."), true);
        return stopped;
    }
    private static int shutdown(CommandContext<CommandSourceStack> context) {
        EventScheduler.shutdown(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("All event schedules and active effects have been halted."), true);
        return 1;
    }
    private static int reload(CommandContext<CommandSourceStack> context) {
        EventScheduler.reload(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Event config and schedules reloaded."), true);
        return 1;
    }
    private static void sendEvent(CommandSourceStack source, ScheduledEvent event, boolean detailed) {
        Component title = detailed
                ? Component.literal("[" + event.id() + "] ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(event.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                : Component.literal(event.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        source.sendSuccess(() -> title, false);
        for (String line : event.description()) {
            source.sendSuccess(() -> Component.literal("  " + line).withStyle(ChatFormatting.GRAY), false);
        }
    }
}
