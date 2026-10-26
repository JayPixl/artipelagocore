package io.jaypixl.artipelagocore.pokecapsuleintegration.mixin;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.pokecapsule.event.PlaceBallHandler;
import com.pokecapsule.util.BallNbt;
import de.z0rdak.yawp.api.FlagEvaluator;
import de.z0rdak.yawp.api.events.flag.FlagCheckRequest;
import de.z0rdak.yawp.core.flag.RegionFlag;
import de.z0rdak.yawp.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps occupied Poké Ball placement inside YAWP placement rules.
 *
 * <p>Cobblemon Occupied Pokéballs 1.3.1 performs the placement inline in
 * {@code onUseBlock}, which is invoked for every block right-click. The preconditions below
 * mirror the upstream ones so a denied placement cannot swallow unrelated interactions.
 */
@Mixin(PlaceBallHandler.class)
public class PlaceBallHandlerMixin {

    @Inject(method = "onUseBlock",
            at = @At("HEAD"),
            cancellable = true)
    private void artipelago$denyOccupiedBallPlacement(Player player, Level level, InteractionHand hand,
                                                      BlockHitResult hitResult,
                                                      CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof PokeBallItem) || !BallNbt.INSTANCE.hasPokemon(stack)) {
            return;
        }

        if (hitResult.getDirection() != Direction.UP) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockPos hitPos = hitResult.getBlockPos();
        BlockPos targetPos = hitPos.above();

        FlagCheckRequest flagCheck = new FlagCheckRequest(targetPos, RegionFlag.PLACE_BLOCKS, level.dimension(), serverPlayer);
        if (Services.FLAG_EVENT_DISPATCHER.post(flagCheck)) {
            return;
        }

        boolean[] denied = {false};
        FlagEvaluator.process(flagCheck).onDenyWithMsg(result -> denied[0] = true);
        if (!denied[0]) {
            return;
        }

        serverPlayer.inventoryMenu.sendAllDataToRemote();
        level.sendBlockUpdated(hitPos, level.getBlockState(hitPos), level.getBlockState(hitPos), 3);
        level.sendBlockUpdated(targetPos, level.getBlockState(targetPos), level.getBlockState(targetPos), 3);
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
