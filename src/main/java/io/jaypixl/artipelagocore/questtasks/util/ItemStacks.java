package io.jaypixl.artipelagocore.questtasks.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Registry lookups shared by the item-filtered task types. */
public final class ItemStacks {

    private ItemStacks() {
    }

    /** Registry id of the stack's item, used by the pure item matchers. */
    public static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /** Tag ids containing the stack's item. */
    public static List<String> itemTagIds(ItemStack stack) {
        return stack.getItemHolder().tags()
                .map(tag -> tag.location().toString())
                .toList();
    }
}
