package io.jaypixl.artipelagocore.questtasks;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.questtasks.block.BlockBreakTask;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Registers this mod's FTB Quests task types.
 *
 * <p>This must run on both sides before any quest file loads. FTB keeps task
 * types in a plain static map rather than a registry, the client parses the
 * same quest data and needs the type to construct the task, and registration of
 * a duplicate id is silently discarded instead of reported.
 */
public final class QuestTaskTypes {

    public static TaskType BLOCK_BREAK;

    private QuestTaskTypes() {
    }

    public static void register() {
        BLOCK_BREAK = register("block_break", BlockBreakTask::new, Items.IRON_PICKAXE);
    }

    private static TaskType register(String path, TaskType.Provider provider, Item icon) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ArtipelagoCoreMod.MOD_ID, path);
        TaskType type = TaskTypes.register(id, provider, () -> ItemIcon.getItemIcon(icon));
        type.setDisplayName(Component.translatable("ftbquests.task." + id.getNamespace() + "." + id.getPath()));
        return type;
    }
}
