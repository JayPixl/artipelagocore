package io.jaypixl.artipelagocore.jobsplusintegration.mixin.block;

//import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import io.jaypixl.artipelagocore.jobsplusintegration.level.block.ItemRestrictionsCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseContainerBlockEntity.class)
public abstract class MixinBaseContainerBlockEntity extends BlockEntity {

    public MixinBaseContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(at = @At("TAIL"), method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z")
    private void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (this instanceof ItemRestrictionsCookingPotBlockEntity cookingPot) {
                if (cookingPot.itemrestrictions$getPlayer() != serverPlayer) {
                    cookingPot.itemrestrictions$setPlayer(serverPlayer);
                    cookingPot.itemrestrictions$setPlayerUUID(serverPlayer.getUUID());
                    CampfireBlockEntity campfire = cookingPot.itemrestrictions$getCampfireBlockEntity();
                    if (campfire != null) {
                        campfire.saveWithFullMetadata(serverPlayer.level().registryAccess());
                    }
                }
            }
        }
    }
}
