package com.neryos.goatapples;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The mod's runtime settings; every value is read live from the config file. */
public final class GoatApplesConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue BAITED_ENABLED;
    public static final ModConfigSpec.DoubleValue BAITED_SHAKE_CHANCE;
    public static final ModConfigSpec.ConfigValue<String> BAITED_HORN_MODE;
    public static final ModConfigSpec.BooleanValue CHOSEN_ENABLED;
    public static final ModConfigSpec.DoubleValue CHOSEN_PICK_CHANCE;
    public static final ModConfigSpec.DoubleValue CHOSEN_SHAKE_CHANCE;
    public static final ModConfigSpec.DoubleValue CHOSEN_HORN_CHANCE;
    public static final ModConfigSpec.IntValue CHOSEN_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue SHAKE_MAX_LEAVES;
    public static final ModConfigSpec.DoubleValue SHAKE_GOLDEN_APPLE_CHANCE;
    public static final ModConfigSpec.IntValue TREE_MIN_FRUIT_LEAVES;
    public static final ModConfigSpec.BooleanValue TREE_NATURAL_LEAVES_ONLY;
    public static final ModConfigSpec.IntValue TREE_REST_SECONDS;
    public static final ModConfigSpec.IntValue LIMITS_MAX_LOOSE_ITEMS_NEARBY;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        BAITED_ENABLED = common.comment("A goat that charges a player or mob, misses and hits a trunk may shake apples out.").translation("goatapples.configuration.baited_enabled").define("baited_enabled", true);
        BAITED_SHAKE_CHANCE = common.comment("The chance a baited charge into a trunk shakes the tree.").translation("goatapples.configuration.baited_shake_chance").defineInRange("baited_shake_chance", 0.6, 0.0, 1.0);
        BAITED_HORN_MODE = common.comment("When a baited charge drops a horn: vanilla (always, as today), with_shake (only when the tree shakes) or never.").translation("goatapples.configuration.baited_horn_mode").defineInList("baited_horn_mode", "vanilla", java.util.Arrays.asList("vanilla", "with_shake", "never"));
        CHOSEN_ENABLED = common.comment("A goat with nothing to ram may pick a nearby fruit tree and charge it.").translation("goatapples.configuration.chosen_enabled").define("chosen_enabled", true);
        CHOSEN_PICK_CHANCE = common.comment("The chance a goat with nothing to ram picks a tree instead.").translation("goatapples.configuration.chosen_pick_chance").defineInRange("chosen_pick_chance", 0.25, 0.0, 1.0);
        CHOSEN_SHAKE_CHANCE = common.comment("The chance a charge at a chosen tree shakes it.").translation("goatapples.configuration.chosen_shake_chance").defineInRange("chosen_shake_chance", 0.8, 0.0, 1.0);
        CHOSEN_HORN_CHANCE = common.comment("The chance a charge at a chosen tree drops a horn.").translation("goatapples.configuration.chosen_horn_chance").defineInRange("chosen_horn_chance", 0.0, 0.0, 1.0);
        CHOSEN_SEARCH_RADIUS = common.comment("How far, in blocks, a goat looks for a tree.").translation("goatapples.configuration.chosen_search_radius").defineInRange("chosen_search_radius", 16, 4, 32);
        SHAKE_MAX_LEAVES = common.comment("At most this many leaves drop something in one shake.").translation("goatapples.configuration.shake_max_leaves").defineInRange("shake_max_leaves", 3, 1, 16);
        SHAKE_GOLDEN_APPLE_CHANCE = common.comment("The chance a leaf that drops fruit also drops a golden apple.").translation("goatapples.configuration.shake_golden_apple_chance").defineInRange("shake_golden_apple_chance", 0.01, 0.0, 1.0);
        TREE_MIN_FRUIT_LEAVES = common.comment("Leaves that bear fruit a tree needs above its trunk to count.").translation("goatapples.configuration.tree_min_fruit_leaves").defineInRange("tree_min_fruit_leaves", 5, 1, 64);
        TREE_NATURAL_LEAVES_ONLY = common.comment("Leaves a player placed do not count.").translation("goatapples.configuration.tree_natural_leaves_only").define("tree_natural_leaves_only", true);
        TREE_REST_SECONDS = common.comment("Seconds a tree gives nothing after it dropped fruit.").translation("goatapples.configuration.tree_rest_seconds").defineInRange("tree_rest_seconds", 300, 0, 86400);
        LIMITS_MAX_LOOSE_ITEMS_NEARBY = common.comment("Nothing drops while more than this many items lie within 8 blocks.").translation("goatapples.configuration.limits_max_loose_items_nearby").defineInRange("limits_max_loose_items_nearby", 12, 0, 256);
        COMMON_SPEC = common.build();
    }
}
