package io.jaypixl.artipelagocore.regionmarket;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class RegionMarketSavedData extends SavedData {

    private final Map<String, RegionMarketEntry> entries = new HashMap<>();

    public Map<String, RegionMarketEntry> getEntries() {
        return entries;
    }

    public static RegionMarketSavedData create() {
        return new RegionMarketSavedData();
    }

    public static RegionMarketSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {

        RegionMarketSavedData data = RegionMarketSavedData.create();

        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag entryTag = (CompoundTag) t;

            String id = entryTag.getString("id");
            int cost = entryTag.getInt("cost");
            String owner = entryTag.getString("owner");
            boolean isStarter = entryTag.getBoolean("isStarter");

            RegionMarketEntry entry = new RegionMarketEntry(id, cost, owner, isStarter);

            data.entries.put(id, entry);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag list = new ListTag();

        for (RegionMarketEntry entry : entries.values()) {

            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("id", entry.getId());
            entryTag.putInt("cost", entry.getCost());
            entryTag.putString("owner", entry.getOwner());
            entryTag.putBoolean("isStarter", entry.getIsStarter());

            list.add(entryTag);
        }

        tag.put("entries", list);

        return tag;
    }

    public void setOwner(String id, String owner) {
        RegionMarketEntry entry = getListing(id);
        entries.replace(
                id,
                new RegionMarketEntry(id, entry.getCost(), owner, entry.getIsStarter())
        );
        this.setDirty();
    }

    public void addListing(String id, int cost, String owner, boolean isStarter) {

        entries.put(id, new RegionMarketEntry(id, cost, owner, isStarter));

        this.setDirty();
    }

    public void removeListing(String id) {

        entries.remove(id);

        this.setDirty();
    }

    public RegionMarketEntry getListing(String id) {
        return entries.get(id);
    }

    public static RegionMarketSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(RegionMarketSavedData::create,
                RegionMarketSavedData::load),
                "region_market"
        );
    }
}