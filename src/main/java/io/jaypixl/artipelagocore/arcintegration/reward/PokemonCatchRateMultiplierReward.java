package io.jaypixl.artipelagocore.arcintegration.reward;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.arcintegration.api.ACActionResultAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class PokemonCatchRateMultiplierReward extends AbstractReward {

    private final float multiplier;

    public PokemonCatchRateMultiplierReward(double chance, int priority, float multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ActionResult result = new ActionResult();
        ((ACActionResultAccess) result).artipelagocore$setCatchRateMultiplier(multiplier);
        return result;
    }

    @Override
    public IRewardType<?> getType() {
        return ACRewardTypes.POKEMON_CATCH_RATE_MULTIPLIER;
    }

    public static class Serializer implements IRewardSerializer<PokemonCatchRateMultiplierReward> {

        @Override
        public PokemonCatchRateMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new PokemonCatchRateMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public PokemonCatchRateMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new PokemonCatchRateMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonCatchRateMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
