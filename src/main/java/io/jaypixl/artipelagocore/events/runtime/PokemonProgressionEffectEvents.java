package io.jaypixl.artipelagocore.events.runtime;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.EvGainedEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;
import com.cobblemon.mod.common.api.pokemon.experience.BattleExperienceSource;
import com.cobblemon.mod.common.api.pokemon.stats.BattleEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.ItemEvSource;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.events.effect.EventEffect;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Applies event multipliers before Cobblemon awards Pokémon XP and EVs. */
public final class PokemonProgressionEffectEvents {
    private static final Gson GSON = new Gson();
    private PokemonProgressionEffectEvents() { }

    public static void register() {
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(PokemonProgressionEffectEvents::onExperienceGained);
        CobblemonEvents.EV_GAINED_EVENT_PRE.subscribe(PokemonProgressionEffectEvents::onEvGained);
    }

    private static void onExperienceGained(ExperienceGainedEvent.Pre event) {
        if (ServerLifecycleHooks.getCurrentServer() == null || event.getExperience() <= 0) return;
        double multiplier = 1.0;
        for (EventEffect effect : EventManager.getActiveEffects(ServerLifecycleHooks.getCurrentServer(), "pokemon_xp_boost")) {
            if (effect.multiplier != null && matchesExperienceContext(effect.contexts, event.getSource())) multiplier *= effect.multiplier;
        }
        event.setExperience(scale(event.getExperience(), multiplier));
    }

    private static void onEvGained(EvGainedEvent.Pre event) {
        if (ServerLifecycleHooks.getCurrentServer() == null || event.getAmount() <= 0) return;
        double multiplier = 1.0;
        for (EventEffect effect : EventManager.getActiveEffects(ServerLifecycleHooks.getCurrentServer(), "pokemon_ev_boost")) {
            if (effect.multiplier != null && matchesEvContext(effect.contexts, event.getSource()) && roll(effect.chance)) {
                multiplier *= effect.multiplier;
            }
        }
        event.setAmount(scale(event.getAmount(), multiplier));
    }

    private static boolean matchesExperienceContext(JsonElement contexts, Object source) {
        return matchesContext(contexts, source instanceof BattleExperienceSource battle ? battle.getFacedPokemon() : null,
                source instanceof BattleExperienceSource, false);
    }

    private static boolean matchesEvContext(JsonElement contexts, Object source) {
        return matchesContext(contexts, source instanceof BattleEvSource battle ? battle.getFacedPokemon() : null,
                source instanceof BattleEvSource, source instanceof ItemEvSource);
    }

    private static boolean matchesContext(JsonElement contexts, List<BattlePokemon> facedPokemon, boolean battle, boolean vitamin) {
        if (contexts == null || !contexts.isJsonArray()) return false;
        for (JsonElement element : contexts.getAsJsonArray()) {
            if (!element.isJsonObject()) continue;
            JsonObject context = element.getAsJsonObject();
            String type = context.has("type") ? context.get("type").getAsString() : "";
            if ("all".equals(type)) return true;
            if ("vitamin".equals(type) && vitamin) return true;
            if ("wild_battle".equals(type) && battle && matchesAny(context.get("pokemon"), facedPokemon)) return true;
        }
        return false;
    }

    private static boolean matchesAny(JsonElement targets, List<BattlePokemon> facedPokemon) {
        if (targets == null || !targets.isJsonArray() || facedPokemon == null) return false;
        for (JsonElement targetElement : targets.getAsJsonArray()) {
            EventEffect.PokemonTarget target = GSON.fromJson(targetElement, EventEffect.PokemonTarget.class);
            if (target == null) continue;
            for (BattlePokemon battlePokemon : facedPokemon) {
                if (matches(target, battlePokemon.getEffectedPokemon())) return true;
            }
        }
        return false;
    }

    private static boolean matches(EventEffect.PokemonTarget target, Pokemon pokemon) {
        String speciesId = pokemon.getSpecies().getResourceIdentifier().toString();
        return ("all".equals(target.species) || speciesId.equals(target.species))
                && (target.aspects == null || target.aspects.isEmpty() || pokemon.getAspects().containsAll(target.aspects));
    }

    private static boolean roll(Double chance) {
        return chance != null && chance > 0.0 && ThreadLocalRandom.current().nextDouble() < Math.min(chance, 1.0);
    }

    private static int scale(int amount, double multiplier) {
        return (int) Math.clamp(Math.round(amount * multiplier), 0L, Integer.MAX_VALUE);
    }
}
