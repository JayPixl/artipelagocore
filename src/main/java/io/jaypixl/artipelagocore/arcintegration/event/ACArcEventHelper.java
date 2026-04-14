package io.jaypixl.artipelagocore.arcintegration.event;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.pokemon.EvGainedEvent;
import com.cobblemon.mod.common.api.pokemon.stats.BattleEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionDataTypes;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionTypes;
import io.jaypixl.artipelagocore.arcintegration.api.ACActionResultAccess;
import io.jaypixl.artipelagocore.arcintegration.api.RestrictionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ACArcEventHelper {
    public static final float RIDE_INTERVAL_DISTANCE = 128.0f;
    public static final int RIDE_INTERVAL_MIN_TICKS = 100;
    private static final double MIN_RIDE_STEP_DISTANCE = 0.05D;
    private static final double MAX_RIDE_STEP_DISTANCE = 8.0D;
    private static final Map<PokemonBattle, Map<UUID, Map<Stat, Integer>>> BATTLE_EV_BONUS_CACHE = new WeakHashMap<>();
    private static final Map<UUID, Map<Stat, Integer>> HYPER_TRAIN_IV_OVERRIDE_CACHE = new HashMap<>();
    private static final Map<UUID, RideProgress> RIDE_PROGRESS_CACHE = new HashMap<>();

    public static int rollBonusRange(int min, int max) {
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
        if (max == 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static Map<Stat, Integer> getOrCreateBattleEvBonus(ArcServerPlayer arcplayer, EvGainedEvent.Pre e, BattleEvSource battleSource) {
        PokemonBattle battle = battleSource.getBattle();
        Map<UUID, Map<Stat, Integer>> byPokemon = BATTLE_EV_BONUS_CACHE.computeIfAbsent(battle, key -> new HashMap<>());
        UUID pokemonId = e.getPokemon().getUuid();
        Map<Stat, Integer> allocation = byPokemon.get(pokemonId);
        if (allocation != null) {
            return allocation;
        }

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_GAIN_POKEMON_EVS)
                .withData(ACActionDataTypes.POKEMON, e.getPokemon())
                .withData(ACActionDataTypes.EV_STAT, e.getStat())
                .withData(ACActionDataTypes.EV_GAIN_AMOUNT, e.getAmount())
                .withData(ACActionDataTypes.EV_GAIN_SOURCE, "battle")
                .build()
                .sendToAction();

        int bonus = rollBonusRange(((ACActionResultAccess) result).artipelagocore$getEvBonusMin(),
                ((ACActionResultAccess) result).artipelagocore$getEvBonusMax());
        allocation = distributeEvBonus(bonus, battleSource);
        byPokemon.put(pokemonId, allocation);
        return allocation;
    }

    public static void clearBattleEvBonus(PokemonBattle battle, UUID pokemonId) {
        Map<UUID, Map<Stat, Integer>> byPokemon = BATTLE_EV_BONUS_CACHE.get(battle);
        if (byPokemon == null) {
            return;
        }

        byPokemon.remove(pokemonId);
        if (byPokemon.isEmpty()) {
            BATTLE_EV_BONUS_CACHE.remove(battle);
        }
    }

    public static void setHyperTrainIvOverride(UUID pokemonId, Stat stat, int value) {
        HYPER_TRAIN_IV_OVERRIDE_CACHE
                .computeIfAbsent(pokemonId, key -> new HashMap<>())
                .put(stat, value);
    }

    public static Integer consumeHyperTrainIvOverride(UUID pokemonId, Stat stat) {
        Map<Stat, Integer> overrides = HYPER_TRAIN_IV_OVERRIDE_CACHE.get(pokemonId);
        if (overrides == null) {
            return null;
        }

        Integer newValue = overrides.remove(stat);
        if (overrides.isEmpty()) {
            HYPER_TRAIN_IV_OVERRIDE_CACHE.remove(pokemonId);
        }
        return newValue;
    }

    public static SpawnBucket rerollSpawnBucket(Map<SpawnBucket, Float> baseWeights, Map<String, Float> multipliers) {
        if (baseWeights.isEmpty() || multipliers.isEmpty()) {
            return null;
        }

        float totalWeight = 0.0f;
        Map<SpawnBucket, Float> adjustedWeights = new HashMap<>();
        for (Map.Entry<SpawnBucket, Float> entry : baseWeights.entrySet()) {
            float multiplier = Math.max(0.0f, multipliers.getOrDefault(entry.getKey().getName(), 1.0f));
            float adjustedWeight = Math.max(0.0f, entry.getValue() * multiplier);
            if (adjustedWeight > 0.0f) {
                adjustedWeights.put(entry.getKey(), adjustedWeight);
                totalWeight += adjustedWeight;
            }
        }

        if (totalWeight <= 0.0f) {
            return null;
        }

        float roll = ThreadLocalRandom.current().nextFloat() * totalWeight;
        for (Map.Entry<SpawnBucket, Float> entry : adjustedWeights.entrySet()) {
            roll -= entry.getValue();
            if (roll <= 0.0f) {
                return entry.getKey();
            }
        }

        return adjustedWeights.keySet().iterator().next();
    }

    public static void tryAssignHiddenAbility(PokemonEntity entity, String playerName) {
        if (entity.getPokemon().getAbility().getPriority() == Priority.LOW) {
//            ArtipelagoCoreMod.LOGGER.info(
//                    "Spawn {} for player {} already had low-priority ability {}",
//                    entity.getPokemon().getSpecies().getResourceIdentifier(),
//                    playerName,
//                    entity.getPokemon().getAbility().getName()
//            );
            return;
        }

        // Cobblemon's standard wild spawn path does not naturally pick hidden abilities here,
        // so the Arc reward upgrades the spawn after the entity exists but before gameplay uses it.
        List<PotentialAbility> hiddenAbilities = entity.getPokemon().getForm().getAbilities().getMapping().get(Priority.LOW);
        if (hiddenAbilities == null || hiddenAbilities.isEmpty()) {
//            ArtipelagoCoreMod.LOGGER.info(
//                    "Spawn {} for player {} has no hidden ability candidates on form {}",
//                    entity.getPokemon().getSpecies().getResourceIdentifier(),
//                    playerName,
//                    entity.getPokemon().getForm().getName()
//            );
            return;
        }

        PotentialAbility hiddenAbility = hiddenAbilities.get(ThreadLocalRandom.current().nextInt(hiddenAbilities.size()));
        entity.getPokemon().updateAbility(hiddenAbility.getTemplate().create(false, hiddenAbility.getPriority()));
//        ArtipelagoCoreMod.LOGGER.info(
//                "Assigned hidden ability {} to spawn {} for player {}",
//                entity.getPokemon().getAbility().getName(),
//                entity.getPokemon().getSpecies().getResourceIdentifier(),
//                playerName
//        );
    }

    public static void clearRideProgress(UUID playerId) {
        RIDE_PROGRESS_CACHE.remove(playerId);
    }

    public static void tickRideProgress(ArcServerPlayer arcplayer, ServerPlayer player, PokemonEntity pokemonEntity) {
        Vec3 currentPos = pokemonEntity.position();
        RideProgress progress = RIDE_PROGRESS_CACHE.get(player.getUUID());
        if (progress == null || !progress.mountId.equals(pokemonEntity.getUUID())) {
            RIDE_PROGRESS_CACHE.put(player.getUUID(), new RideProgress(pokemonEntity.getUUID(), currentPos));
            return;
        }

        progress.rideDurationTicks++;
        progress.ticksSinceLastReward++;

        double stepDistance = currentPos.distanceTo(progress.lastPosition);
        progress.lastPosition = currentPos;

        if (stepDistance < MIN_RIDE_STEP_DISTANCE || stepDistance > MAX_RIDE_STEP_DISTANCE) {
            return;
        }

        progress.totalDistance += (float) stepDistance;
        progress.intervalDistance += (float) stepDistance;

        if (progress.intervalDistance < RIDE_INTERVAL_DISTANCE || progress.ticksSinceLastReward < RIDE_INTERVAL_MIN_TICKS) {
            return;
        }

        new ActionDataBuilder(arcplayer, ACActionTypes.ON_RIDE_POKEMON_INTERVAL)
                .withData(ACActionDataTypes.POKEMON, pokemonEntity.getPokemon())
                .withData(ACActionDataTypes.RIDE_INTERVAL_DISTANCE, RIDE_INTERVAL_DISTANCE)
                .withData(ACActionDataTypes.RIDE_TOTAL_DISTANCE, progress.totalDistance)
                .withData(ACActionDataTypes.RIDE_DURATION_TICKS, progress.rideDurationTicks)
                .build()
                .sendToAction();

        ArtipelagoCoreMod.LOGGER.info(
                "Ride interval trigger: player={}, pokemon={}, intervalDistance={}, totalDistance={}, durationTicks={}",
                player.getGameProfile().getName(),
                pokemonEntity.getPokemon().getSpecies().getResourceIdentifier(),
                RIDE_INTERVAL_DISTANCE,
                progress.totalDistance,
                progress.rideDurationTicks
        );

        progress.intervalDistance -= RIDE_INTERVAL_DISTANCE;
        progress.ticksSinceLastReward = 0;
    }

    private static final class RideProgress {
        private final UUID mountId;
        private Vec3 lastPosition;
        private float intervalDistance;
        private float totalDistance;
        private int rideDurationTicks;
        private int ticksSinceLastReward;

        private RideProgress(UUID mountId, Vec3 lastPosition) {
            this.mountId = mountId;
            this.lastPosition = lastPosition;
        }
    }

    private static Map<Stat, Integer> distributeEvBonus(int totalBonus, BattleEvSource battleSource) {
        if (totalBonus <= 0) {
            return new HashMap<>();
        }
        List<Stat> yieldStats = getBattleEvYieldStats(battleSource);
        if (yieldStats.isEmpty()) {
            return new HashMap<>();
        }

        int base = totalBonus / yieldStats.size();
        int remainder = totalBonus % yieldStats.size();
        Map<Stat, Integer> allocation = new HashMap<>();
        for (int i = 0; i < yieldStats.size(); i++) {
            int bonus = base + (i < remainder ? 1 : 0);
            if (bonus > 0) {
                allocation.put(yieldStats.get(i), bonus);
            }
        }
        return allocation;
    }

    private static List<Stat> getBattleEvYieldStats(BattleEvSource battleSource) {
        Set<Stat> stats = new HashSet<>();
        for (BattlePokemon opponent : battleSource.getFacedPokemon()) {
            if (opponent.getHealth() != 0) {
                continue;
            }
            for (Map.Entry<Stat, Integer> entry : opponent.getOriginalPokemon().getForm().getEvYield().entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    stats.add(entry.getKey());
                }
            }
        }

        List<Stat> sorted = new ArrayList<>(stats);
        sorted.sort(Comparator.comparing(stat -> stat.getIdentifier().toString()));
        return sorted;
    }

    public static boolean isRestrictedBlockInteract(ServerPlayer serverPlayer, BlockState blockState, BlockPos blockPos, ItemStack itemStack, Level world) {
        if (!(serverPlayer instanceof ArcPlayer arcPlayer)
                || !(serverPlayer instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer)) {
            return false;
        }

        RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                new ActionDataBuilder(arcPlayer, null)
                        .withData(ActionDataType.ITEM_STACK, itemStack)
                        .withData(ActionDataType.ITEM, itemStack.getItem())
                        .withData(ActionDataType.BLOCK_STATE, blockState)
                        .withData(ActionDataType.BLOCK_POSITION, blockPos)
                        .withData(ActionDataType.WORLD, world)
                        .build()
        );
        return result.isRestricted(RestrictionTypes.INTERACT_BLOCK);
    }

    public static boolean isRestrictedBlockPlace(ServerPlayer serverPlayer, BlockState blockState, BlockPos blockPos, Level world) {
        if (!(serverPlayer instanceof ArcPlayer arcPlayer)
                || !(serverPlayer instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer)) {
            return false;
        }

        RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                new ActionDataBuilder(arcPlayer, null)
                        .withData(ActionDataType.ITEM_STACK, blockState.getBlock().asItem().getDefaultInstance())
                        .withData(ActionDataType.BLOCK_STATE, blockState)
                        .withData(ActionDataType.BLOCK_POSITION, blockPos)
                        .withData(ActionDataType.WORLD, world)
                        .build()
        );
        return result.isRestricted(RestrictionType.PLACE_BLOCK);
    }

    public static boolean isRestrictedEntityInteract(ServerPlayer serverPlayer, Entity entity, ItemStack itemStack, Level world) {
        if (!(serverPlayer instanceof ArcPlayer arcPlayer)
                || !(serverPlayer instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer)) {
            return false;
        }

        RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                new ActionDataBuilder(arcPlayer, null)
                        .withData(ActionDataType.ITEM_STACK, itemStack)
                        .withData(ActionDataType.ITEM, itemStack.getItem())
                        .withData(ActionDataType.ENTITY, entity)
                        .withData(ActionDataType.WORLD, world)
                        .build()
        );
        return result.isRestricted(RestrictionTypes.INTERACT_ENTITY);
    }
}
