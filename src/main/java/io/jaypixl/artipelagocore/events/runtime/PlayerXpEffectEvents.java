package io.jaypixl.artipelagocore.events.runtime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jaypixl.artipelagocore.events.effect.EventEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Applies configured player experience bonuses at their originating game hooks. */
public final class PlayerXpEffectEvents {
    private static final Map<UUID, FishingBonus> FISHING_BONUSES = new HashMap<>();

    private PlayerXpEffectEvents() { }

    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getAmount() <= 0) return;
        event.setAmount(scale(event.getAmount(), multiplier(player, "all", null)));
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || event.getDroppedExperience() <= 0) return;
        event.setDroppedExperience(scale(event.getDroppedExperience(), multiplier(player, "break_block", event)));
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        double multiplier = multiplier(player, "fishing", null);
        if (multiplier != 1.0) {
            FISHING_BONUSES.put(player.getUUID(), new FishingBonus(player.level().getGameTime() + 1, player.getX(), player.getY(), player.getZ(), multiplier, event.getDrops().size()));
        }
    }

    @SubscribeEvent
    public static void onExperienceOrbJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ExperienceOrb orb) || event.getLevel().isClientSide()) return;
        FISHING_BONUSES.entrySet().removeIf(entry -> applyFishingBonus(entry, orb, event.getLevel().getGameTime()));
    }

    private static boolean applyFishingBonus(Map.Entry<UUID, FishingBonus> entry, ExperienceOrb orb, long gameTime) {
        FishingBonus bonus = entry.getValue();
        if (gameTime > bonus.expiresAt()) return true;
        double distanceSquared = orb.distanceToSqr(bonus.x(), bonus.y() + 0.5, bonus.z() + 0.5);
        if (distanceSquared > 4.0) return false;
        orb.value = scale(orb.value, bonus.multiplier());
        return --bonus.remainingOrbs <= 0;
    }

    private static double multiplier(ServerPlayer player, String context, BlockDropsEvent blockEvent) {
        double result = 1.0;
        for (EventEffect effect : EventManager.getActiveEffects(player.server, "player_xp_boost")) {
            if (effect.multiplier == null || !hasContext(effect.contexts, context, blockEvent)) continue;
            result *= effect.multiplier;
        }
        return result;
    }

    private static boolean hasContext(JsonElement contexts, String expectedType, BlockDropsEvent blockEvent) {
        if (contexts == null || !contexts.isJsonArray()) return false;
        for (JsonElement element : contexts.getAsJsonArray()) {
            if (!element.isJsonObject()) continue;
            JsonObject context = element.getAsJsonObject();
            if (!expectedType.equals(string(context, "type"))) continue;
            if (!"break_block".equals(expectedType) || matchesBlock(context, blockEvent)) return true;
        }
        return false;
    }

    private static boolean matchesBlock(JsonObject context, BlockDropsEvent event) {
        JsonElement blocks = context.get("blocks");
        if (blocks == null || !blocks.isJsonArray()) return false;
        Block block = event.getState().getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        for (JsonElement value : blocks.getAsJsonArray()) {
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id == null) continue;
            if (blockId.equals(id) || event.getState().is(TagKey.create(Registries.BLOCK, id))) return true;
        }
        return false;
    }

    private static String string(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null ? "" : value.getAsString();
    }

    private static int scale(int experience, double multiplier) {
        return (int) Math.clamp(Math.round(experience * multiplier), 0L, Integer.MAX_VALUE);
    }

    private static final class FishingBonus {
        private final long expiresAt;
        private final double x;
        private final double y;
        private final double z;
        private final double multiplier;
        private int remainingOrbs;

        private FishingBonus(long expiresAt, double x, double y, double z, double multiplier, int remainingOrbs) {
            this.expiresAt = expiresAt;
            this.x = x;
            this.y = y;
            this.z = z;
            this.multiplier = multiplier;
            this.remainingOrbs = remainingOrbs;
        }

        private long expiresAt() { return expiresAt; }
        private double x() { return x; }
        private double y() { return y; }
        private double z() { return z; }
        private double multiplier() { return multiplier; }
    }
}
