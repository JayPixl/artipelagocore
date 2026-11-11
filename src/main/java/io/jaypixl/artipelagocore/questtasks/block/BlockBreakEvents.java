package io.jaypixl.artipelagocore.questtasks.block;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Advances every Break Block task whose filter matches a player's block break.
 *
 * <p>Only hand and tool breaks count: cancellations are ignored, creative mode
 * and fake players are skipped, and explosions or pistons never reach this
 * event. Progress is submitted per task so each task resolves its own Tracking
 * Scope store.
 */
public final class BlockBreakEvents {

    private BlockBreakEvents() {
    }

    /**
     * Runs at lowest priority so a protection or claim mod that cancels the
     * break later in the chain is still observed as cancelled: a normal-priority
     * listener can only see cancellations that already happened.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isCreative() || player instanceof FakePlayer) {
            return;
        }

        Optional<ServerQuestFile> optionalFile = ServerQuestFile.getInstance();
        if (optionalFile.isEmpty()) {
            return;
        }
        ServerQuestFile file = optionalFile.get();

        List<BlockBreakTask> tasks = file.collect(BlockBreakTask.class);
        if (tasks.isEmpty()) {
            return;
        }

        BlockState state = event.getState();
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockKey == null) {
            return;
        }
        String blockId = blockKey.toString();

        LazyBlockTags blockTags = new LazyBlockTags(state);
        TeamData data = null;
        for (BlockBreakTask task : tasks) {
            if (!task.hasFilter() || !task.matches(blockId, blockTags)) {
                continue;
            }
            if (data == null) {
                data = file.getTeamData(player).orElse(null);
                if (data == null) {
                    return;
                }
            }
            task.submitTask(data, player, ItemStack.EMPTY);
        }
    }

    /**
     * Resolves the broken block's tag ids at most once per break, and only when
     * a tag-filtered task actually asks. Reading tags for every break would add
     * a per-break allocation for servers that only use id filters.
     */
    private static final class LazyBlockTags implements Supplier<Collection<String>> {

        private final BlockState state;
        private List<String> tags;

        private LazyBlockTags(BlockState state) {
            this.state = state;
        }

        @Override
        public List<String> get() {
            if (tags == null) {
                tags = state.getTags().map(tag -> tag.location().toString()).toList();
            }
            return tags;
        }
    }
}
