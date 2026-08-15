package io.jaypixl.artipelagocore.pokecapsuleintegration.mixin;

import com.pokecapsule.event.PlaceBallHandler;
import de.z0rdak.yawp.api.FlagEvaluator;
import de.z0rdak.yawp.api.events.flag.FlagCheckRequest;
import de.z0rdak.yawp.core.flag.RegionFlag;
import de.z0rdak.yawp.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlaceBallHandler.class)
public class PlaceBallHandlerMixin {
    @Inject(method = "placeBlock",
            at = @At("HEAD"),
            cancellable = true)
    private void onPlaceBlock(Level level, BlockPos pos, BlockPos targetPos, ServerPlayer player, ItemStack stack, CallbackInfo ci) {
        FlagCheckRequest flagCheck = new FlagCheckRequest(targetPos, RegionFlag.PLACE_BLOCKS, level.dimension(), player);
        if (Services.FLAG_EVENT_DISPATCHER.post(flagCheck)) {
            return;
        }

        boolean[] denied = {false};
        FlagEvaluator.process(flagCheck).onDenyWithMsg(result -> denied[0] = true);
        if (denied[0]) {
            player.inventoryMenu.sendAllDataToRemote();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            level.sendBlockUpdated(targetPos, level.getBlockState(targetPos), level.getBlockState(targetPos), 3);
            ci.cancel();
        }
    }
}
