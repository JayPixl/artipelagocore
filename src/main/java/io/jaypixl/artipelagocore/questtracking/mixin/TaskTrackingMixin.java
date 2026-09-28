package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.task.Task;
import io.jaypixl.artipelagocore.questtracking.TaskTracking;
import io.jaypixl.artipelagocore.questtracking.TaskTrackingHolder;
import io.jaypixl.artipelagocore.questtracking.TrackingNameMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Task.class)
public abstract class TaskTrackingMixin implements TaskTrackingHolder {

    @Unique
    private TaskTracking artipelago$taskTracking = TaskTracking.INHERIT;

    @Override
    public TaskTracking artipelago$getTaskTracking() {
        return artipelago$taskTracking;
    }

    @Override
    public void artipelago$setTaskTracking(TaskTracking tracking) {
        artipelago$taskTracking = tracking == null ? TaskTracking.INHERIT : tracking;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void artipelago$writeTracking(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (artipelago$taskTracking != TaskTracking.INHERIT) {
            tag.putString("tracking", artipelago$taskTracking.wireValue());
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void artipelago$readTracking(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        artipelago$setTaskTracking(TaskTracking.parseTask(tag.getString("tracking")));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void artipelago$writeTrackingNet(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeEnum(artipelago$taskTracking);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void artipelago$readTrackingNet(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        artipelago$setTaskTracking(buf.readEnum(TaskTracking.class));
    }

    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    private void artipelago$addTrackingConfig(ConfigGroup config, CallbackInfo ci) {
        // Pin the display key: without this the label resolves through the
        // per-type subgroup path (ftbquests.task.<type>.tracking), needing a
        // lang entry for every present and future task type. One shared key
        // covers them all; tooltip follows as <key>.tooltip when defined.
        config.addEnum("tracking", artipelago$taskTracking, this::artipelago$setTaskTracking, TrackingNameMaps.TASK)
                .setNameKey("ftbquests.task.tracking");
    }
}
