package io.jaypixl.artipelagocore.events.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EventSavedData extends SavedData {
    private static final Gson GSON = new GsonBuilder().create();
    private final Map<String, ScheduledEvent> events = new LinkedHashMap<>();

    public static EventSavedData create() { return new EventSavedData(); }

    public static EventSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        EventSavedData data = create();
        for (Tag value : tag.getList("events", Tag.TAG_STRING)) {
            try {
                ScheduledEvent event = GSON.fromJson(value.getAsString(), ScheduledEvent.class);
                if (event != null) data.events.put(event.id(), event);
            } catch (RuntimeException ignored) { }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag eventsTag = new ListTag();
        events.values().forEach(event -> eventsTag.add(StringTag.valueOf(GSON.toJson(event))));
        tag.put("events", eventsTag);
        return tag;
    }

    public void add(ScheduledEvent event) { events.put(event.id(), event); setDirty(); }
    public boolean contains(String id) { return events.containsKey(id); }
    public void clear() { if (!events.isEmpty()) { events.clear(); setDirty(); } }
    public boolean remove(String id) { boolean removed = events.remove(id) != null; if (removed) setDirty(); return removed; }
    public int removeByPreset(String presetId) {
        int before = events.size();
        events.values().removeIf(event -> event.recurringId().equals(presetId));
        int removed = before - events.size();
        if (removed > 0) setDirty();
        return removed;
    }
    public Collection<ScheduledEvent> getAll() { return new ArrayList<>(events.values()); }
    public Collection<ScheduledEvent> getActive(long now) { return events.values().stream().filter(event -> event.isActive(now)).toList(); }
    public void removeExpired(long now) { if (events.values().removeIf(event -> event.endTime() <= now)) setDirty(); }

    public static EventSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(EventSavedData::create, EventSavedData::load), "artipelago_events");
    }
}
