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

public class PokemonEvContextCondition extends AbstractCondition {
    private final Context context;

    public PokemonEvContextCondition(boolean inverted, Context context) {
        super(inverted);
        this.context = context;
    }

    @Override
    public IConditionType<? extends PokemonEvContextCondition> getType() {
        return ACConditionTypes.POKEMON_EV_CONTEXT;
    }

    @Override
    public boolean isMet(ActionData actionData) {
        String source = actionData.getData(ACActionDataTypes.EV_GAIN_SOURCE);
        if (source == null) {
            return false;
        }
        return switch (context) {
            case BATTLE -> "battle".equals(source);
            case VITAMIN -> "vitamin".equals(source);
        };
    }

    public enum Context {
        BATTLE,
        VITAMIN;

        public static Context fromSerializedName(String value) {
            return switch (value.toLowerCase()) {
                case "battle" -> BATTLE;
                case "vitamin" -> VITAMIN;
                default -> throw new IllegalArgumentException("Unknown ev context: " + value);
            };
        }
    }

    public static class Serializer implements IConditionSerializer<PokemonEvContextCondition> {

        @Override
        public PokemonEvContextCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new PokemonEvContextCondition(
                    inverted,
                    Context.fromSerializedName(GsonHelper.getAsString(jsonObject, "context"))
            );
        }

        @Override
        public PokemonEvContextCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf buf, boolean inverted) {
            return new PokemonEvContextCondition(
                    inverted,
                    buf.readEnum(Context.class)
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PokemonEvContextCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeEnum(type.context);
        }
    }
}
