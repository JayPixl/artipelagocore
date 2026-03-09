package io.jaypixl.artipelagocore.jobsplusintegration.mixin.menu;

import com.cobblemon.mod.common.block.campfirepot.CookingPotMenu;
import io.jaypixl.artipelagocore.jobsplusintegration.level.menu.ItemRestrictionsCookingPotMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CookingPotMenu.class)
public abstract class MixinCookingPotMenu extends AbstractContainerMenu implements ItemRestrictionsCookingPotMenu {

    @Shadow @Final
    private CraftingContainer container;

    protected MixinCookingPotMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public CraftingContainer itemrestrictions$getCookingPot() {
        return container;
    }
}