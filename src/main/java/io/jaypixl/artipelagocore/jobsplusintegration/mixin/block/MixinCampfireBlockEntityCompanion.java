package io.jaypixl.artipelagocore.jobsplusintegration.mixin.block;

import com.cobblemon.mod.common.CobblemonRecipeTypes;
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.cobblemon.mod.common.item.crafting.CookingPotRecipe;
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase;
import com.cobblemon.mod.common.item.crafting.CookingPotShapelessRecipe;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import io.jaypixl.artipelagocore.jobsplusintegration.level.block.ItemRestrictionsCookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.Companion.class)
public class MixinCampfireBlockEntityCompanion {
    @Inject(
            method = "serverTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void itemrestrictions$cancelCooking(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity blockEntity,
            CallbackInfo ci
    ) {
        if (level.isClientSide()) return;

        ItemRestrictionsCookingPotBlockEntity cookingPot = (ItemRestrictionsCookingPotBlockEntity) (Object) blockEntity;

        if (cookingPot.itemrestrictions$getPlayer() == null && cookingPot.itemrestrictions$getPlayerUUID() != null && level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(cookingPot.itemrestrictions$getPlayerUUID());
            cookingPot.itemrestrictions$setPlayer(player);

            //CobbledArcMod.LOGGER.info("LOGGED PLAYER" + cookingPot.itemrestrictions$getPlayerUUID());
        }

        if (cookingPot.itemrestrictions$getPlayer() != null) {
            CraftingInput craftingInput = CraftingInput.of(3, 3, blockEntity.getItems().subList(1, 10));
            //CobbledArcMod.LOGGER.info("Crafting Input Made");

            CookingPotRecipeBase foundRecipe = null;

            for (RecipeHolder<CookingPotRecipe> recipe :
                    level.getRecipeManager().getAllRecipesFor(CobblemonRecipeTypes.INSTANCE.getCOOKING_POT_COOKING())) {

                if (recipe.value().matches(craftingInput, level)) {
                    //CobbledArcMod.LOGGER.info("Matched shaped recipe: " + recipe.id());
                    foundRecipe = recipe.value();
                }
            }

            for (RecipeHolder<CookingPotShapelessRecipe> recipe :
                    level.getRecipeManager().getAllRecipesFor(CobblemonRecipeTypes.INSTANCE.getCOOKING_POT_SHAPELESS())) {

                if (recipe.value().matches(craftingInput, level)) {
                    //CobbledArcMod.LOGGER.info("Matched shapeless recipe: " + recipe.id());
                    foundRecipe = recipe.value();
                }
            }

            if (foundRecipe != null) {
                //CobbledArcMod.LOGGER.info("RECIPE PRESENT: " + foundRecipe.getResult().toString());
                RestrictionResult result = new RestrictionResult();
                ItemStack resultStack = foundRecipe.assemble(craftingInput, level.registryAccess());
                if (cookingPot.itemrestrictions$getPlayer() instanceof ItemRestrictionsServerPlayer player) {
                    if (player instanceof ArcPlayer arcPlayer && ((ServerPlayer) player).getServer() != null) {
                        //CobbledArcMod.LOGGER.info("GETTING RESULT!");
                        result = player.itemrestrictions$isRestricted(
                                new ActionDataBuilder(arcPlayer, null)
                                        .withData(ActionDataType.ITEM_STACK, resultStack)
                                        .build());
                        /*result.getRestrictedBy().forEach(v -> {
                            CobbledArcMod.LOGGER.info(v.name());
                        });*/
                    }
                }
                if (result.isRestricted(RestrictionType.BREW)) {
                    //CobbledArcMod.LOGGER.info("IS RESTRICTED!");
                    ((CampfireAccessor) (Object) blockEntity).itemrestrictions$setCookingProgress(0);
                    blockEntity.setChanged();
                    ci.cancel();

                    cookingPot.itemrestrictions$sendPacketCantBrew(RestrictionType.BREW, cookingPot);
                    cookingPot.itemrestrictions$setRestricted(true);
                }
            } else {
                if (cookingPot.itemrestrictions$getRestricted()) {
                    cookingPot.itemrestrictions$sendPacketCantBrew(RestrictionType.NONE, cookingPot);
                    cookingPot.itemrestrictions$setRestricted(false);
                }
            }
        }
    }
}
