package io.jaypixl.artipelagocore.item;

import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArtipelagoCoreMod.MOD_ID);

    public static final DeferredItem<Item> RAW_COPPER_CHUNK = ITEMS.registerItem("raw_copper_chunk", Item::new);
    public static final DeferredItem<Item> RAW_IRON_CHUNK = ITEMS.registerItem("raw_iron_chunk", Item::new);
    public static final DeferredItem<Item> RAW_GOLD_CHUNK = ITEMS.registerItem("raw_gold_chunk", Item::new);
    public static final DeferredItem<Item> LAPIS_SHARD = ITEMS.registerItem("lapis_shard", Item::new);
    public static final DeferredItem<Item> EMERALD_SHARD = ITEMS.registerItem("emerald_shard", Item::new);
    public static final DeferredItem<Item> DIAMOND_SHARD = ITEMS.registerItem("diamond_shard", Item::new);

    private static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RAW_COPPER_CHUNK.get());
            event.accept(ModItems.RAW_IRON_CHUNK.get());
            event.accept(ModItems.RAW_GOLD_CHUNK.get());
            event.accept(ModItems.LAPIS_SHARD.get());
            event.accept(ModItems.EMERALD_SHARD.get());
            event.accept(ModItems.DIAMOND_SHARD.get());
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItems::buildContents);
    }
}
