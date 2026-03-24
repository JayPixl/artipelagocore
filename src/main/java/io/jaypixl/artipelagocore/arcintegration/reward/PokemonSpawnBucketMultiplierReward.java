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

/**
 * Multiplies one named Cobblemon spawn bucket for the current player-scoped spawn roll.
 * Matching rewards stack multiplicatively so separate perks can keep amplifying the same bucket.
 */
public class PokemonSpawnBucketMultiplierReward extends AbstractReward {

    private final String bucket;
    private final float multiplier;

    public PokemonSpawnBucketMultiplierReward(double chance, int priority, String bucket, float multiplier) {
        super(chance, priority);
        this.bucket = bucket;
        this.multiplier = Math.max(0.0f, multiplier);
    }

    @Override
    public Component getDescription() {
        return getDescription(bucket + " x" + multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ActionResult result = new ActionResult();
        ((ACActionResultAccess) result).artipelagocore$addSpawnBucketMultiplier(bucket, multiplier);
        return result;
    }

    @Override
    public IRewardType<?> getType() {
        return ACRewardTypes.POKEMON_SPAWN_BUCKET_MULTIPLIER;
    }

    public static class Serializer implements IRewardSerializer<PokemonSpawnBucketMultiplierReward> {

        @Override
        public PokemonSpawnBucketMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new PokemonSpawnBucketMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsString(jsonObject, "bucket"),
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public PokemonSpawnBucketMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new PokemonSpawnBucketMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readUtf(),
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonSpawnBucketMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeUtf(type.bucket);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
