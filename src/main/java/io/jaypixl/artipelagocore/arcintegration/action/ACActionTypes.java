package io.jaypixl.artipelagocore.arcintegration.action;

import com.daqem.arc.api.action.type.ActionType;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.resources.ResourceLocation;

public final class ACActionTypes {
    public static ActionType<LevelUpPokemonAction> ON_POKEMON_LEVEL_UP;
    public static ActionType<RidePokemonIntervalAction> ON_RIDE_POKEMON_INTERVAL;
    public static ActionType<GainPokemonExpAction> ON_GAIN_POKEMON_EXP;
    public static ActionType<GainPokemonEvAction> ON_GAIN_POKEMON_EVS;
    public static ActionType<ModifyPokemonCatchRateAction> ON_MODIFY_POKEMON_CATCH_RATE;
    public static ActionType<HyperTrainPokemonIvAction> ON_HYPER_TRAIN_POKEMON_IV;
    public static ActionType<ChoosePokemonSpawnBucketAction> ON_CHOOSE_POKEMON_SPAWN_BUCKET;
    public static ActionType<SpawnWildPokemonAction> ON_SPAWN_WILD_POKEMON;
    public static ActionType<CatchWildPokemonAction> ON_CATCH_WILD_POKEMON;
    public static ActionType<DefeatPokemonAction> ON_DEFEAT_POKEMON;

    public static void init() {
        ON_POKEMON_LEVEL_UP = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_pokemon_level_up"),
                new LevelUpPokemonAction.Serializer()
        );
        ON_RIDE_POKEMON_INTERVAL = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_ride_pokemon_interval"),
                new RidePokemonIntervalAction.Serializer()
        );
        ON_GAIN_POKEMON_EXP = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_gain_pokemon_exp"),
                new GainPokemonExpAction.Serializer()
        );
        ON_GAIN_POKEMON_EVS = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_gain_pokemon_evs"),
                new GainPokemonEvAction.Serializer()
        );
        ON_MODIFY_POKEMON_CATCH_RATE = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_modify_pokemon_catch_rate"),
                new ModifyPokemonCatchRateAction.Serializer()
        );
        ON_HYPER_TRAIN_POKEMON_IV = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_hyper_train_pokemon_iv"),
                new HyperTrainPokemonIvAction.Serializer()
        );
        ON_CHOOSE_POKEMON_SPAWN_BUCKET = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_choose_pokemon_spawn_bucket"),
                new ChoosePokemonSpawnBucketAction.Serializer()
        );
        ON_SPAWN_WILD_POKEMON = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_spawn_wild_pokemon"),
                new SpawnWildPokemonAction.Serializer()
        );
        ON_DEFEAT_POKEMON = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_defeat_pokemon"),
                new DefeatPokemonAction.Serializer()
        );
        ON_CATCH_WILD_POKEMON = ActionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "on_catch_wild_pokemon"),
                new CatchWildPokemonAction.Serializer()
        );
    }
}
