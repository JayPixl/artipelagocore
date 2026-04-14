package io.jaypixl.artipelagocore.yawp;

import de.z0rdak.yawp.api.FlagEvaluator;
import de.z0rdak.yawp.api.events.region.FlagCheckEvent;
import de.z0rdak.yawp.core.flag.RegionFlag;
import de.z0rdak.yawp.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class YawpCompatHelper {

    private YawpCompatHelper() {
    }

    public static InteractionResultHolder<ItemStack> denyFluidPlacementIfRestricted(
            Level level,
            Player player,
            InteractionHand hand,
            BlockPos fluidPos,
            BlockPos placedPos
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return null;
        }

        FlagCheckEvent flagCheck = new FlagCheckEvent(placedPos, RegionFlag.PLACE_BLOCKS, level.dimension(), serverPlayer);
        if (Services.EVENT.post(flagCheck)) {
            return null;
        }

        boolean[] denied = {false};
        FlagEvaluator.process(flagCheck).onDenyWithMsg(result -> denied[0] = true);
        if (!denied[0]) {
            return null;
        }

        serverPlayer.inventoryMenu.sendAllDataToRemote();
        level.sendBlockUpdated(fluidPos, level.getBlockState(fluidPos), level.getBlockState(fluidPos), 3);
        level.sendBlockUpdated(placedPos, level.getBlockState(placedPos), level.getBlockState(placedPos), 3);
        return InteractionResultHolder.fail(serverPlayer.getItemInHand(hand));
    }
}
