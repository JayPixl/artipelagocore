package io.jaypixl.artipelagocore.questtasks.trade;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.EnumConfig;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import io.jaypixl.artipelagocore.questtasks.ScopedProgressTask;
import io.jaypixl.artipelagocore.questtasks.item.ItemFilter;
import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Shared configuration, serialization and counting for the two shop
 * transaction task types.
 *
 * <p>Sold and bought always describe the shop's side. A trade is normalized
 * once by {@link TraderTradeEvents}, then each task answers how much it counts
 * through the pure {@link TradeDelta}.
 */
public abstract class AbstractTransactionTask extends ScopedProgressTask {

    private static final String KEY_AMOUNT = "value";
    private static final String KEY_COUNTS = "counts";
    private static final String KEY_SOLD_FILTER = "sold_filter";
    private static final String KEY_SOLD_ITEM = "sold_item";
    private static final String KEY_SOLD_TAG = "sold_tag";
    private static final String KEY_BOUGHT_FILTER = "bought_filter";
    private static final String KEY_BOUGHT_ITEM = "bought_item";
    private static final String KEY_BOUGHT_TAG = "bought_tag";

    private static final long DEFAULT_AMOUNT = 100L;

    private long amount = DEFAULT_AMOUNT;
    private TransactionCounts counts = TransactionCounts.SOLD_ITEMS;
    private ItemFilter.Mode soldMode = ItemFilter.Mode.ANY;
    private String soldItem = "";
    private String soldTag = "";
    private ItemFilter.Mode boughtMode = ItemFilter.Mode.ANY;
    private String boughtItem = "";
    private String boughtTag = "";

    private ItemFilter soldFilter = ItemFilter.any();
    private ItemFilter boughtFilter = ItemFilter.any();

    protected AbstractTransactionTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public long getMaxProgress() {
        return amount;
    }

    /** How much this task counts for one normalized trade, from its perspective. */
    public long delta(TradeDelta.Record record, TradeDelta.Perspective perspective) {
        return TradeDelta.compute(record, counts, soldFilter, boughtFilter, perspective);
    }

    public void submitTrade(TeamData data, ServerPlayer player, long delta) {
        submitProgress(data, player, delta);
    }

    private void refreshFilters() {
        soldFilter = ItemFilter.of(soldMode, soldMode == ItemFilter.Mode.TAG ? soldTag : soldItem);
        boughtFilter = ItemFilter.of(boughtMode, boughtMode == ItemFilter.Mode.TAG ? boughtTag : boughtItem);
    }

    private String langBase() {
        return "ftbquests.task." + getType().getTypeId().getNamespace()
                + "." + getType().getTypeId().getPath();
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong(KEY_AMOUNT, amount);
        nbt.putString(KEY_COUNTS, counts.id());
        nbt.putString(KEY_SOLD_FILTER, soldMode.id());
        if (!soldItem.isBlank()) {
            nbt.putString(KEY_SOLD_ITEM, soldItem);
        }
        if (!soldTag.isBlank()) {
            nbt.putString(KEY_SOLD_TAG, soldTag);
        }
        nbt.putString(KEY_BOUGHT_FILTER, boughtMode.id());
        if (!boughtItem.isBlank()) {
            nbt.putString(KEY_BOUGHT_ITEM, boughtItem);
        }
        if (!boughtTag.isBlank()) {
            nbt.putString(KEY_BOUGHT_TAG, boughtTag);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        amount = nbt.contains(KEY_AMOUNT) ? Math.max(1L, nbt.getLong(KEY_AMOUNT)) : DEFAULT_AMOUNT;
        counts = TransactionCounts.byId(nbt.getString(KEY_COUNTS));
        soldMode = ItemFilter.Mode.byId(nbt.getString(KEY_SOLD_FILTER));
        soldItem = nbt.getString(KEY_SOLD_ITEM);
        soldTag = nbt.getString(KEY_SOLD_TAG);
        boughtMode = ItemFilter.Mode.byId(nbt.getString(KEY_BOUGHT_FILTER));
        boughtItem = nbt.getString(KEY_BOUGHT_ITEM);
        boughtTag = nbt.getString(KEY_BOUGHT_TAG);
        refreshFilters();
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(amount);
        buffer.writeEnum(counts);
        buffer.writeEnum(soldMode);
        buffer.writeUtf(soldItem);
        buffer.writeUtf(soldTag);
        buffer.writeEnum(boughtMode);
        buffer.writeUtf(boughtItem);
        buffer.writeUtf(boughtTag);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarLong();
        counts = buffer.readEnum(TransactionCounts.class);
        soldMode = buffer.readEnum(ItemFilter.Mode.class);
        soldItem = buffer.readUtf();
        soldTag = buffer.readUtf();
        boughtMode = buffer.readEnum(ItemFilter.Mode.class);
        boughtItem = buffer.readUtf();
        boughtTag = buffer.readUtf();
        refreshFilters();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);

        group.addLong("value", amount, v -> amount = v, DEFAULT_AMOUNT, 1L, Long.MAX_VALUE).setOrder(100);

        NameMap<TransactionCounts> countsMap = NameMap.of(TransactionCounts.SOLD_ITEMS,
                TransactionCounts.values())
                .baseNameKey(langBase() + ".counts")
                .create();
        EnumConfig<TransactionCounts> countsConfig = group.addEnum("counts", counts, v -> counts = v,
                countsMap, TransactionCounts.SOLD_ITEMS);
        countsConfig.setOrder(101);

        NameMap<ItemFilter.Mode> soldModes = NameMap.of(ItemFilter.Mode.ANY,
                new ItemFilter.Mode[]{ItemFilter.Mode.ANY, ItemFilter.Mode.ITEM, ItemFilter.Mode.TAG})
                .baseNameKey(langBase() + ".sold_filter")
                .create();
        EnumConfig<ItemFilter.Mode> soldModeConfig = group.addEnum("sold_filter", soldMode, v -> {
            soldMode = v;
            refreshFilters();
        }, soldModes, ItemFilter.Mode.ANY);
        soldModeConfig.setOrder(102);

        StringConfig soldItemConfig = group.addString("sold_item", soldItem, v -> {
            soldItem = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        soldItemConfig.setOrder(103);

        StringConfig soldTagConfig = group.addString("sold_tag", soldTag, v -> {
            soldTag = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        soldTagConfig.setOrder(104);

        NameMap<ItemFilter.Mode> boughtModes = NameMap.of(ItemFilter.Mode.ANY,
                new ItemFilter.Mode[]{ItemFilter.Mode.ANY, ItemFilter.Mode.ITEM, ItemFilter.Mode.TAG})
                .baseNameKey(langBase() + ".bought_filter")
                .create();
        EnumConfig<ItemFilter.Mode> boughtModeConfig = group.addEnum("bought_filter", boughtMode, v -> {
            boughtMode = v;
            refreshFilters();
        }, boughtModes, ItemFilter.Mode.ANY);
        boughtModeConfig.setOrder(105);

        StringConfig boughtItemConfig = group.addString("bought_item", boughtItem, v -> {
            boughtItem = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        boughtItemConfig.setOrder(106);

        StringConfig boughtTagConfig = group.addString("bought_tag", boughtTag, v -> {
            boughtTag = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        boughtTagConfig.setOrder(107);

        // Dependent rows stay visible and are dimmed, reading the dropdown's
        // pending value because the setter only runs on Accept.
        soldItemConfig.setCanEdit(() -> soldModeConfig.getValue() != ItemFilter.Mode.TAG);
        soldTagConfig.setCanEdit(() -> soldModeConfig.getValue() == ItemFilter.Mode.TAG);
        boughtItemConfig.setCanEdit(() -> boughtModeConfig.getValue() != ItemFilter.Mode.TAG);
        boughtTagConfig.setCanEdit(() -> boughtModeConfig.getValue() == ItemFilter.Mode.TAG);
    }
}
