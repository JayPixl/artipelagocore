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

public class PokemonEvBonusReward extends AbstractReward {

    private final int min;
    private final int max;

    public PokemonEvBonusReward(double chance, int priority, int min, int max) {
        super(chance, priority);
        this.min = min;
        this.max = max;
    }

    @Override
    public Component getDescription() {
        return getDescription(min + " - " + max);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ActionResult result = new ActionResult();
        ((ACActionResultAccess) result).artipelagocore$setEvBonusRange(min, max);
        return result;
    }

    @Override
    public IRewardType<?> getType() {
        return ACRewardTypes.POKEMON_EV_BONUS;
    }

    public static class Serializer implements IRewardSerializer<PokemonEvBonusReward> {

        @Override
        public PokemonEvBonusReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new PokemonEvBonusReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "min"),
                    GsonHelper.getAsInt(jsonObject, "max"));
        }

        @Override
        public PokemonEvBonusReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new PokemonEvBonusReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonEvBonusReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.min);
            friendlyByteBuf.writeInt(type.max);
        }
    }
}
