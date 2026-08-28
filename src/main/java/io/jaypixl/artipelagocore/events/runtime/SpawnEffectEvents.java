package io.jaypixl.artipelagocore.events.runtime;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import io.jaypixl.artipelagocore.events.effect.EventEffect;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Applies event effects while Cobblemon generates wild Pokémon. */
public final class SpawnEffectEvents {
    private static final float DEFAULT_SHINY_COEFFICIENT = 8192.0F;

    private SpawnEffectEvents() { }

    public static void register() {
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(SpawnEffectEvents::applyShinyBoosts);
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(SpawnEffectEvents::applySpawnEffects);
    }

    private static void applyShinyBoosts(ShinyChanceCalculationEvent event) {
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        for (EventEffect effect : EventManager.getActiveEffects(ServerLifecycleHooks.getCurrentServer(), "shiny_boost")) {
            if (!matches(effect, event.getPokemon())) continue;
            float coefficient = effect.value == null ? DEFAULT_SHINY_COEFFICIENT : effect.value.floatValue();
            if (coefficient <= 0.0F) continue;
            if ("set".equals(effect.operation)) {
                event.addModificationFunction((current, player, pokemon) -> 1.0F / coefficient);
            } else if ("multiply".equals(effect.operation)) {
                event.addModificationFunction((current, player, pokemon) -> current / coefficient);
            }
        }
    }

    private static void applySpawnEffects(SpawnEvent<PokemonEntity> event) {
        Pokemon pokemon = event.getEntity().getPokemon();
        if (event.getEntity().level().isClientSide() || ServerLifecycleHooks.getCurrentServer() == null) return;
        for (EventEffect effect : EventManager.getActiveEvents(ServerLifecycleHooks.getCurrentServer()).stream()
                .flatMap(scheduledEvent -> scheduledEvent.effects().stream()).toList()) {
            if (!matches(effect, pokemon)) continue;
            switch (effect.type) {
                case "hidden_ability" -> applyHiddenAbility(effect, pokemon);
                case "knows_move" -> applyKnownMove(effect, pokemon);
                case "min_ivs" -> applyMinimumIvs(effect, pokemon);
                default -> { }
            }
        }
    }

    private static boolean matches(EventEffect effect, Pokemon pokemon) {
        if (effect.targets == null || effect.targets.isEmpty()) return true;
        String speciesId = pokemon.getSpecies().getResourceIdentifier().toString();
        for (EventEffect.PokemonTarget target : effect.targets) {
            if (!"all".equals(target.species) && !speciesId.equals(target.species)) continue;
            if (target.aspects == null || pokemon.getAspects().containsAll(target.aspects)) return true;
        }
        return false;
    }

    private static void applyHiddenAbility(EventEffect effect, Pokemon pokemon) {
        if (!roll(effect.chance)) return;
        List<HiddenAbility> hiddenAbilities = pokemon.getForm().getAbilities().getMapping().getOrDefault(Priority.LOW, List.of()).stream()
                .filter(HiddenAbility.class::isInstance)
                .map(HiddenAbility.class::cast)
                .toList();
        if (hiddenAbilities == null || hiddenAbilities.isEmpty()) return;
        HiddenAbility hiddenAbility = hiddenAbilities.get(ThreadLocalRandom.current().nextInt(hiddenAbilities.size()));
        pokemon.updateAbility(hiddenAbility.getTemplate().create(false, Priority.LOW));
    }

    private static void applyKnownMove(EventEffect effect, Pokemon pokemon) {
        if (effect.move == null || !roll(effect.chance)) return;
        var moveTemplate = Moves.getByName(effect.move);
        if (moveTemplate == null || pokemon.getMoveSet().getMoves().stream().anyMatch(move -> move.getTemplate() == moveTemplate)) return;
        for (BenchedMove learnedMove : pokemon.getBenchedMoves()) {
            if (learnedMove.getMoveTemplate() == moveTemplate) return;
        }
        MoveSet moveSet = pokemon.getMoveSet();
        int slot = ThreadLocalRandom.current().nextInt(MoveSet.MOVE_COUNT);
        moveSet.setMove(slot, moveTemplate.create());
        pokemon.getBenchedMoves().add(new BenchedMove(moveTemplate, 0));
    }

    private static void applyMinimumIvs(EventEffect effect, Pokemon pokemon) {
        if (effect.value == null) return;
        int minimum = Math.clamp(effect.value.intValue(), 0, 31);
        for (Stat stat : Stats.Companion.getPERMANENT()) {
            if (pokemon.getIvs().get(stat) < minimum) pokemon.setIV(stat, minimum);
        }
    }

    private static boolean roll(Double chance) {
        return chance != null && chance > 0.0 && ThreadLocalRandom.current().nextDouble() < Math.min(chance, 1.0);
    }
}
