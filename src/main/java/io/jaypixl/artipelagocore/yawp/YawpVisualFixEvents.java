package io.jaypixl.artipelagocore.yawp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class YawpVisualFixEvents {

    private YawpVisualFixEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!e.isCanceled() || !(e.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        e.setCancellationResult(InteractionResult.FAIL);
        e.setUseBlock(TriState.FALSE);
        e.setUseItem(TriState.FALSE);
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        if (!e.isCanceled() || !(e.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        e.setCancellationResult(InteractionResult.FAIL);
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract e) {
        if (!e.isCanceled() || !(e.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        e.setCancellationResult(InteractionResult.FAIL);
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onInteractEntitySpecific(PlayerInteractEvent.EntityInteractSpecific e) {
        if (!e.isCanceled() || !(e.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        e.setCancellationResult(InteractionResult.FAIL);
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent e) {
        if (!e.isCanceled() || !(e.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        e.getBlockSnapshot().restore();
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }
}
