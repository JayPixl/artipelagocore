package io.jaypixl.artipelagocore.arcintegration.condition;

import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.resources.ResourceLocation;

public final class ACConditionTypes {
    public static IConditionType<CatchInBattleCondition> IN_BATTLE;
    public static IConditionType<PokemonEvContextCondition> POKEMON_EV_CONTEXT;
    public static IConditionType<PokemonExpContextCondition> POKEMON_EXP_CONTEXT;
    public static IConditionType<BattleContextCondition> BATTLE_CONTEXT;

    private ACConditionTypes() {
    }

    public static void init() {
        IN_BATTLE = ConditionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "in_battle"),
                new CatchInBattleCondition.Serializer()
        );
        POKEMON_EV_CONTEXT = ConditionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_ev_context"),
                new PokemonEvContextCondition.Serializer()
        );
        POKEMON_EXP_CONTEXT = ConditionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon_exp_context"),
                new PokemonExpContextCondition.Serializer()
        );
        BATTLE_CONTEXT = ConditionType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "battle_context"),
                new BattleContextCondition.Serializer()
        );
    }
}
