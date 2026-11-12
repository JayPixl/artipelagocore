package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftblibrary.config.BooleanConfig;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.jaypixl.artipelagocore.questtasks.QuestTaskTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Credits the customer for trading at a shop.
 *
 * <p>Sold and bought follow the shop's side, so buying from a player shop
 * advances Sold items and selling to it advances Bought items. Coins are
 * counted from the customer's point of view by {@link TradeDelta.Perspective}.
 * A trade the customer makes with their own shop never counts, and
 * server-owned (non-player-owned) shops can be excluded with
 * {@code allow_server_owned}.
 */
public class CustomerTransactionTask extends AbstractTransactionTask {

    private static final String KEY_ALLOW_SERVER_OWNED = "allow_server_owned";

    private boolean allowServerOwned = true;

    public CustomerTransactionTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return QuestTaskTypes.CUSTOMER_TRANSACTION;
    }

    public boolean allowServerOwned() {
        return allowServerOwned;
    }

    /**
     * Whether a trade at this kind of trader counts. Only server-owned
     * (admin) shops are governed by the checkbox; player- and team-owned shops
     * always count for the customer.
     */
    public boolean allowsTrader(TraderOwnership ownership) {
        return ownership.countsForCustomer(allowServerOwned);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putBoolean(KEY_ALLOW_SERVER_OWNED, allowServerOwned);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        allowServerOwned = !nbt.contains(KEY_ALLOW_SERVER_OWNED)
                || nbt.getBoolean(KEY_ALLOW_SERVER_OWNED);
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeBoolean(allowServerOwned);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        allowServerOwned = buffer.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);
        BooleanConfig allowConfig = group.addBool("allow_server_owned", allowServerOwned,
                v -> allowServerOwned = v, true);
        allowConfig.setOrder(108);
    }
}
