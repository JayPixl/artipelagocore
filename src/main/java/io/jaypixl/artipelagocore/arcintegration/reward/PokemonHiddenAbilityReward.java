package io.jaypixl.artipelagocore.arcintegration.reward;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.arcintegration.api.ACActionResultAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
/**
 * Marks a spawned wild Pokemon to receive a hidden ability when this reward fires.
 * Arc's native reward `chance` handles the RNG, so this reward itself only carries intent.
 */
public class PokemonHiddenAbilityReward extends AbstractReward {

    public PokemonHiddenAbilityReward(double chance, int priority) {
        super(chance, priority);
    }

    @Override
    public Component getDescription() {
        return super.getDescription();
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ActionResult result = new ActionResult();
        ((ACActionResultAccess) result).artipelagocore$withHiddenAbility();
        return result;
    }

    @Override
    public IRewardType<?> getType() {
        return ACRewardTypes.POKEMON_HIDDEN_ABILITY;
    }

    public static class Serializer implements IRewardSerializer<PokemonHiddenAbilityReward> {

        @Override
        public PokemonHiddenAbilityReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new PokemonHiddenAbilityReward(
                    chance,
                    priority);
        }

        @Override
        public PokemonHiddenAbilityReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new PokemonHiddenAbilityReward(
                    chance,
                    priority);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonHiddenAbilityReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
