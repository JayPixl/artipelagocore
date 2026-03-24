package io.jaypixl.artipelagocore.arcintegration.reward;

import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.resources.ResourceLocation;

public class ACRewardTypes {
    public static IRewardType<PokemonExpMultiplierReward> POKEMON_EXP_MULTIPLIER;
    public static IRewardType<PokemonCatchRateMultiplierReward> POKEMON_CATCH_RATE_MULTIPLIER;
    public static IRewardType<PokemonEvBonusReward> POKEMON_EV_BONUS;
    public static IRewardType<PokemonHyperTrainIvBonusReward> POKEMON_HYPER_TRAIN_IV_BONUS;
    public static IRewardType<PokemonSpawnBucketMultiplierReward> POKEMON_SPAWN_BUCKET_MULTIPLIER;
    public static IRewardType<PokemonHiddenAbilityReward> POKEMON_HIDDEN_ABILITY;

    public static void init() {
        POKEMON_EXP_MULTIPLIER = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_exp_multiplier"),
                new PokemonExpMultiplierReward.Serializer()
        );
        POKEMON_CATCH_RATE_MULTIPLIER = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_catch_rate_multiplier"),
                new PokemonCatchRateMultiplierReward.Serializer()
        );
        POKEMON_EV_BONUS = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_ev_bonus"),
                new PokemonEvBonusReward.Serializer()
        );
        POKEMON_HYPER_TRAIN_IV_BONUS = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_hyper_train_iv_bonus"),
                new PokemonHyperTrainIvBonusReward.Serializer()
        );
        POKEMON_SPAWN_BUCKET_MULTIPLIER = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_spawn_bucket_multiplier"),
                new PokemonSpawnBucketMultiplierReward.Serializer()
        );
        POKEMON_HIDDEN_ABILITY = RewardType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_hidden_ability"),
                new PokemonHiddenAbilityReward.Serializer()
        );
    }
}
