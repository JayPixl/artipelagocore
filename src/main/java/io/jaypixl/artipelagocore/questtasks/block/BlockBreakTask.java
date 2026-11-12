package io.jaypixl.artipelagocore.questtasks.block;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.EnumConfig;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import io.jaypixl.artipelagocore.questtasks.QuestTaskTypes;
import io.jaypixl.artipelagocore.questtasks.ScopedProgressTask;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Counts player-broken blocks that match one block id or one block tag.
 *
 * <p>Detection lives in {@link BlockBreakEvents}; this class owns the
 * configuration, its serialization, whether a break matches, and the single
 * progress write. The write goes through the Tracking Scope router rather than
 * straight to the store FTB handed us, because FTB's submit reroute only
 * targets its own task classes.
 */
public class BlockBreakTask extends ScopedProgressTask {

    private static final String KEY_AMOUNT = "value";
    private static final String KEY_FILTER = "block_filter";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_BLOCK_TAG = "block_tag";

    private static final long DEFAULT_AMOUNT = 100L;

    private long amount = DEFAULT_AMOUNT;
    private BlockFilter.Mode mode = BlockFilter.Mode.BLOCK;
    private String block = "";
    private String blockTag = "";

    /**
     * Normalized view of the active side, refreshed whenever configuration or
     * NBT changes. Kept because matching runs on every block break; rebuilding
     * it there would re-run id validation once per task per break.
     */
    private BlockFilter filter = BlockFilter.none();

    public BlockBreakTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return QuestTaskTypes.BLOCK_BREAK;
    }

    @Override
    public long getMaxProgress() {
        return amount;
    }

    public boolean matches(String blockId, Supplier<Collection<String>> blockTags) {
        Collection<String> tags = filter.mode() == BlockFilter.Mode.TAG && !filter.isEmpty()
                ? blockTags.get()
                : List.of();
        return filter.matches(blockId, tags);
    }

    private void refreshFilter() {
        filter = BlockFilter.of(mode, mode == BlockFilter.Mode.TAG ? blockTag : block);
    }

    @Override
    public void submitTask(TeamData data, ServerPlayer player, ItemStack stack) {
        submitProgress(data, player, 1L);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong(KEY_AMOUNT, amount);
        nbt.putString(KEY_FILTER, mode.id());
        if (!block.isBlank()) {
            nbt.putString(KEY_BLOCK, block);
        }
        if (!blockTag.isBlank()) {
            nbt.putString(KEY_BLOCK_TAG, blockTag);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        amount = nbt.contains(KEY_AMOUNT) ? Math.max(1L, nbt.getLong(KEY_AMOUNT)) : DEFAULT_AMOUNT;
        mode = BlockFilter.Mode.byId(nbt.getString(KEY_FILTER));
        block = nbt.getString(KEY_BLOCK);
        blockTag = nbt.getString(KEY_BLOCK_TAG);
        refreshFilter();
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(amount);
        buffer.writeEnum(mode);
        buffer.writeUtf(block);
        buffer.writeUtf(blockTag);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarLong();
        mode = buffer.readEnum(BlockFilter.Mode.class);
        block = buffer.readUtf();
        blockTag = buffer.readUtf();
        refreshFilter();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);

        group.addLong("value", amount, v -> amount = v, DEFAULT_AMOUNT, 1L, Long.MAX_VALUE).setOrder(100);

        EnumConfig<BlockFilter.Mode> modeConfig = group.addEnum("block_filter", mode, v -> {
            mode = v;
            refreshFilter();
        }, NameMap.of(BlockFilter.Mode.BLOCK, BlockFilter.Mode.values())
                .baseNameKey("ftbquests.task.artipelago.block_break.block_filter")
                .create(), BlockFilter.Mode.BLOCK);
        modeConfig.setOrder(101);

        StringConfig blockConfig = group.addString("block", block, v -> {
            block = v;
            refreshFilter();
        }, "", BlockFilter.CONFIG_PATTERN);
        blockConfig.setOrder(102);

        StringConfig tagConfig = group.addString("block_tag", blockTag, v -> {
            blockTag = v;
            refreshFilter();
        }, "", BlockFilter.CONFIG_PATTERN);
        tagConfig.setOrder(103);

        // FTB's config screen snapshots its fields, so fields are dimmed
        // instead of hidden. The predicate must read the dropdown's pending
        // value, not our own field: the setter that assigns `mode` only runs on
        // Accept (ConfigGroup.save -> applyValue), while the dropdown updates
        // the pending value immediately. Reading the field instead would leave
        // the newly selected field dimmed until the screen was reopened.
        blockConfig.setCanEdit(() -> modeConfig.getValue() != BlockFilter.Mode.TAG);
        tagConfig.setCanEdit(() -> modeConfig.getValue() == BlockFilter.Mode.TAG);
    }
}
