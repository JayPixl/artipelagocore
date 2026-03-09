package io.jaypixl.artipelagocore.jobsplusintegration.level.block;

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.daqem.itemrestrictions.data.RestrictionType;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface ItemRestrictionsCookingPotBlockEntity {

    ServerPlayer itemrestrictions$getPlayer();

    void itemrestrictions$setPlayer(ServerPlayer player);

    UUID itemrestrictions$getPlayerUUID();

    void itemrestrictions$setPlayerUUID(UUID playerUUID);

    boolean itemrestrictions$getRestricted();

    void itemrestrictions$setRestricted(boolean bool);

    void itemrestrictions$sendPacketCantBrew(RestrictionType type, ItemRestrictionsCookingPotBlockEntity block);

    CampfireBlockEntity itemrestrictions$getCampfireBlockEntity();
}