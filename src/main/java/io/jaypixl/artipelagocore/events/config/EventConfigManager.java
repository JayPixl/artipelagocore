package io.jaypixl.artipelagocore.events.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EventConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("artipelago/events.json");
    private static EventConfig config = new EventConfig();

    private EventConfigManager() { }

    public static void load() {
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.notExists(PATH)) {
                Files.writeString(PATH, GSON.toJson(config));
                return;
            }
            EventConfig loaded = GSON.fromJson(Files.readString(PATH), EventConfig.class);
            config = loaded == null ? new EventConfig() : loaded;
            if (config.settings == null) config.settings = new EventConfig.Settings();
            if (config.presets == null) config.presets = java.util.List.of();
            if (config.schedules == null) config.schedules = java.util.List.of();
        } catch (IOException | RuntimeException exception) {
            ArtipelagoCoreMod.LOGGER.error("Could not load event configuration from {}", PATH, exception);
        }
    }

    public static EventConfig get() { return config; }

    public static EventConfig.Preset getPreset(String id) {
        return config.presets.stream().filter(preset -> preset.id.equals(id)).findFirst().orElse(null);
    }
}
