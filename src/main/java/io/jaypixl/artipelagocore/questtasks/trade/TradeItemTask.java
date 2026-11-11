package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.EnumConfig;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.jaypixl.artipelagocore.questtasks.QuestTaskTypes;
import io.jaypixl.artipelagocore.questtasks.ScopedProgressTask;
import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;
import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Counts items moved between two players through {@code /lctrade}, from each
 * participant's own perspective.
 *
 * <p>Detection lives in the {@code PlayerTrade} mixin; this class owns the
 * configuration, serialization, matching and the single progress write. Money
 * moved by the trade is deliberately ignored.
 */
public class TradeItemTask extends ScopedProgressTask {

    private static final String KEY_AMOUNT = "value";
    private static final String KEY_COUNTS = "counts";
    private static final String KEY_FILTER = "item_filter";
    private static final String KEY_ITEM = "item";
    private static final String KEY_ITEM_TAG = "item_tag";

    private static final long DEFAULT_AMOUNT = 100L;

    private long amount = DEFAULT_AMOUNT;
    private TradeItemCounts counts = TradeItemCounts.TRADE_AWAY;
    private ItemFilter.Mode mode = ItemFilter.Mode.ANY;
    private String item = "";
    private String itemTag = "";

    private ItemFilter filter = ItemFilter.any();

    public TradeItemTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return QuestTaskTypes.TRADE_ITEM;
    }

    @Override
    public long getMaxProgress() {
        return amount;
    }

    /**
     * @param tradedAway what this participant put into the trade
     * @param tradedFor  what this participant received
     */
    public long delta(List<TradeDelta.ItemAmount> tradedAway, List<TradeDelta.ItemAmount> tradedFor) {
        return TradeItemDelta.compute(tradedAway, tradedFor, counts, filter);
    }

    public void submitTrade(TeamData data, ServerPlayer player, long delta) {
        submitProgress(data, player, delta);
    }

    private void refreshFilter() {
        filter = ItemFilter.of(mode, mode == ItemFilter.Mode.TAG ? itemTag : item);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong(KEY_AMOUNT, amount);
        nbt.putString(KEY_COUNTS, counts.id());
        nbt.putString(KEY_FILTER, mode.id());
        if (!item.isBlank()) {
            nbt.putString(KEY_ITEM, item);
        }
        if (!itemTag.isBlank()) {
            nbt.putString(KEY_ITEM_TAG, itemTag);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        amount = nbt.contains(KEY_AMOUNT) ? Math.max(1L, nbt.getLong(KEY_AMOUNT)) : DEFAULT_AMOUNT;
        counts = TradeItemCounts.byId(nbt.getString(KEY_COUNTS));
        mode = ItemFilter.Mode.byId(nbt.getString(KEY_FILTER));
        item = nbt.getString(KEY_ITEM);
        itemTag = nbt.getString(KEY_ITEM_TAG);
        refreshFilter();
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(amount);
        buffer.writeEnum(counts);
        buffer.writeEnum(mode);
        buffer.writeUtf(item);
        buffer.writeUtf(itemTag);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarLong();
        counts = buffer.readEnum(TradeItemCounts.class);
        mode = buffer.readEnum(ItemFilter.Mode.class);
        item = buffer.readUtf();
        itemTag = buffer.readUtf();
        refreshFilter();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);

        group.addLong("value", amount, v -> amount = v, DEFAULT_AMOUNT, 1L, Long.MAX_VALUE).setOrder(100);

        NameMap<TradeItemCounts> countsMap = NameMap.of(TradeItemCounts.TRADE_AWAY,
                TradeItemCounts.values())
                .baseNameKey("ftbquests.task.artipelago.trade_item.counts")
                .create();
        EnumConfig<TradeItemCounts> countsConfig = group.addEnum("counts", counts, v -> counts = v,
                countsMap, TradeItemCounts.TRADE_AWAY);
        countsConfig.setOrder(101);

        NameMap<ItemFilter.Mode> modes = NameMap.of(ItemFilter.Mode.ANY,
                new ItemFilter.Mode[]{ItemFilter.Mode.ANY, ItemFilter.Mode.ITEM, ItemFilter.Mode.TAG})
                .baseNameKey("ftbquests.task.artipelago.trade_item.item_filter")
                .create();
        EnumConfig<ItemFilter.Mode> modeConfig = group.addEnum("item_filter", mode, v -> {
            mode = v;
            refreshFilter();
        }, modes, ItemFilter.Mode.ANY);
        modeConfig.setOrder(102);

        StringConfig itemConfig = group.addString("item", item, v -> {
            item = v;
            refreshFilter();
        }, "", ResourceIds.PATTERN);
        itemConfig.setOrder(103);

        StringConfig tagConfig = group.addString("item_tag", itemTag, v -> {
            itemTag = v;
            refreshFilter();
        }, "", ResourceIds.PATTERN);
        tagConfig.setOrder(104);

        itemConfig.setCanEdit(() -> modeConfig.getValue() != ItemFilter.Mode.TAG);
        tagConfig.setCanEdit(() -> modeConfig.getValue() == ItemFilter.Mode.TAG);
    }
}
