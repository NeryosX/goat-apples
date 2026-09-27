package com.neryos.goatapples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** What a goat's charge into a tree does: the shake, the tree's rest and the horn. */
public final class TreeShake {
    public static final TagKey<Block> FRUIT_LEAVES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("goatapples", "fruit_leaves"));
    private static final ResourceKey<LootTable> SHAKE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("goatapples", "gameplay/goat_shake"));
    /** When each tree that dropped fruit gives fruit again, by dimension and trunk. */
    private static final Map<String, Long> RESTING = new HashMap<>();

    private TreeShake() {
    }

    /** At a charge's impact on a horn-snapping block, where vanilla would drop
     *  a horn: shakes the tree if it is one, and answers whether a horn fell. */
    public static boolean impact(Goat goat, boolean chosen) {
        ServerLevel level = (ServerLevel) goat.level();
        BlockPos trunk = trunkHit(level, goat);
        boolean shook = false;
        if (trunk != null) {
            boolean on = chosen ? GoatApplesConfig.CHOSEN_ENABLED.get() : GoatApplesConfig.BAITED_ENABLED.get();
            double chance = chosen ? GoatApplesConfig.CHOSEN_SHAKE_CHANCE.get() : GoatApplesConfig.BAITED_SHAKE_CHANCE.get();
            if (on && level.random.nextDouble() < chance) shook = shake(level, goat, trunk);
        }
        if (chosen) return level.random.nextDouble() < GoatApplesConfig.CHOSEN_HORN_CHANCE.get() && goat.dropHorn();
        String mode = GoatApplesConfig.BAITED_HORN_MODE.get();
        if (mode.equals("never") || (mode.equals("with_shake") && !shook)) return false;
        return goat.dropHorn();
    }

    /** The log the goat ran into, found the way vanilla finds the block it hit. */
    static BlockPos trunkHit(ServerLevel level, Goat goat) {
        Vec3 ahead = goat.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize();
        BlockPos at = BlockPos.containing(goat.position().add(ahead));
        for (BlockPos pos : new BlockPos[] {at, at.above()}) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) && state.is(BlockTags.SNAPS_GOAT_HORN)) return pos;
        }
        return null;
    }

    /** Leaves that bear fruit above a trunk block, in the canopy's box. */
    static List<BlockPos> fruitLeaves(ServerLevel level, BlockPos trunk) {
        boolean naturalOnly = GoatApplesConfig.TREE_NATURAL_LEAVES_ONLY.get();
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(trunk.offset(-3, 2, -3), trunk.offset(3, 8, 3))) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(FRUIT_LEAVES)) continue;
            if (naturalOnly && state.hasProperty(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.PERSISTENT)) continue;
            found.add(pos.immutable());
        }
        return found;
    }

    /** The lowest log of the trunk, which names the tree for its rest. */
    static BlockPos base(ServerLevel level, BlockPos trunk) {
        BlockPos pos = trunk;
        while (level.getBlockState(pos.below()).is(BlockTags.LOGS)) pos = pos.below();
        return pos;
    }

    static boolean resting(ServerLevel level, BlockPos trunk) {
        Long until = RESTING.get(key(level, trunk));
        return until != null && until > level.getGameTime();
    }

    private static String key(ServerLevel level, BlockPos trunk) {
        return level.dimension().location() + "@" + base(level, trunk).asLong();
    }

    /** Leaves rustle and some let their fruit fall, unless the tree is resting. */
    static boolean shake(ServerLevel level, Goat goat, BlockPos trunk) {
        List<BlockPos> leaves = fruitLeaves(level, trunk);
        if (leaves.isEmpty()) return false;

        // The whole canopy rustles: leaf bits over many of its leaves, and the leaves' own sound.
        BlockState leaf = level.getBlockState(leaves.get(0));
        SoundType sound = leaf.getSoundType();
        level.playSound(null, leaves.get(0), sound.getHitSound(), SoundSource.BLOCKS, sound.getVolume() + 0.6F, sound.getPitch() * 0.8F);
        level.playSound(null, leaves.get(leaves.size() / 2), sound.getBreakSound(), SoundSource.BLOCKS, 0.4F, sound.getPitch() * 1.3F);
        for (int i = 0; i < Math.min(16, leaves.size()); i++) {
            BlockPos pos = leaves.get(level.random.nextInt(leaves.size()));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(pos)), pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 5, 0.45, 0.3, 0.45, 0.0);
        }

        if (resting(level, trunk)) return true;
        Vec3 middle = Vec3.atCenterOf(trunk);
        if (level.getEntitiesOfClass(ItemEntity.class, new AABB(middle, middle).inflate(8.0)).size() > GoatApplesConfig.LIMITS_MAX_LOOSE_ITEMS_NEARBY.get()) return true;

        // One to the most leaves the config allows let fruit fall, each a little after the last.
        int count = 1 + level.random.nextInt(Math.min(GoatApplesConfig.SHAKE_MAX_LEAVES.get(), leaves.size()));
        LootTable table = level.getServer().reloadableRegistries().getLootTable(SHAKE_LOOT);
        long now = level.getGameTime();
        boolean dropped = false;
        for (int i = 0; i < count; i++) {
            BlockPos pos = leaves.remove(level.random.nextInt(leaves.size()));
            LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.THIS_ENTITY, goat)
                .create(LootContextParamSets.CHEST);
            List<ItemStack> fruit = new ArrayList<>(table.getRandomItems(params));
            // Now and then a golden apple, as rare as the config makes it.
            if (level.random.nextDouble() < GoatApplesConfig.SHAKE_GOLDEN_APPLE_CHANCE.get()) fruit.add(new ItemStack(Items.GOLDEN_APPLE));
            BlockPos under = underside(level, pos);
            for (ItemStack stack : fruit) {
                FALLING.add(new Falling(level, under, stack, now + 3 + i * 5L + level.random.nextInt(4)));
                dropped = true;
            }
        }
        if (dropped) {
            if (RESTING.size() > 512) RESTING.values().removeIf(until -> until <= now);
            RESTING.put(key(level, trunk), now + GoatApplesConfig.TREE_REST_SECONDS.get() * 20L);
        }
        return true;
    }

    /** The first open square under a leaf: fruit starts there and falls, so it
     *  drops out of the canopy rather than being pushed out of a leaf block. */
    private static BlockPos underside(ServerLevel level, BlockPos leaf) {
        BlockPos pos = leaf.below();
        for (int i = 0; i < 8 && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty(); i++) pos = pos.below();
        return pos;
    }

    private record Falling(ServerLevel level, BlockPos at, ItemStack stack, long due) {
    }

    private static final List<Falling> FALLING = new ArrayList<>();

    /** Each server tick of a level: the fruit whose moment has come falls. */
    public static void tick(ServerLevel level) {
        if (FALLING.isEmpty()) return;
        long now = level.getGameTime();
        FALLING.removeIf(f -> {
            if (f.level() != level || f.due() > now) return false;
            ItemEntity item = new ItemEntity(level, f.at().getX() + 0.5 + level.random.triangle(0.0, 0.2), f.at().getY() + 0.7, f.at().getZ() + 0.5 + level.random.triangle(0.0, 0.2), f.stack());
            item.setDeltaMovement(0.0, 0.0, 0.0);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
            return true;
        });
    }
}
