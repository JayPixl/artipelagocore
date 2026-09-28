package io.jaypixl.artipelagocore.eggmoves.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.jaypixl.artipelagocore.eggmoves.config.EggMovesConfigManager;
import io.jaypixl.artipelagocore.eggmoves.runtime.PastureTeachingService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class EggMovesCommands {
    private EggMovesCommands() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eggmoves")
                .then(Commands.literal("reload").requires(source -> source.hasPermission(2)).executes(EggMovesCommands::reload)));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        EggMovesConfigManager.load();
        PastureTeachingService.clearTimers();
        context.getSource().sendSuccess(() -> Component.literal("Egg move config reloaded; pending teaching timers cleared."), true);
        return 1;
    }
}
