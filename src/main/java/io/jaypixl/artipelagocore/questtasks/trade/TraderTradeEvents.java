package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.CoinCurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * Normalizes one completed Lightman's Currency trader trade and fans progress
 * out to the Customer and Merchant Transaction tasks.
 *
 * <p>Only {@link ItemTraderData} is accepted, which excludes gacha machines,
 * slot machines, paygates and command traders without naming each type. The
 * event's product list is interpreted by direction: a sale's product is what
 * left the shop, a purchase's product is what entered it, and a barter's two
 * nested lists are the shop-gave side followed by the shop-received side.
 * Money-only trades are discarded later by {@link TradeDelta}.
 */
public final class TraderTradeEvents {

    private TraderTradeEvents() {
    }

    @SubscribeEvent
    public static void onPostTrade(TradeEvent.PostTradeEvent event) {
        TraderData trader = event.getTrader();
        if (!(trader instanceof ItemTraderData)) {
            return;
        }
        TradeContext context = event.getContext();
        if (context == null || !context.hasPlayer()) {
            return;
        }
        Player rawPlayer = context.getPlayer();
        if (!(rawPlayer instanceof ServerPlayer player)) {
            return;
        }

        TradeDirection direction = event.getTrade().getTradeDirection();
        List<ItemStack> sold = List.of();
        List<ItemStack> bought = List.of();
        Object product = event.getProduct();
        switch (direction) {
            case SALE -> sold = stacks(product);
            case PURCHASE -> bought = stacks(product);
            case BARTER -> {
                if (product instanceof List<?> pair && pair.size() >= 2) {
                    // Lightman builds this pair as [shop-gave, shop-received].
                    sold = stacks(pair.get(0));
                    bought = stacks(pair.get(1));
                }
            }
            default -> {
                return;
            }
        }

        long price = coinCoreValue(event.getPricePaid());
        TradeDelta.Trade record = new TradeDelta.Trade(
                TradeAmounts.of(sold),
                TradeAmounts.of(bought),
                direction == TradeDirection.SALE ? price : 0L,
                direction == TradeDirection.PURCHASE ? price : 0L);

        Optional<ServerQuestFile> optionalFile = ServerQuestFile.getInstance();
        if (optionalFile.isEmpty()) {
            return;
        }
        ServerQuestFile file = optionalFile.get();
        List<CustomerTransactionTask> customerTasks = file.collect(CustomerTransactionTask.class);
        List<MerchantTransactionTask> merchantTasks = file.collect(MerchantTransactionTask.class);
        if (customerTasks.isEmpty() && merchantTasks.isEmpty()) {
            return;
        }

        UUID ownerId = playerOwnerId(trader);
        if (ownerId != null && ownerId.equals(player.getUUID())) {
            // A trade with your own shop never advances your own tasks.
            return;
        }
        boolean playerOwned = ownerId != null;

        creditCustomers(file, customerTasks, record, player, playerOwned);
        creditMerchant(merchantTasks, record, ownerId);
    }

    private static void creditCustomers(ServerQuestFile file, List<CustomerTransactionTask> tasks,
            TradeDelta.Trade record, ServerPlayer player, boolean playerOwned) {
        TeamData data = null;
        for (CustomerTransactionTask task : tasks) {
            if (!task.allowsTrader(playerOwned)) {
                continue;
            }
            long delta = task.delta(record, TradeDelta.Perspective.CUSTOMER);
            if (delta <= 0L) {
                continue;
            }
            if (data == null) {
                data = file.getTeamData(player).orElse(null);
                if (data == null) {
                    return;
                }
            }
            task.submitTrade(data, player, delta);
        }
    }

    private static void creditMerchant(List<MerchantTransactionTask> tasks,
            TradeDelta.Trade record, UUID ownerId) {
        if (ownerId == null) {
            // Server- and team-owned shops have no player owner to credit.
            return;
        }
        for (MerchantTransactionTask task : tasks) {
            long delta = task.delta(record, TradeDelta.Perspective.MERCHANT);
            if (delta > 0L) {
                task.submitOwnerTrade(ownerId, delta);
            }
        }
    }

    private static UUID playerOwnerId(TraderData trader) {
        Owner owner = trader.getOwner().getValidOwner();
        if (owner instanceof PlayerOwner playerOwner) {
            return playerOwner.player.id;
        }
        return null;
    }

    private static List<ItemStack> stacks(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Object element : list) {
            if (element instanceof ItemStack stack && !stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    /**
     * Core value of the price only when it is coin currency: core values are
     * only comparable within one currency type, and barter money is empty.
     */
    private static long coinCoreValue(MoneyValue price) {
        if (price == null || price.isEmpty()) {
            return 0L;
        }
        CurrencyType currency = price.getCurrency();
        if (currency == null || !CoinCurrencyType.TYPE.equals(currency.getType())) {
            return 0L;
        }
        return Math.max(0L, price.getCoreValue());
    }
}
