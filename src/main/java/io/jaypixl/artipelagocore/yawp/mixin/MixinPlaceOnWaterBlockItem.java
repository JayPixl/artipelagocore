package io.jaypixl.artipelagocore.yawp.mixin;

import io.jaypixl.artipelagocore.yawp.YawpCompatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlaceOnWaterBlockItem.class)
public abstract class MixinPlaceOnWaterBlockItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void artipelago$denyFluidPlacement(Level world, Player user, InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        BlockHitResult hitResult = PlaceOnWaterBlockItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos fluidPos = hitResult.getBlockPos();
        if (world.getFluidState(fluidPos).isEmpty()) {
            return;
        }

        InteractionResultHolder<ItemStack> denied = YawpCompatHelper.denyFluidPlacementIfRestricted(
                world,
                user,
                hand,
                fluidPos,
                fluidPos.above()
        );
        if (denied != null) {
            cir.setReturnValue(denied);
        }
    }
}
