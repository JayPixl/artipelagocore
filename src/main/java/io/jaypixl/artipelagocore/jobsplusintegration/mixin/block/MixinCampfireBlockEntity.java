package io.jaypixl.artipelagocore.jobsplusintegration.mixin.block;

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import dev.architectury.networking.NetworkManager;
import io.jaypixl.artipelagocore.jobsplusintegration.level.block.ItemRestrictionsCookingPotBlockEntity;
import io.jaypixl.artipelagocore.jobsplusintegration.level.menu.ItemRestrictionsCookingPotMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(CampfireBlockEntity.class)
public abstract class MixinCampfireBlockEntity implements ItemRestrictionsCookingPotBlockEntity {

    @Shadow
    private int cookingProgress;

    @Unique
    @Nullable
    private UUID itemrestrictions$playerUUID;

    @Unique
    @Nullable
    private ServerPlayer itemrestrictions$player;

    @Unique
    private boolean itemrestrictions$isRestricted = false;

    @Unique
    public boolean itemrestrictions$getRestricted() {
        return itemrestrictions$isRestricted;
    }

    @Unique
    public void itemrestrictions$setRestricted(boolean bool) {
        itemrestrictions$isRestricted = bool;
    }

    @Inject(at = @At("TAIL"), method = "saveAdditional")
    private void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        ServerPlayer serverPlayer = itemrestrictions$getPlayer();
        if (serverPlayer != null) {
            compoundTag.putString("ItemRestrictionsServerPlayer", serverPlayer.getUUID().toString());
        } else {
            UUID uuid = itemrestrictions$getPlayerUUID();
            if (uuid != null) {
                compoundTag.putString("ItemRestrictionsServerPlayer", uuid.toString());
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "loadAdditional")
    private void load(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (compoundTag.contains("ItemRestrictionsServerPlayer")) {
            itemrestrictions$setPlayerUUID(UUID.fromString(compoundTag.getString("ItemRestrictionsServerPlayer")));
        }
    }

    @Unique
    public void itemrestrictions$sendPacketCantBrew(RestrictionType type, ItemRestrictionsCookingPotBlockEntity block) {
        if (block.itemrestrictions$getPlayer().containerMenu instanceof ItemRestrictionsCookingPotMenu menu) {
            if (menu.itemrestrictions$getCookingPot().equals(block.itemrestrictions$getCampfireBlockEntity())) {
                NetworkManager.sendToPlayer(block.itemrestrictions$getPlayer(), new ClientboundRestrictionPacket(type));
            }
        }
    }

    @Override
    public ServerPlayer itemrestrictions$getPlayer() {
        return itemrestrictions$player;
    }

    @Override
    public void itemrestrictions$setPlayer(ServerPlayer player) {
        this.itemrestrictions$player = player;
    }

    @Override
    public UUID itemrestrictions$getPlayerUUID() {
        return itemrestrictions$playerUUID;
    }

    @Override
    public void itemrestrictions$setPlayerUUID(UUID playerUUID) {
        this.itemrestrictions$playerUUID = playerUUID;
    }

    @Override
    @Nullable
    public CampfireBlockEntity itemrestrictions$getCampfireBlockEntity() {
        return (CampfireBlockEntity) (Object) this;
    }
}
