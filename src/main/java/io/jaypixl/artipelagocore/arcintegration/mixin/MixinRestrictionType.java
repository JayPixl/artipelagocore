package io.jaypixl.artipelagocore.arcintegration.mixin;

import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RestrictionType.class)
public abstract class MixinRestrictionType {

    // Allow replacing the enum's internal array
    @Shadow @Mutable
    private static RestrictionType[] $VALUES;

    // This exposes the enum's private constructor
    @Invoker("<init>")
    public static RestrictionType invokeInit(String internalName, int ordinal, String translationKey) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {

        RestrictionType interactBlock =
                invokeInit("INTERACT_BLOCK", $VALUES.length, "inventory.cant_interact_block");
        RestrictionType interactEntity =
                invokeInit("INTERACT_ENTITY", $VALUES.length + 1, "inventory.cant_interact_entity");

        RestrictionType[] newValues = new RestrictionType[$VALUES.length + 2];
        System.arraycopy($VALUES, 0, newValues, 0, $VALUES.length);
        newValues[$VALUES.length] = interactBlock;
        newValues[$VALUES.length + 1] = interactEntity;

        $VALUES = newValues;
    }
}
