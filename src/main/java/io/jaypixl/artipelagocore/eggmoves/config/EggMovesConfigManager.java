package io.jaypixl.artipelagocore.eggmoves.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EggMovesConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("artipelago/eggmoves.json");
    private static EggMovesConfig config = new EggMovesConfig();

    private EggMovesConfigManager() { }

    public static void load() {
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.notExists(PATH)) {
                config = new EggMovesConfig();
                Files.writeString(PATH, GSON.toJson(config));
                return;
            }
            EggMovesConfig loaded = GSON.fromJson(Files.readString(PATH), EggMovesConfig.class);
            config = loaded == null ? new EggMovesConfig() : loaded;
            List<String> corrections = config.clamped();
            for (String correction : corrections) {
                ArtipelagoCoreMod.LOGGER.warn("Egg moves config clamped: {}", correction);
            }
        } catch (IOException | RuntimeException exception) {
            ArtipelagoCoreMod.LOGGER.error("Could not load egg move configuration from {}", PATH, exception);
        }
    }

    public static EggMovesConfig get() { return config; }
}
