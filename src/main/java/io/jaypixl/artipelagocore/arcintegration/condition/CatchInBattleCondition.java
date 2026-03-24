package io.jaypixl.artipelagocore.arcintegration.condition;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionDataTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CatchInBattleCondition extends AbstractCondition {
    public CatchInBattleCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public IConditionType<? extends CatchInBattleCondition> getType() {
        return ACConditionTypes.CATCH_IN_BATTLE;
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Boolean inBattle = actionData.getData(ACActionDataTypes.CATCH_IN_BATTLE);
        return Boolean.TRUE.equals(inBattle);
    }

    public static class Serializer implements IConditionSerializer<CatchInBattleCondition> {

        @Override
        public CatchInBattleCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new CatchInBattleCondition(inverted);
        }

        @Override
        public CatchInBattleCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf buf, boolean inverted) {
            return new CatchInBattleCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CatchInBattleCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
