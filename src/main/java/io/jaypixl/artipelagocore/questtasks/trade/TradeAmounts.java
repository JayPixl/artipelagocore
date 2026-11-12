package io.jaypixl.artipelagocore.questtasks.trade;

import io.jaypixl.artipelagocore.questtasks.util.ItemStacks;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Reduces item stacks to the pure {@link TradeDelta.ItemAmount} form. */
public final class TradeAmounts {

    private TradeAmounts() {
    }

    public static List<TradeDelta.ItemAmount> of(Collection<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<TradeDelta.ItemAmount> amounts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            amounts.add(new TradeDelta.ItemAmount(
                    ItemStacks.itemId(stack),
                    stack.getCount(),
                    ItemStacks.itemTagIds(stack)));
        }
        return amounts;
    }
}
