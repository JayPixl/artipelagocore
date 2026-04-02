package io.jaypixl.artipelagocore.arcintegration.action;

import com.daqem.arc.api.action.AbstractAction;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RidePokemonIntervalAction extends AbstractAction {
    public RidePokemonIntervalAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ACActionTypes.ON_RIDE_POKEMON_INTERVAL;
    }

    public static class Serializer implements IActionSerializer<RidePokemonIntervalAction> {

        @Override
        public RidePokemonIntervalAction fromJson(ResourceLocation location,
                                                  JsonObject jsonObject,
                                                  ResourceLocation actionHolderLocation,
                                                  IActionHolderType<?> actionHolderType,
                                                  boolean performOnClient,
                                                  List<IReward> rewards,
                                                  List<ICondition> conditions) {
            return new RidePokemonIntervalAction(
                    location,
                    actionHolderLocation,
                    actionHolderType,
                    performOnClient,
                    rewards,
                    conditions
            );
        }

        @Override
        public RidePokemonIntervalAction fromNetwork(ResourceLocation location,
                                                     RegistryFriendlyByteBuf buf,
                                                     ResourceLocation actionHolderLocation,
                                                     IActionHolderType<?> actionHolderType,
                                                     boolean performOnClient,
                                                     List<IReward> rewards,
                                                     List<ICondition> conditions) {
            return new RidePokemonIntervalAction(
                    location,
                    actionHolderLocation,
                    actionHolderType,
                    performOnClient,
                    rewards,
                    conditions
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, RidePokemonIntervalAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
