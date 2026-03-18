package io.jaypixl.artipelagocore.regionmarket;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.z0rdak.yawp.api.core.IDimensionRegionApi;
import de.z0rdak.yawp.api.core.RegionManager;
import de.z0rdak.yawp.api.permission.Permissions;
import de.z0rdak.yawp.core.region.IMarkableRegion;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RegionMarketCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("regionmarket")
                        .then(Commands.literal("add")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("regionId", StringArgumentType.word())
                                        .then(Commands.argument("cost", IntegerArgumentType.integer(0))
                                                .executes(RegionMarketCommands::addCommand)
                                        )
                                        .then(Commands.literal("starter")
                                                .executes(RegionMarketCommands::addStarterCommand)
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("regionId", StringArgumentType.word())
                                        .executes(RegionMarketCommands::removeCommand)
                                )
                        )
                        .then(Commands.literal("list")
                                .requires(source -> source.hasPermission(2))
                                .executes(RegionMarketCommands::listCommand)
                        )
                        .then(Commands.literal("claimstarterhousing")
                                .executes(RegionMarketCommands::claimStarterHousingCommand)
                        )
                        .then(Commands.literal("buy")
                                .then(Commands.argument("regionId", StringArgumentType.word())
                                    .executes(RegionMarketCommands::buyCommand)
                                )
                        )
                        .then(Commands.literal("info")
                                .executes(RegionMarketCommands::infoCommand)
                        )
        );
    }

    private static int addCommand(CommandContext<CommandSourceStack> ctx) {
        String regionId = StringArgumentType.getString(ctx, "regionId");
        int cost = IntegerArgumentType.getInteger(ctx, "cost");

        ServerLevel level = ctx.getSource().getLevel();
        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        Optional<IDimensionRegionApi> optionalIDimensionRegionApi = RegionManager.get().getDimRegionApi(level.dimension());

        if (optionalIDimensionRegionApi.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting dimensional region for " + level.dimension()));
            return 0;
        }

        IDimensionRegionApi regionApi = optionalIDimensionRegionApi.get();

        if (!regionApi.hasLocal(regionId)) {
            ctx.getSource().sendFailure(Component.literal("Region with id " + regionId + " does not exist!"));
            return 0;
        }

        data.addListing(regionId, cost, "", false);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Added listing: " + regionId + " for $" + cost),
                true
        );

        return 1;
    }

    private static int addStarterCommand(CommandContext<CommandSourceStack> ctx) {
        String regionId = StringArgumentType.getString(ctx, "regionId");

        ServerLevel level = ctx.getSource().getLevel();

        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        Optional<IDimensionRegionApi> optionalIDimensionRegionApi = RegionManager.get().getDimRegionApi(level.dimension());

        if (optionalIDimensionRegionApi.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting dimensional region for " + level.dimension()));
            return 0;
        }

        IDimensionRegionApi regionApi = optionalIDimensionRegionApi.get();

        if (!regionApi.hasLocal(regionId)) {
            ctx.getSource().sendFailure(Component.literal("Region with id " + regionId + " does not exist!"));
            return 0;
        }

        data.addListing(regionId, 0, "", true);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Added starter listing: " + regionId),
                true
        );

        return 1;
    }

    private static int removeCommand(CommandContext<CommandSourceStack> ctx) {
        String regionId = StringArgumentType.getString(ctx, "regionId");

        ServerLevel level = ctx.getSource().getLevel();
        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        data.removeListing(regionId);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Removed listing: " + regionId),
                true
        );

        return 1;
    }

    private static int listCommand(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        if (data.getEntries().isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("No regions are currently listed."),
                    false
            );
            return 1;
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Region Market Listings:"),
                false
        );

        for (RegionMarketEntry entry : data.getEntries().values()) {

            ctx.getSource().sendSuccess(
                    () -> Component.literal(
                            entry.getId()
                                    + " | Cost: $" + entry.getCost()
                                    + " | Owner: " + entry.getOwner()
                                    + " | Starter?: " + entry.getIsStarter()
                    ),
                    false
            );
        }

        return 1;
    }

    private static int claimStarterHousingCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();

        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        // find first starter listing
        RegionMarketEntry starterEntry = null;

        for (RegionMarketEntry entry : data.getEntries().values()) {
            if (entry.getIsStarter() && Objects.equals(entry.getOwner(), "")) {
                starterEntry = entry;
                break;
            }
        }

        if (starterEntry == null) {
            player.sendSystemMessage(Component.literal("No starter housing available."));
            return 0;
        }

        String regionId = starterEntry.getId();

        Optional<IDimensionRegionApi> optionalIDimensionRegionApi = RegionManager.get().getDimRegionApi(level.dimension());

        if (optionalIDimensionRegionApi.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting dimensional region for " + level.dimension()));
            return 0;
        }

        IDimensionRegionApi regionApi = optionalIDimensionRegionApi.get();

        Optional<IMarkableRegion> optionalRegion = regionApi.getLocalRegion(regionId);

        if (optionalRegion.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting local region " + regionId));
            return 0;
        }

        IMarkableRegion region = optionalRegion.get();

        region.addPlayer(player, Permissions.MEMBER);

        if (!region.hasPlayer(player.getUUID(), Permissions.MEMBER)) {
            ctx.getSource().sendFailure(Component.literal("Error adding player to region " + regionId));
            return 0;
        }

        data.setOwner(regionId, player.getUUID().toString());

        player.sendSystemMessage(Component.literal(
                "Starter housing claimed: " + regionId
        ));

        return 1;
    }

    private static int buyCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();

        String regionId = StringArgumentType.getString(ctx, "regionId");

        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        RegionMarketEntry regionEntry = null;

        for (RegionMarketEntry entry : data.getEntries().values()) {
            if (Objects.equals(entry.getId(), regionId)) {
                regionEntry = entry;
                break;
            }
        }

        if (regionEntry == null) {
            player.sendSystemMessage(Component.literal("Could not find region " + regionId));
            return 0;
        }

        if (regionEntry.getIsStarter()) {
            player.sendSystemMessage(Component.literal("Region is not for sale!"));
            return 0;
        }

        if (!Objects.equals(regionEntry.getOwner(), "")) { // TODO: Change to check for current owner or inhabitants
            player.sendSystemMessage(Component.literal("This region is already owned by " + level.getPlayerByUUID(UUID.fromString(regionEntry.getOwner())).getDisplayName()));
            return 0;
        }

        Optional<IDimensionRegionApi> optionalIDimensionRegionApi = RegionManager.get().getDimRegionApi(level.dimension());

        if (optionalIDimensionRegionApi.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting dimensional region for " + level.dimension()));
            return 0;
        }

        IDimensionRegionApi regionApi = optionalIDimensionRegionApi.get();

        Optional<IMarkableRegion> optionalRegion = regionApi.getLocalRegion(regionId);

        if (optionalRegion.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting local region " + regionId));
            return 0;
        }

        IMarkableRegion region = optionalRegion.get();

        MoneyValue takeAmount = CoinValue.fromNumber(CoinAPI.MAIN_CHAIN, regionEntry.getCost());

        BankReference br = PlayerBankReference.of(player);
        IBankAccount account = br.get();

        if (account == null) {
            ctx.getSource().sendFailure(Component.literal("Could not get player's bank account!"));
            return 0;
        }

        if (!account.extractMoney(takeAmount,true).isEmpty()) {
            // Does not have enough money in account
            ctx.getSource().sendFailure(Component.literal("You cannot afford this region"));
            return 0;
        }

        account.extractMoney(takeAmount,false);
        account.pushLocalNotification(new DepositWithdrawNotification.Custom(Component.literal("Region Market"),account.getName(),false, takeAmount));

        region.addPlayer(player, Permissions.OWNER);

        ArtipelagoCoreMod.LOGGER.info(player.getUUID().toString());

        if (!region.hasPlayer(player.getUUID(), Permissions.OWNER)) {
            ctx.getSource().sendFailure(Component.literal("Error adding player to region " + regionId));
            return 0;
        }

        data.setOwner(regionId, player.getStringUUID());

        player.sendSystemMessage(Component.literal(
                "Region purchase completed: " + regionId
        ));

        return 1;
    }

    private static int infoCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();

        RegionMarketSavedData data = RegionMarketSavedData.get(level);

        Optional<IDimensionRegionApi> optionalIDimensionRegionApi = RegionManager.get().getDimRegionApi(level.dimension());

        if (optionalIDimensionRegionApi.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Error getting dimensional region for " + level.dimension()));
            return 0;
        }

        IDimensionRegionApi regionApi = optionalIDimensionRegionApi.get();

        List<IMarkableRegion> regionList = regionApi.getRegionsAt(player.blockPosition());

        IMarkableRegion markableRegion = null;

        for (IMarkableRegion region : regionList) {
            if (data.getEntries().containsKey(region.getName())) {
                markableRegion = region;
                break;
            }
        }

        if (markableRegion == null) {
            ctx.getSource().sendSystemMessage(Component.literal("No region available here!"));
            return 0;
        }

        RegionMarketEntry listing = data.getListing(markableRegion.getName());

        if (listing == null) {
            ctx.getSource().sendSystemMessage(Component.literal("No region available here!"));
            return 0;
        }

        player.sendSystemMessage(Component.literal(
                "ID: " + listing.getId() +
                    (listing.getIsStarter() ? " | Starter Housing" : "") +
                        (Objects.equals(listing.getOwner(), "") ?
                                " | Cost: $" + listing.getCost() :
                                " | Owned by: " + level.getPlayerByUUID(UUID.fromString(listing.getOwner())).getName().getString())
        ));

        return 1;
    }
}