package io.jaypixl.artipelagocore.questtasks.fish;

import dev.ftb.mods.ftblibrary.config.BooleanConfig;
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
import io.jaypixl.artipelagocore.questtasks.util.ItemStacks;
import io.jaypixl.artipelagocore.questtasks.util.ResourceIds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Counts fishing catches that contain one configured item or item tag,
 * optionally restricted to one biome or biome tag.
 *
 * <p>Detection lives in {@link FishUpItemEvents}; this class owns the
 * configuration, serialization, matching and the single progress write. Both
 * vanilla fishing and Tide 2 raise the same catch event, so both count
 * identically and no Tide-specific code is needed.
 */
public class FishUpItemTask extends ScopedProgressTask {

    private static final String KEY_AMOUNT = "value";
    private static final String KEY_FILTER = "item_filter";
    private static final String KEY_ITEM = "item";
    private static final String KEY_ITEM_TAG = "item_tag";
    private static final String KEY_FILTER_BIOME = "filter_biome";
    private static final String KEY_BIOME = "biome";

    private static final long DEFAULT_AMOUNT = 100L;
    private static final String DEFAULT_BIOME = "minecraft:plains";

    private long amount = DEFAULT_AMOUNT;
    private ItemFilter.Mode mode = ItemFilter.Mode.ITEM;
    private String item = "";
    private String itemTag = "";
    private boolean filterBiome;
    private String biome = DEFAULT_BIOME;

    private ItemFilter itemFilter = ItemFilter.of(ItemFilter.Mode.ITEM, "");
    private BiomeFilter biomeFilter = BiomeFilter.disabled();

    public FishUpItemTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return QuestTaskTypes.FISH;
    }

    @Override
    public long getMaxProgress() {
        return amount;
    }

    /** The bobber's biome, resolved lazily only when a biome filter needs it. */
    public record BiomeContext(String biomeId, Collection<String> tagIds) {

        public BiomeContext {
            biomeId = biomeId == null ? "" : biomeId;
            tagIds = tagIds == null ? List.of() : List.copyOf(tagIds);
        }
    }

    /**
     * True when the catch contains a matching item and the bobber's biome (if
     * the filter is enabled) matches. A blank item filter accepts every catch;
     * a malformed one never matches.
     */
    public boolean matches(List<ItemStack> drops, Supplier<BiomeContext> biomeContext) {
        if (drops == null || itemFilter.isMalformed()) {
            return false;
        }
        boolean unconstrained = itemFilter.isUnconstrained();
        boolean itemMatched = false;
        for (ItemStack stack : drops) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (unconstrained) {
                itemMatched = true;
                break;
            }
            String id = ItemStacks.itemId(stack);
            Collection<String> tags = itemFilter.mode() == ItemFilter.Mode.TAG
                    ? ItemStacks.itemTagIds(stack)
                    : List.of();
            if (itemFilter.matches(id, tags)) {
                itemMatched = true;
                break;
            }
        }
        if (!itemMatched) {
            return false;
        }
        if (!biomeFilter.isEnabled()) {
            return true;
        }
        BiomeContext context = biomeContext == null ? null : biomeContext.get();
        return context != null && biomeFilter.matches(context.biomeId(), context.tagIds());
    }

    public void submitCatch(TeamData data, ServerPlayer player) {
        submitProgress(data, player, 1L);
    }

    private void refreshFilters() {
        ItemFilter.Mode effective = mode == ItemFilter.Mode.ANY ? ItemFilter.Mode.ITEM : mode;
        itemFilter = ItemFilter.of(effective, effective == ItemFilter.Mode.TAG ? itemTag : item);
        biomeFilter = BiomeFilter.of(filterBiome, biome);
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong(KEY_AMOUNT, amount);
        nbt.putString(KEY_FILTER, mode.id());
        if (!item.isBlank()) {
            nbt.putString(KEY_ITEM, item);
        }
        if (!itemTag.isBlank()) {
            nbt.putString(KEY_ITEM_TAG, itemTag);
        }
        nbt.putBoolean(KEY_FILTER_BIOME, filterBiome);
        if (!biome.isBlank()) {
            nbt.putString(KEY_BIOME, biome);
        }
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        amount = nbt.contains(KEY_AMOUNT) ? Math.max(1L, nbt.getLong(KEY_AMOUNT)) : DEFAULT_AMOUNT;
        mode = normalizeMode(ItemFilter.Mode.byId(nbt.getString(KEY_FILTER)));
        item = nbt.getString(KEY_ITEM);
        itemTag = nbt.getString(KEY_ITEM_TAG);
        filterBiome = nbt.getBoolean(KEY_FILTER_BIOME);
        biome = nbt.contains(KEY_BIOME) ? nbt.getString(KEY_BIOME) : DEFAULT_BIOME;
        refreshFilters();
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(amount);
        buffer.writeEnum(mode);
        buffer.writeUtf(item);
        buffer.writeUtf(itemTag);
        buffer.writeBoolean(filterBiome);
        buffer.writeUtf(biome);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarLong();
        mode = normalizeMode(buffer.readEnum(ItemFilter.Mode.class));
        item = buffer.readUtf();
        itemTag = buffer.readUtf();
        filterBiome = buffer.readBoolean();
        biome = buffer.readUtf();
        refreshFilters();
    }

    /** Fish has no "Any" mode: a corrupt or missing mode fails closed to Item. */
    private static ItemFilter.Mode normalizeMode(ItemFilter.Mode mode) {
        return mode == ItemFilter.Mode.TAG ? ItemFilter.Mode.TAG : ItemFilter.Mode.ITEM;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);

        group.addLong("value", amount, v -> amount = v, DEFAULT_AMOUNT, 1L, Long.MAX_VALUE).setOrder(100);

        NameMap<ItemFilter.Mode> modes = NameMap.of(ItemFilter.Mode.ITEM,
                new ItemFilter.Mode[]{ItemFilter.Mode.ITEM, ItemFilter.Mode.TAG})
                .baseNameKey("ftbquests.task.artipelago.fish.item_filter")
                .create();
        EnumConfig<ItemFilter.Mode> modeConfig = group.addEnum("item_filter", mode, v -> {
            mode = v;
            refreshFilters();
        }, modes, ItemFilter.Mode.ITEM);
        modeConfig.setOrder(101);

        StringConfig itemConfig = group.addString("item", item, v -> {
            item = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        itemConfig.setOrder(102);

        StringConfig tagConfig = group.addString("item_tag", itemTag, v -> {
            itemTag = v;
            refreshFilters();
        }, "", ResourceIds.PATTERN);
        tagConfig.setOrder(103);

        BooleanConfig biomeToggle = group.addBool("filter_biome", filterBiome, v -> {
            filterBiome = v;
            refreshFilters();
        }, false);
        biomeToggle.setOrder(104);

        EnumConfig<String> biomeConfig = addBiomeConfig(group);
        biomeConfig.setOrder(105);

        // FTB's config screen snapshots its fields, so dependent rows stay
        // visible and are dimmed instead of hidden. The predicate reads the
        // dropdown's pending value because the setter only runs on Accept.
        itemConfig.setCanEdit(() -> modeConfig.getValue() != ItemFilter.Mode.TAG);
        tagConfig.setCanEdit(() -> modeConfig.getValue() == ItemFilter.Mode.TAG);
        biomeConfig.setCanEdit(biomeToggle::getValue);
    }

    @OnlyIn(Dist.CLIENT)
    private EnumConfig<String> addBiomeConfig(ConfigGroup group) {
        List<String> known = knownBiomes();
        NameMap<String> map = NameMap.of(biome, known).create();
        return group.addEnum("biome", biome, v -> {
            biome = v;
            refreshFilters();
        }, map, biome);
    }

    /**
     * Every registered biome id plus every biome tag, built fresh for each
     * config screen open. Unlike FTB's own static biome cache this picks up
     * datapack reloads, because the editor is the only consumer.
     */
    @OnlyIn(Dist.CLIENT)
    private List<String> knownBiomes() {
        List<String> list = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Registry<Biome> registry = minecraft.level.registryAccess()
                    .registryOrThrow(Registries.BIOME);
            registry.registryKeySet().stream()
                    .map(ResourceKey::location)
                    .map(Object::toString)
                    .sorted(Comparator.naturalOrder())
                    .forEach(list::add);
            registry.getTagNames()
                    .map(TagKey::location)
                    .map(location -> "#" + location)
                    .sorted(Comparator.naturalOrder())
                    .forEach(list::add);
        }
        if (list.isEmpty()) {
            list.add(DEFAULT_BIOME);
        }
        if (!list.contains(biome)) {
            list.add(biome);
        }
        return list;
    }
}
