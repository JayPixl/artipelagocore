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

public class CatchWildPokemonAction extends AbstractAction {
    public CatchWildPokemonAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ACActionTypes.ON_CATCH_WILD_POKEMON;
    }

    public static class Serializer implements IActionSerializer<CatchWildPokemonAction> {

        @Override
        public CatchWildPokemonAction fromJson(ResourceLocation location,
                                               JsonObject jsonObject,
                                               ResourceLocation actionHolderLocation,
                                               IActionHolderType<?> actionHolderType,
                                               boolean performOnClient,
                                               List<IReward> rewards,
                                               List<ICondition> conditions) {
            return new CatchWildPokemonAction(
                    location,
                    actionHolderLocation,
                    actionHolderType,
                    performOnClient,
                    rewards,
                    conditions
            );
        }

        @Override
        public CatchWildPokemonAction fromNetwork(ResourceLocation location,
                                                  RegistryFriendlyByteBuf buf,
                                                  ResourceLocation actionHolderLocation,
                                                  IActionHolderType<?> actionHolderType,
                                                  boolean performOnClient,
                                                  List<IReward> rewards,
                                                  List<ICondition> conditions) {
            return new CatchWildPokemonAction(
                    location,
                    actionHolderLocation,
                    actionHolderType,
                    performOnClient,
                    rewards,
                    conditions
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CatchWildPokemonAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
