package com.neryos.goatapples;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

/** The mod's runtime settings; every value is read live from the config file. */
public final class GoatApplesConfig {
    abstract static class Value {
        final String key;
        final String comment;

        Value(String key, String comment) {
            this.key = key;
            this.comment = comment;
        }

        /** Takes the file's value when it is one this setting allows. */
        abstract boolean read(JsonElement e);

        /** Goes back to the default and writes it into the file. */
        abstract void reset(JsonObject json);

        abstract String allowed();
    }

    public static final class IntValue extends Value {
        final int fallback;
        final int min;
        final int max;
        private volatile int value;

        IntValue(String key, int fallback, int min, int max, String comment) {
            super(key, comment);
            this.fallback = fallback;
            this.min = min;
            this.max = max;
            this.value = fallback;
        }

        public int get() {
            return value;
        }

        boolean read(JsonElement e) {
            if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) return false;
            value = Math.max(min, Math.min(max, e.getAsInt()));
            return true;
        }

        void reset(JsonObject json) {
            value = fallback;
            json.addProperty(key, fallback);
        }

        String allowed() {
            return "Range: " + min + " to " + max + ".";
        }
    }

    public static final class DoubleValue extends Value {
        final double fallback;
        final double min;
        final double max;
        private volatile double value;

        DoubleValue(String key, double fallback, double min, double max, String comment) {
            super(key, comment);
            this.fallback = fallback;
            this.min = min;
            this.max = max;
            this.value = fallback;
        }

        public double get() {
            return value;
        }

        boolean read(JsonElement e) {
            if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) return false;
            value = Math.max(min, Math.min(max, e.getAsDouble()));
            return true;
        }

        void reset(JsonObject json) {
            value = fallback;
            json.addProperty(key, fallback);
        }

        String allowed() {
            return "Range: " + min + " to " + max + ".";
        }
    }

    public static final class BoolValue extends Value {
        final boolean fallback;
        private volatile boolean value;

        BoolValue(String key, boolean fallback, String comment) {
            super(key, comment);
            this.fallback = fallback;
            this.value = fallback;
        }

        public boolean get() {
            return value;
        }

        boolean read(JsonElement e) {
            if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean()) return false;
            value = e.getAsBoolean();
            return true;
        }

        void reset(JsonObject json) {
            value = fallback;
            json.addProperty(key, fallback);
        }

        String allowed() {
            return "true or false.";
        }
    }

    public static final class WordValue extends Value {
        final String fallback;
        final List<String> words;
        private volatile String value;

        WordValue(String key, String fallback, List<String> words, String comment) {
            super(key, comment);
            this.fallback = fallback;
            this.words = words;
            this.value = fallback;
        }

        public String get() {
            return value;
        }

        boolean read(JsonElement e) {
            if (e == null || !e.isJsonPrimitive() || !words.contains(e.getAsString())) return false;
            value = e.getAsString();
            return true;
        }

        void reset(JsonObject json) {
            value = fallback;
            json.addProperty(key, fallback);
        }

        String allowed() {
            return "One of: " + String.join(", ", words) + ".";
        }
    }

    public static final BoolValue BAITED_ENABLED = new BoolValue("baited_enabled", true, "A goat that charges a player or mob, misses and hits a trunk may shake apples out.");
    public static final DoubleValue BAITED_SHAKE_CHANCE = new DoubleValue("baited_shake_chance", 0.6, 0.0, 1.0, "The chance a baited charge into a trunk shakes the tree.");
    public static final WordValue BAITED_HORN_MODE = new WordValue("baited_horn_mode", "vanilla", List.of("vanilla", "with_shake", "never"), "When a baited charge drops a horn: vanilla (always, as today), with_shake (only when the tree shakes) or never.");
    public static final BoolValue CHOSEN_ENABLED = new BoolValue("chosen_enabled", true, "A goat with nothing to ram may pick a nearby fruit tree and charge it.");
    public static final DoubleValue CHOSEN_PICK_CHANCE = new DoubleValue("chosen_pick_chance", 0.25, 0.0, 1.0, "The chance a goat with nothing to ram picks a tree instead.");
    public static final DoubleValue CHOSEN_SHAKE_CHANCE = new DoubleValue("chosen_shake_chance", 0.8, 0.0, 1.0, "The chance a charge at a chosen tree shakes it.");
    public static final DoubleValue CHOSEN_HORN_CHANCE = new DoubleValue("chosen_horn_chance", 0.0, 0.0, 1.0, "The chance a charge at a chosen tree drops a horn.");
    public static final IntValue CHOSEN_SEARCH_RADIUS = new IntValue("chosen_search_radius", 16, 4, 32, "How far, in blocks, a goat looks for a tree.");
    public static final IntValue SHAKE_MAX_LEAVES = new IntValue("shake_max_leaves", 3, 1, 16, "At most this many leaves drop something in one shake.");
    public static final DoubleValue SHAKE_GOLDEN_APPLE_CHANCE = new DoubleValue("shake_golden_apple_chance", 0.01, 0.0, 1.0, "The chance a leaf that drops fruit also drops a golden apple.");
    public static final IntValue TREE_MIN_FRUIT_LEAVES = new IntValue("tree_min_fruit_leaves", 5, 1, 64, "Leaves that bear fruit a tree needs above its trunk to count.");
    public static final BoolValue TREE_NATURAL_LEAVES_ONLY = new BoolValue("tree_natural_leaves_only", true, "Leaves a player placed do not count.");
    public static final IntValue TREE_REST_SECONDS = new IntValue("tree_rest_seconds", 300, 0, 86400, "Seconds a tree gives nothing after it dropped fruit.");
    public static final IntValue LIMITS_MAX_LOOSE_ITEMS_NEARBY = new IntValue("limits_max_loose_items_nearby", 12, 0, 256, "Nothing drops while more than this many items lie within 8 blocks.");

    private static final List<Value> VALUES = List.of(
        BAITED_ENABLED, BAITED_SHAKE_CHANCE, BAITED_HORN_MODE, CHOSEN_ENABLED, CHOSEN_PICK_CHANCE,
        CHOSEN_SHAKE_CHANCE, CHOSEN_HORN_CHANCE, CHOSEN_SEARCH_RADIUS, SHAKE_MAX_LEAVES, SHAKE_GOLDEN_APPLE_CHANCE,
        TREE_MIN_FRUIT_LEAVES, TREE_NATURAL_LEAVES_ONLY, TREE_REST_SECONDS, LIMITS_MAX_LOOSE_ITEMS_NEARBY
    );
    private static final String FILE = "goatapples-common.json";
    private static Path dir;

    /** Reads the file, writes what is missing, then watches the directory for edits. */
    public static void load(Path configDir) {
        dir = configDir;
        read();
        Thread thread = new Thread(GoatApplesConfig::watch, "goatapples-config");
        thread.setDaemon(true);
        thread.start();
    }

    private static synchronized void read() {
        Path path = dir.resolve(FILE);
        JsonObject json = new JsonObject();
        if (Files.isRegularFile(path)) {
            try {
                json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            } catch (Exception halfWritten) {
                return;
            }
        }
        boolean changed = false;
        for (Value v : VALUES) {
            if (!v.read(json.get(v.key))) {
                v.reset(json);
                changed = true;
            }
            if (!json.has("_" + v.key)) {
                json.addProperty("_" + v.key, (v.comment.isEmpty() ? "" : v.comment + " ") + v.allowed());
                changed = true;
            }
        }
        if (changed) {
            try {
                Files.createDirectories(dir);
                Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n");
            } catch (IOException ignored) {
            }
        }
    }

    private static void watch() {
        try (WatchService watcher = dir.getFileSystem().newWatchService()) {
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (FILE.equals(String.valueOf(event.context()))) read();
                }
                key.reset();
            }
        } catch (Exception ignored) {
        }
    }
}
