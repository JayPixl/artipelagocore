package io.jaypixl.artipelagocore.arcintegration.event;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnBucketChosenEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;
import com.cobblemon.mod.common.api.events.pokemon.EvGainedEvent;
import com.cobblemon.mod.common.api.events.pokemon.HyperTrainedIvEvent;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.experience.BattleExperienceSource;
import com.cobblemon.mod.common.api.pokemon.experience.CandyExperienceSource;
import com.cobblemon.mod.common.api.pokemon.stats.BattleEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.ItemEvSource;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.interactive.VitaminItem;
import com.cobblemon.mod.common.pokemon.IVs;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcServerPlayer;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.arcintegration.api.ACActionResultAccess;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionDataTypes;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionTypes;
import io.jaypixl.artipelagocore.arcintegration.condition.BattleContextCondition;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ACArcEvents {

    public static void init() {
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(ACArcEvents::handlePokemonGainExp);
        CobblemonEvents.EV_GAINED_EVENT_PRE.subscribe(ACArcEvents::handlePokemonGainEvs);
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(ACArcEvents::handlePokemonCatchRate);
        CobblemonEvents.HYPER_TRAINED_IV_PRE.subscribe(ACArcEvents::handlePokemonHyperTrainIv);
        CobblemonEvents.HYPER_TRAINED_IV_POST.subscribe(ACArcEvents::handlePokemonHyperTrainIvPost);
        CobblemonEvents.SPAWN_BUCKET_CHOSEN.subscribe(ACArcEvents::handleSpawnBucketChosen);
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(ACArcEvents::handlePokemonEntitySpawn);
        CobblemonEvents.BATTLE_FAINTED.subscribe(ACArcEvents::handleBattlePokemonFainted);
        CobblemonEvents.POKEMON_CAPTURED.subscribe(ACArcEvents::handleWildPokemonCaught);
    }

    public static void handlePokemonGainExp(ExperienceGainedEvent.Pre e) {
        //ArtipelagoCoreMod.LOGGER.info("COBBLEMON EVENT FIRED!");
        if (!(e.getPokemon().getOwnerPlayer() instanceof ServerPlayer player) || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        boolean gainedInBattle = e.getSource() instanceof BattleExperienceSource;
        boolean gainedFromCandy = e.getSource() instanceof CandyExperienceSource;

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_GAIN_POKEMON_EXP)
                .withData(ACActionDataTypes.POKEMON, e.getPokemon())
                .withData(ACActionDataTypes.EXP_GAIN_AMOUNT, e.getExperience())
                .withData(ACActionDataTypes.EXP_GAINED_IN_BATTLE, gainedInBattle)
                .withData(ACActionDataTypes.EXP_GAINED_FROM_CANDY, gainedFromCandy)
                .build()
                .sendToAction();

        //ArtipelagoCoreMod.LOGGER.info("OLD EXP: " + e.getExperience());

        float expMultiplier = ((ACActionResultAccess) result).artipelagocore$getExpMultiplier();
        int newExp = Math.round(e.getExperience() * expMultiplier);

        //ArtipelagoCoreMod.LOGGER.info("New EXP: " + newExp);
        //ArtipelagoCoreMod.LOGGER.info("Multiplier: " + expMultiplier);

        e.setExperience(newExp);
    }

    public static void handlePokemonGainEvs(EvGainedEvent.Pre e) {
        ServerPlayer player = null;
        String sourceType = null;
        if (e.getSource() instanceof BattleEvSource) {
            sourceType = "battle";
            if (e.getPokemon().getOwnerPlayer() instanceof ServerPlayer owner) {
                player = owner;
            }
        } else if (e.getSource() instanceof ItemEvSource itemSource) {
            if (itemSource.getStack().getItem() instanceof VitaminItem) {
                sourceType = "vitamin";
                player = itemSource.getPlayer();
            }
        }

        if (sourceType == null || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        if ("battle".equals(sourceType) && e.getSource() instanceof BattleEvSource battleSource) {
            Map<Stat, Integer> allocation = ACArcEventHelper.getOrCreateBattleEvBonus(arcplayer, e, battleSource);
            Integer bonus = allocation.get(e.getStat());
            if (bonus != null && bonus > 0) {
                e.setAmount(e.getAmount() + bonus);
                allocation.remove(e.getStat());
                if (allocation.isEmpty()) {
                    ACArcEventHelper.clearBattleEvBonus(battleSource.getBattle(), e.getPokemon().getUuid());
                }
            }
            return;
        }

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_GAIN_POKEMON_EVS)
                .withData(ACActionDataTypes.POKEMON, e.getPokemon())
                .withData(ACActionDataTypes.EV_STAT, e.getStat())
                .withData(ACActionDataTypes.EV_GAIN_AMOUNT, e.getAmount())
                .withData(ACActionDataTypes.EV_GAIN_SOURCE, sourceType)
                .build()
                .sendToAction();

        int bonus = ACArcEventHelper.rollBonusRange(((ACActionResultAccess) result).artipelagocore$getEvBonusMin(),
                ((ACActionResultAccess) result).artipelagocore$getEvBonusMax());
        if (bonus > 0) {
            e.setAmount(e.getAmount() + bonus);
        }
    }

    public static void handlePokemonCatchRate(PokemonCatchRateEvent e) {
        if (!(e.getThrower() instanceof ServerPlayer player) || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_MODIFY_POKEMON_CATCH_RATE)
                .withData(ACActionDataTypes.POKEMON, e.getPokemonEntity().getPokemon())
                .withData(ACActionDataTypes.CATCH_RATE, e.getCatchRate())
                .withData(ACActionDataTypes.POKEBALL_TYPE, e.getPokeBallEntity().getPokeBall().getName().toString())
                .withData(ACActionDataTypes.IS_IN_BATTLE, e.getPokemonEntity().getBattleId() != null)
                .build()
                .sendToAction();

        float catchRateMultiplier = ((ACActionResultAccess) result).artipelagocore$getCatchRateMultiplier();
        if (catchRateMultiplier != 1.0f) {
            e.setCatchRate(Math.max(0.0f, e.getCatchRate() * catchRateMultiplier));
        }
    }

    public static void handlePokemonHyperTrainIv(HyperTrainedIvEvent.Pre e) {
        if (!(e.getPokemon().getOwnerPlayer() instanceof ServerPlayer player) || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }
        if (e.getValue() <= e.getPokemon().getIvs().getEffectiveBattleIV(e.getStat())) {
            return;
        }

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_HYPER_TRAIN_POKEMON_IV)
                .withData(ACActionDataTypes.POKEMON, e.getPokemon())
                .withData(ACActionDataTypes.IV_STAT, e.getStat())
                .withData(ACActionDataTypes.IV_TARGET_VALUE, e.getValue())
                .build()
                .sendToAction();

        int bonus = ACArcEventHelper.rollBonusRange(((ACActionResultAccess) result).artipelagocore$getIvBonusMin(),
                ((ACActionResultAccess) result).artipelagocore$getIvBonusMax());
        //ArtipelagoCoreMod.LOGGER.info("Bonus IV: " + bonus);
        if (bonus > 0) {
            int newValue = Math.min(IVs.MAX_VALUE, e.getValue() + bonus);
            //ArtipelagoCoreMod.LOGGER.info("New IV Value: " + newValue);
            e.setValue(newValue);
            ACArcEventHelper.setHyperTrainIvOverride(e.getPokemon().getUuid(), e.getStat(), newValue);
        }
    }

    public static void handlePokemonHyperTrainIvPost(HyperTrainedIvEvent.Post e) {
        Integer newValue = ACArcEventHelper.consumeHyperTrainIvOverride(e.getPokemon().getUuid(), e.getStat());
        if (newValue == null || newValue == e.getValue()) {
            return;
        }

        float healthRatio = e.getPokemon().getMaxHealth() <= 0 ? 0.0f
                : Math.clamp(e.getPokemon().getCurrentHealth() / (float) e.getPokemon().getMaxHealth(), 0.0f, 1.0f);
        e.getPokemon().getIvs().setHyperTrainedIV(e.getStat(), newValue);
        if (e.getStat() == Stats.HP) {
            e.getPokemon().setCurrentHealth(Math.round(e.getPokemon().getMaxHealth() * healthRatio));
        }
    }

    public static void handleSpawnBucketChosen(SpawnBucketChosenEvent e) {
        if (!(e.getSpawnCause().getEntity() instanceof ServerPlayer player) || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        // This action runs at bucket-selection time so rewards can bias a player's spawn table
        // without touching Cobblemon's global spawn data.
        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_CHOOSE_POKEMON_SPAWN_BUCKET)
                .withData(ACActionDataTypes.SPAWN_BUCKET, e.getBucket().getName())
                .build()
                .sendToAction();

        var rerolledBucket = ACArcEventHelper.rerollSpawnBucket(
                e.getBucketWeights(),
                ((ACActionResultAccess) result).artipelagocore$getSpawnBucketMultipliers()
        );
        if (rerolledBucket != null) {
            e.setBucket(rerolledBucket);
        }
    }

    public static void handlePokemonEntitySpawn(SpawnEvent<PokemonEntity> e) {
        if (e.getEntity().getSpawnCause() == null
                || !(e.getEntity().getSpawnCause().getEntity() instanceof ServerPlayer player)
                || !(player instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        ActionResult result = new ActionDataBuilder(arcplayer, ACActionTypes.ON_SPAWN_WILD_POKEMON)
                .withData(ACActionDataTypes.POKEMON, e.getEntity().getPokemon())
                .build()
                .sendToAction();

        if (!((ACActionResultAccess) result).artipelagocore$getHiddenAbility()) {
            return;
        }

        ACArcEventHelper.tryAssignHiddenAbility(e.getEntity(), player.getGameProfile().getName());
    }

    public static void handleBattlePokemonFainted(BattleFaintedEvent e) {
        BattleActor defeatedActor = e.getKilled().getActor();
        for (ServerPlayer player : e.getBattle().getPlayers()) {
            BattleActor playerActor = e.getBattle().getActor(player);
            if (playerActor == null || playerActor.getSide() == defeatedActor.getSide() || !(player instanceof ArcServerPlayer arcplayer)) {
                continue;
            }

            String ctx;

            if (e.getBattle().isPvP()) {
                ctx = "pvp";
            } else if (e.getBattle().isPvW()) {
                ctx = "pve";
            } else {
                ctx = "pvn";
            }

            new ActionDataBuilder(arcplayer, ACActionTypes.ON_DEFEAT_POKEMON)
                    .withData(ACActionDataTypes.POKEMON, e.getKilled().getEffectedPokemon())
                    .withData(ACActionDataTypes.BATTLE_CONTEXT, ctx)
                    .build()
                    .sendToAction();
        }
    }

    public static void handleWildPokemonCaught(PokemonCapturedEvent e) {
        if (!(e.getPlayer() instanceof ArcServerPlayer arcplayer)) {
            return;
        }

        new ActionDataBuilder(arcplayer, ACActionTypes.ON_CATCH_WILD_POKEMON)
                .withData(ACActionDataTypes.POKEMON, e.getPokemon())
                .build()
                .sendToAction();
    }
}
