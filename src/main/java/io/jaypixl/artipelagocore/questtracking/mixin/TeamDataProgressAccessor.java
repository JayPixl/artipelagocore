package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftbquests.quest.TeamData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads/writes the repeatable-quest state maps on {@code TeamData}.
 * FTB exposes no setter for the repeat cooldown or the completion count,
 * but Global repeatable quests record them claim-store-locally while the
 * quest book reads the member store — so the router must copy them across
 * on repeat-resets (see {@code propagateGlobalRepeatState}).
 */
@Mixin(TeamData.class)
public interface TeamDataProgressAccessor {

    @Accessor("questRepeatableTime")
    Long2LongMap artipelago$getRepeatableTime();

    @Accessor("completionCount")
    Long2IntMap artipelago$getCompletionCount();
}
