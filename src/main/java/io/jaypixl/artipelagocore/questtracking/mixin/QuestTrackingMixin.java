package io.jaypixl.artipelagocore.questtracking.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import io.jaypixl.artipelagocore.questtracking.QuestTrackingHolder;
import io.jaypixl.artipelagocore.questtracking.TrackingNameMaps;
import io.jaypixl.artipelagocore.questtracking.TrackingScope;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Quest.class)
public abstract class QuestTrackingMixin implements QuestTrackingHolder {

    @Unique
    private TrackingScope artipelago$trackingScope = TrackingScope.TEAM;

    @Override
    public TrackingScope artipelago$getTrackingScope() {
        return artipelago$trackingScope;
    }

    @Override
    public void artipelago$setTrackingScope(TrackingScope scope) {
        artipelago$trackingScope = scope == null ? TrackingScope.TEAM : scope;
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void artipelago$writeTracking(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (artipelago$trackingScope != TrackingScope.TEAM) {
            tag.putString("tracking", artipelago$trackingScope.wireValue());
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void artipelago$readTracking(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {
        artipelago$setTrackingScope(TrackingScope.parseQuest(tag.getString("tracking")));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void artipelago$writeTrackingNet(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeEnum(artipelago$trackingScope);
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void artipelago$readTrackingNet(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        artipelago$setTrackingScope(buf.readEnum(TrackingScope.class));
    }

    @Inject(method = "fillConfigGroup", at = @At("TAIL"))
    private void artipelago$addTrackingConfig(ConfigGroup config, CallbackInfo ci) {
        config.addEnum("tracking", artipelago$trackingScope, this::artipelago$setTrackingScope, TrackingNameMaps.SCOPE);
    }
}
