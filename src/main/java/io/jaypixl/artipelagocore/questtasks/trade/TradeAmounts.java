package io.jaypixl.artipelagocore.questtasks.trade;

import net.minecraft.core.registries.BuiltInRegistries;
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
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            List<String> tags = stack.getItem().builtInRegistryHolder().tags()
                    .map(tag -> tag.location().toString())
                    .toList();
            amounts.add(new TradeDelta.ItemAmount(id, stack.getCount(), tags));
        }
        return amounts;
    }
}
