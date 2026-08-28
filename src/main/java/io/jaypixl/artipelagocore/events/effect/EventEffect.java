package io.jaypixl.artipelagocore.events.effect;

import com.google.gson.JsonElement;
import java.util.List;

/** One of the effect objects accepted directly in {@code events.json}. */
public final class EventEffect {
    public String type = "";
    public List<PokemonTarget> targets = List.of();
    /** Context shape varies by effect type (spawn, player XP, Pokémon XP/EV, or loot). */
    public JsonElement contexts;
    public String operation;
    public Double value;
    public Double chance;
    public String move;
    public List<Spawn> spawns = List.of();
    public Double multiplier;
    public List<BucketAssignment> buckets = List.of();
    public JsonElement items;

    public static final class PokemonTarget { public String species = "all"; public List<String> aspects = List.of(); }
    /** Native-style Cobblemon spawn detail, limited to the event schema's supported fields. */
    public static final class Spawn {
        public String id = "";
        public String pokemon = "";
        public String context = "grounded";
        public String bucket = "common";
        public double weight;
        public String level;
        public JsonElement condition;
        public JsonElement anticondition;
        public List<String> aspects = List.of();
        public List<String> forms = List.of();
        public List<HeldItem> heldItems = List.of();
    }
    public static final class HeldItem { public String item = ""; public double percentage; }
    public static final class BucketAssignment { public String bucket = "common"; public double weight; }
}
