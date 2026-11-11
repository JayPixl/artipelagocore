package io.jaypixl.artipelagocore.questtasks.mixin;

import io.github.lightman314.lightmanscurrency.common.playertrading.PlayerTrade;
import io.jaypixl.artipelagocore.questtasks.trade.PlayerTradeHandler;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Credits items moved by a completed {@code /lctrade} player trade.
 *
 * <p>{@code PlayerTrade} posts no event of any kind, so the only observation
 * point is the return of its private {@code tryExecuteTrade}. The trade
 * containers are still populated there because the completed-trade menu
 * deliberately skips clearing them, which is also why the {@link Unique}
 * guard is mandatory rather than defensive: late confirmation packets can
 * re-enter {@code tryExecuteTrade} after completion and would otherwise credit
 * the same trade twice.
 */
@Mixin(PlayerTrade.class)
public abstract class PlayerTradeMixin {

    @Shadow
    private boolean completed;

    @Shadow
    @Final
    private SimpleContainer hostItems;

    @Shadow
    @Final
    private SimpleContainer guestItems;

    @Unique
    private boolean artipelago$credited;

    @Shadow
    public abstract UUID getHostID();

    @Shadow
    public abstract UUID getGuestID();

    @Inject(method = "tryExecuteTrade", at = @At("RETURN"))
    private void artipelago$creditCompletedTrade(CallbackInfo ci) {
        if (!completed || artipelago$credited) {
            return;
        }
        artipelago$credited = true;
        PlayerTradeHandler.onCompletedTrade(
                getHostID(),
                getGuestID(),
                PlayerTradeHandler.snapshot(hostItems),
                PlayerTradeHandler.snapshot(guestItems));
    }
}
