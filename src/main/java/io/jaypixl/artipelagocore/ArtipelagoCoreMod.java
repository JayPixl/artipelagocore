package io.jaypixl.artipelagocore;

import com.mojang.logging.LogUtils;
import io.jaypixl.artipelagocore.item.ModItems;
import io.jaypixl.artipelagocore.regionmarket.RegionMarketCommands;
import io.jaypixl.artipelagocore.regionmarket.RegionMarketEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod("artipelagocore")
public final class ArtipelagoCoreMod {

    public static final String MOD_ID = "artipelagocore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArtipelagoCoreMod(IEventBus eventBus) {
        NeoForge.EVENT_BUS.register(ArtipelagoCoreMod.class);
        NeoForge.EVENT_BUS.register(RegionMarketEvents.class);

        ModItems.register(eventBus);
    }

    @SubscribeEvent
    public static void onCommandRegistration(final RegisterCommandsEvent event) {
        RegionMarketCommands.register(event.getDispatcher());
    }

}
