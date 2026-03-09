package io.jaypixl.artipelagocore.jobsplusintegration.mixin.block;

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CampfireBlockEntity.class)
public interface CampfireAccessor {

    @Accessor("cookingProgress")
    void itemrestrictions$setCookingProgress(int value);

}