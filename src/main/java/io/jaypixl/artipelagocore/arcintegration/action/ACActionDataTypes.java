package io.jaypixl.artipelagocore.arcintegration.action;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.data.type.IActionDataType;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class ACActionDataTypes {
    public static IActionDataType<Integer> EXP_GAIN_AMOUNT;
    public static IActionDataType<Boolean> EXP_GAINED_IN_BATTLE;
    public static IActionDataType<Boolean> EXP_GAINED_FROM_CANDY;
    public static IActionDataType<Integer> EV_GAIN_AMOUNT;
    public static IActionDataType<String> EV_GAIN_SOURCE;
    public static IActionDataType<Stat> EV_STAT;
    public static IActionDataType<Float> CATCH_RATE;
    public static IActionDataType<String> POKEBALL_TYPE;
    public static IActionDataType<Boolean> IS_IN_BATTLE;
    public static IActionDataType<Stat> IV_STAT;
    public static IActionDataType<Integer> IV_TARGET_VALUE;
    public static IActionDataType<Pokemon> POKEMON;
    public static IActionDataType<String> SPAWN_BUCKET;
    public static IActionDataType<String> BATTLE_CONTEXT;
    public static IActionDataType<InteractionHand> HAND;

    public static void init() {
        EXP_GAIN_AMOUNT = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "exp_gain_amount")
        );
        EXP_GAINED_IN_BATTLE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "exp_gained_in_battle")
        );
        EXP_GAINED_FROM_CANDY = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "exp_gained_from_candy")
        );
        EV_GAIN_AMOUNT = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "ev_gain_amount")
        );
        EV_GAIN_SOURCE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "ev_gain_source")
        );
        EV_STAT = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "ev_stat")
        );
        CATCH_RATE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "catch_rate")
        );
        POKEBALL_TYPE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokeball_type")
        );
        IS_IN_BATTLE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "catch_in_battle")
        );
        IV_STAT = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "iv_stat")
        );
        IV_TARGET_VALUE = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "iv_target_value")
        );
        POKEMON = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "pokemon")
        );
        SPAWN_BUCKET = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "spawn_bucket")
        );
        BATTLE_CONTEXT = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "battle_context")
        );
        HAND = ActionDataType.register(
                ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, "hand")
        );
    }
}
