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

public class PokemonExpContextCondition extends AbstractCondition {
    private final Context context;

    public PokemonExpContextCondition(boolean inverted, Context context) {
        super(inverted);
        this.context = context;
    }

    @Override
    public IConditionType<? extends PokemonExpContextCondition> getType() {
        return ACConditionTypes.POKEMON_EXP_CONTEXT;
    }

    @Override
    public boolean isMet(ActionData actionData) {
        return switch (context) {
            case BATTLE -> Boolean.TRUE.equals(actionData.getData(ACActionDataTypes.EXP_GAINED_IN_BATTLE));
            case CANDY -> Boolean.TRUE.equals(actionData.getData(ACActionDataTypes.EXP_GAINED_FROM_CANDY));
        };
    }

    public enum Context {
        BATTLE,
        CANDY;

        public static Context fromSerializedName(String value) {
            return switch (value.toLowerCase()) {
                case "battle" -> BATTLE;
                case "candy", "exp_candy" -> CANDY;
                default -> throw new IllegalArgumentException("Unknown exp context: " + value);
            };
        }
    }

    public static class Serializer implements IConditionSerializer<PokemonExpContextCondition> {

        @Override
        public PokemonExpContextCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new PokemonExpContextCondition(
                    inverted,
                    Context.fromSerializedName(GsonHelper.getAsString(jsonObject, "context"))
            );
        }

        @Override
        public PokemonExpContextCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf buf, boolean inverted) {
            return new PokemonExpContextCondition(
                    inverted,
                    buf.readEnum(Context.class)
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonExpContextCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeEnum(type.context);
        }
    }
}
