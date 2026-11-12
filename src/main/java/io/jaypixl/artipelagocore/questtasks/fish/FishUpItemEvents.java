package io.jaypixl.artipelagocore.questtasks.fish;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Advances every Fish Up Item task whose item and biome filters match a caught
 * drop.
 *
 * <p>Runs on the NeoForge common event, which both vanilla fishing and Tide 2
 * raise, so a Tide catch counts exactly like a vanilla one. Cancellations are
 * ignored and progress is always {@code +1} per catch, never the stack count.
 */
public final class FishUpItemEvents {

    private FishUpItemEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemFished(ItemFishedEvent event) {
        if (event.isCanceled()) {
            return;
        }
        FishingHook hook = event.getHookEntity();
        if (hook == null) {
            return;
        }
        Player owner = hook.getPlayerOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }

        Optional<ServerQuestFile> optionalFile = ServerQuestFile.getInstance();
        if (optionalFile.isEmpty()) {
            return;
        }
        ServerQuestFile file = optionalFile.get();
        List<FishUpItemTask> tasks = file.collect(FishUpItemTask.class);
        if (tasks.isEmpty()) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        LazyBiome biome = new LazyBiome(hook);
        TeamData data = null;
        for (FishUpItemTask task : tasks) {
            if (!task.matches(drops, biome)) {
                continue;
            }
            if (data == null) {
                data = file.getTeamData(player).orElse(null);
                if (data == null) {
                    return;
                }
            }
            task.submitCatch(data, player);
        }
    }

    /**
     * Resolves the bobber's biome at most once per catch, and only when a task
     * actually has the biome filter enabled.
     */
    private static final class LazyBiome implements Supplier<FishUpItemTask.BiomeContext> {

        private final FishingHook hook;
        private FishUpItemTask.BiomeContext context;

        private LazyBiome(FishingHook hook) {
            this.hook = hook;
        }

        @Override
        public FishUpItemTask.BiomeContext get() {
            if (context == null) {
                Holder<Biome> holder = hook.level().getBiome(hook.blockPosition());
                String id = holder.unwrapKey()
                        .map(ResourceKey::location)
                        .map(Object::toString)
                        .orElse("");
                List<String> tags = holder.tags()
                        .map(tag -> tag.location().toString())
                        .toList();
                context = new FishUpItemTask.BiomeContext(id, tags);
            }
            return context;
        }
    }
}
