package io.jaypixl.artipelagocore.events.runtime;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.spawning.BestSpawner;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.SpawnLoader;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.events.effect.EventEffect;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Rebuilds temporary world-spawn changes from Cobblemon's unmodified state. */
public final class SpawnEffectOverlay {
    private static final Map<SpawnDetail, Float> BASE_WEIGHTS = new IdentityHashMap<>();
    private static final Map<String, Float> BASE_BUCKET_WEIGHTS = new HashMap<>();
    private static final List<SpawnDetail> INJECTED_SPAWNS = new ArrayList<>();

    private SpawnEffectOverlay() { }

    public static void refresh(MinecraftServer server) {
        SpawnPool pool = CobblemonSpawnPools.INSTANCE.getWORLD_SPAWN_POOL();
        restore(pool);
        snapshot(pool);

        for (EventEffect effect : EventManager.getActiveEffects(server, "multiply_spawn_weight")) {
            if (effect.multiplier == null) continue;
            for (SpawnDetail detail : BASE_WEIGHTS.keySet()) {
                if (matches(effect, detail)) detail.setWeight(detail.getWeight() * effect.multiplier.floatValue());
            }
        }
        for (EventEffect effect : EventManager.getActiveEffects(server, "modify_global_bucket_weights")) {
            for (EventEffect.BucketAssignment assignment : effect.buckets) {
                if (BASE_BUCKET_WEIGHTS.containsKey(assignment.bucket)) {
                    BestSpawner.INSTANCE.getConfig().getWorldBuckets().put(assignment.bucket, (float) assignment.weight);
                }
            }
        }
        for (EventEffect effect : EventManager.getActiveEffects(server, "add_spawns")) {
            for (EventEffect.Spawn spawn : effect.spawns) addSpawn(server, pool, spawn);
        }
        if (!INJECTED_SPAWNS.isEmpty()) pool.precalculate();
    }

    public static void clear() {
        try { restore(CobblemonSpawnPools.INSTANCE.getWORLD_SPAWN_POOL()); }
        catch (IllegalStateException ignored) { }
    }

    private static void restore(SpawnPool pool) {
        BASE_WEIGHTS.forEach(SpawnDetail::setWeight);
        BestSpawner.INSTANCE.getConfig().getWorldBuckets().putAll(BASE_BUCKET_WEIGHTS);
        pool.getDetails().removeAll(INJECTED_SPAWNS);
        if (!INJECTED_SPAWNS.isEmpty()) pool.precalculate();
        BASE_WEIGHTS.clear();
        BASE_BUCKET_WEIGHTS.clear();
        INJECTED_SPAWNS.clear();
    }

    private static void snapshot(SpawnPool pool) {
        for (SpawnDetail detail : pool.getDetails()) BASE_WEIGHTS.put(detail, detail.getWeight());
        BASE_BUCKET_WEIGHTS.putAll(BestSpawner.INSTANCE.getConfig().getWorldBuckets());
    }

    private static boolean matches(EventEffect effect, SpawnDetail detail) {
        if (!(detail instanceof PokemonSpawnDetail pokemonDetail)) return false;
        PokemonProperties pokemon = pokemonDetail.getPokemon();
        for (EventEffect.PokemonTarget target : effect.targets) {
            boolean speciesMatches = "all".equals(target.species) || target.species.equals(pokemon.getSpecies());
            boolean aspectsMatch = target.aspects == null || target.aspects.isEmpty()
                    || pokemon.getAspects().containsAll(target.aspects);
            if (speciesMatches && aspectsMatch) return true;
        }
        return false;
    }

    private static void addSpawn(MinecraftServer server, SpawnPool pool, EventEffect.Spawn spawn) {
        if (spawn.id == null || spawn.id.isBlank() || pool.getDetails().stream().anyMatch(detail -> spawn.id.equals(detail.getId()))) {
            ArtipelagoCoreMod.LOGGER.warn("Skipping event spawn with duplicate or blank id '{}'", spawn.id);
            return;
        }
        try {
            SpawnDetail detail = SpawnLoader.INSTANCE.getGson().fromJson(toNativeJson(spawn), SpawnDetail.class);
            if (!detail.isValid()) {
                ArtipelagoCoreMod.LOGGER.warn("Skipping invalid event spawn '{}'", spawn.id);
                return;
            }
            detail.onServerLoad(server);
            pool.getDetails().add(detail);
            INJECTED_SPAWNS.add(detail);
        } catch (RuntimeException exception) {
            ArtipelagoCoreMod.LOGGER.error("Could not add event spawn '{}'", spawn.id, exception);
        }
    }

    private static JsonObject toNativeJson(EventEffect.Spawn spawn) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "pokemon");
        json.addProperty("id", spawn.id);
        json.addProperty("pokemon", spawn.pokemon);
        json.addProperty("context", spawn.context);
        json.addProperty("bucket", spawn.bucket);
        json.addProperty("weight", spawn.weight);
        if (spawn.level != null) json.addProperty("level", spawn.level);
        if (spawn.condition != null) json.add("condition", spawn.condition.deepCopy());
        if (spawn.anticondition != null) json.add("anticondition", spawn.anticondition.deepCopy());
        if (spawn.aspects != null && !spawn.aspects.isEmpty()) json.add("aspects", stringArray(spawn.aspects));
        if (spawn.forms != null && !spawn.forms.isEmpty()) json.add("forms", stringArray(spawn.forms));
        if (spawn.heldItems != null && !spawn.heldItems.isEmpty()) {
            JsonArray items = new JsonArray();
            for (EventEffect.HeldItem item : spawn.heldItems) {
                JsonObject entry = new JsonObject();
                entry.addProperty("item", item.item);
                entry.addProperty("percentage", item.percentage);
                items.add(entry);
            }
            json.add("heldItems", items);
        }
        return json;
    }

    private static JsonArray stringArray(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }
}
