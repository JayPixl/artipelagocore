package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Credits both participants of a completed {@code /lctrade} trade.
 *
 * <p>Called from the {@code PlayerTrade} mixin at the return of
 * {@code tryExecuteTrade} while both trade containers are still populated.
 * Each player counts what they gave, what they received, or both according to
 * their own task's Counts mode.
 */
public final class PlayerTradeHandler {

    private PlayerTradeHandler() {
    }

    public static void onCompletedTrade(UUID hostId, UUID guestId,
            List<ItemStack> hostItems, List<ItemStack> guestItems) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ServerPlayer host = server.getPlayerList().getPlayer(hostId);
        ServerPlayer guest = server.getPlayerList().getPlayer(guestId);
        if (host == null || guest == null) {
            return;
        }
        ServerQuestFile file = ServerQuestFile.getInstance().orElse(null);
        if (file == null) {
            return;
        }
        List<TradeItemTask> tasks = file.collect(TradeItemTask.class);
        if (tasks.isEmpty()) {
            return;
        }

        List<TradeDelta.ItemAmount> hostAmounts = TradeAmounts.of(hostItems);
        List<TradeDelta.ItemAmount> guestAmounts = TradeAmounts.of(guestItems);
        credit(file, tasks, host, hostAmounts, guestAmounts);
        credit(file, tasks, guest, guestAmounts, hostAmounts);
    }

    /**
     * Snapshots a trade container now, before the completed trade is reaped
     * and its stacks are handed to the players.
     */
    public static List<ItemStack> snapshot(Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        if (container == null) {
            return stacks;
        }
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            stacks.add(container.getItem(slot).copy());
        }
        return stacks;
    }

    private static void credit(ServerQuestFile file, List<TradeItemTask> tasks, ServerPlayer player,
            List<TradeDelta.ItemAmount> own, List<TradeDelta.ItemAmount> other) {
        TeamData data = file.getTeamData(player).orElse(null);
        if (data == null) {
            return;
        }
        for (TradeItemTask task : tasks) {
            long delta = task.delta(own, other);
            if (delta > 0L) {
                task.submitTrade(data, player, delta);
            }
        }
    }
}
