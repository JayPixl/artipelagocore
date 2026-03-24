package io.jaypixl.artipelagocore.arcintegration.condition;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.arcintegration.action.ACActionDataTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class BattleContextCondition extends AbstractCondition {
    private final Context context;

    public BattleContextCondition(boolean inverted, Context context) {
        super(inverted);
        this.context = context;
    }

    @Override
    public IConditionType<? extends BattleContextCondition> getType() {
        return ACConditionTypes.BATTLE_CONTEXT;
    }

    @Override
    public boolean isMet(ActionData actionData) {
        String ctx = actionData.getData(ACActionDataTypes.BATTLE_CONTEXT);
        if (ctx == null) return false;
        return context.serialized.equalsIgnoreCase(ctx);
    }

    public enum Context {
        PVP("pvp"),
        PVE("pve"),
        PVN("pvn");

        final String serialized;
        Context(String s) { this.serialized = s; }

        public static Context fromSerializedName(String value) {
            return switch (value.toLowerCase()) {
                case "pvp" -> PVP;
                case "pve", "wild" -> PVE;
                case "pvn", "npc" -> PVN;
                default -> throw new IllegalArgumentException("Unknown battle context: " + value);
            };
        }
    }

    public static class Serializer implements IConditionSerializer<BattleContextCondition> {

        @Override
        public BattleContextCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new BattleContextCondition(
                    inverted,
                    Context.fromSerializedName(GsonHelper.getAsString(jsonObject, "context"))
            );
        }

        @Override
        public BattleContextCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf buf, boolean inverted) {
            return new BattleContextCondition(
                    inverted,
                    buf.readEnum(Context.class)
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, BattleContextCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeEnum(type.context);
        }
    }
}
