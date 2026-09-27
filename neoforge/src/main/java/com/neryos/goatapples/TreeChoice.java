package com.neryos.goatapples;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** A goat with nothing to ram may pick a fruit tree nearby and charge it the
 *  way it charges a player: the same walk, lowered head, sound and sprint. */
public final class TreeChoice extends Behavior<Goat> {
    private static final int PREPARE_TICKS = 20;
    /** Goats whose next impact is a charge at a tree they chose, until when. */
    private static final Map<Goat, Long> CHARGING = new WeakHashMap<>();
    private static final Map<Goat, TreeChoice> PLANNING = new WeakHashMap<>();

    private BlockPos trunk;
    private BlockPos start;
    private long reachedStart = -1;

    public TreeChoice() {
        super(ImmutableMap.of(
            MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED), 160);
    }

    /** Whether this goat is on its way to a tree, so vanilla's "nothing to ram"
     *  cooldown waits. */
    public static boolean planning(Goat goat) {
        return PLANNING.containsKey(goat);
    }

    /** Whether the impact now is a charge at a chosen tree; asked once. */
    public static boolean takeCharge(Goat goat) {
        Long until = CHARGING.remove(goat);
        return until != null && until >= goat.level().getGameTime();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goat goat) {
        if (!GoatApplesConfig.CHOSEN_ENABLED.get() || goat.isBaby()) return false;
        if (somethingToRam(goat)) return false;
        if (level.random.nextDouble() >= GoatApplesConfig.CHOSEN_PICK_CHANCE.get()) return false;
        return pick(level, goat);
    }

    /** Vanilla rams what it can: a tree only stands in for nothing. */
    private static boolean somethingToRam(Goat goat) {
        Optional<NearestVisibleLivingEntities> seen = goat.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return seen.isPresent() && seen.get().findClosest(e -> e.getType() != EntityType.GOAT && e.canBeSeenAsEnemy()
            && !(e instanceof Player player && (player.isCreative() || player.isSpectator()))).isPresent();
    }

    /** The nearest fruit tree in sight that is not resting, with a start square in a straight lane from it. */
    private boolean pick(ServerLevel level, Goat goat) {
        int radius = GoatApplesConfig.CHOSEN_SEARCH_RADIUS.get();
        BlockPos at = goat.blockPosition();
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-radius, -1, -radius), at.offset(radius, 1, radius))) {
            double far = pos.distSqr(at);
            if (far < 16 || far > radius * radius || far >= best) continue;
            if (!level.getBlockState(pos).is(BlockTags.LOGS) || !level.getBlockState(pos).is(BlockTags.SNAPS_GOAT_HORN)) continue;
            if (TreeShake.fruitLeaves(level, pos).size() < GoatApplesConfig.TREE_MIN_FRUIT_LEAVES.get()) continue;
            if (TreeShake.resting(level, pos) || !seen(level, goat, pos)) continue;
            BlockPos lane = startSquare(goat, pos);
            if (lane == null) continue;
            best = far;
            trunk = pos.immutable();
            start = lane;
        }
        return trunk != null;
    }

    private static boolean seen(ServerLevel level, Goat goat, BlockPos pos) {
        BlockHitResult hit = level.clip(new ClipContext(goat.getEyePosition(), Vec3.atCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, goat));
        return hit.getBlockPos().equals(pos);
    }

    /** As vanilla finds one before a ram: the farthest walkable square of a
     *  straight lane out from the trunk, four to seven away, the goat's nearest. */
    private static BlockPos startSquare(Goat goat, BlockPos trunk) {
        BlockPos chosen = null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos last = null;
            for (int i = 1; i <= 7; i++) {
                BlockPos pos = trunk.relative(direction, i);
                if (!walkable(goat, pos)) break;
                last = pos;
            }
            if (last == null || last.distManhattan(trunk) < 4) continue;
            if (chosen != null && goat.blockPosition().distSqr(last) >= goat.blockPosition().distSqr(chosen)) continue;
            Path path = goat.getNavigation().createPath(last, 0);
            if (path != null && path.canReach()) chosen = last;
        }
        return chosen;
    }

    private static boolean walkable(Goat goat, BlockPos pos) {
        return goat.getNavigation().isStableDestination(pos) && goat.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(goat, pos)) == 0.0F;
    }

    @Override
    protected void start(ServerLevel level, Goat goat, long gameTime) {
        PLANNING.put(goat, this);
        reachedStart = -1;
        goat.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(start, 1.25F, 0));
        goat.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(trunk));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goat goat, long gameTime) {
        return trunk != null && !goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET) && level.getBlockState(trunk).is(BlockTags.LOGS);
    }

    @Override
    protected void tick(ServerLevel level, Goat goat, long gameTime) {
        Brain<Goat> brain = goat.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(trunk));
        if (!goat.blockPosition().equals(start)) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(start, 1.25F, 0));
            return;
        }
        level.broadcastEntityEvent(goat, (byte) 58);
        if (reachedStart < 0) reachedStart = gameTime;
        if (gameTime - reachedStart < PREPARE_TICKS) return;
        // Just inside the trunk, so the goat's own charge runs into it.
        Vec3 face = Vec3.atBottomCenterOf(trunk).add(Vec3.atLowerCornerOf(start.subtract(trunk)).normalize().scale(0.4));
        brain.setMemory(MemoryModuleType.RAM_TARGET, face);
        level.playSound(null, goat, goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM, SoundSource.NEUTRAL, 1.0F, goat.getVoicePitch());
        CHARGING.put(goat, gameTime + 200);
    }

    @Override
    protected void stop(ServerLevel level, Goat goat, long gameTime) {
        PLANNING.remove(goat);
        if (!goat.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
            // A plan that came to nothing waits as long as a failed ram does.
            level.broadcastEntityEvent(goat, (byte) 59);
            goat.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, goat.isScreamingGoat() ? 100 : 600);
        }
        trunk = null;
        start = null;
    }
}
