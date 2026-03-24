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

public class PokemonExpMultiplierReward extends AbstractReward {

    private final float multiplier;

    public PokemonExpMultiplierReward(double chance, int priority, float multiplier) {
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
        ((ACActionResultAccess) result).artipelagocore$setExpMultiplier(multiplier);
        return result;
    }

    @Override
    public IRewardType<?> getType() {
        return ACRewardTypes.POKEMON_EXP_MULTIPLIER;
    }

    public static class Serializer implements IRewardSerializer<PokemonExpMultiplierReward> {

        @Override
        public PokemonExpMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new PokemonExpMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public PokemonExpMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new PokemonExpMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonExpMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
