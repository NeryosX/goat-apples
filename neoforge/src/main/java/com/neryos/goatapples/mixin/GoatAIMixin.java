package com.neryos.goatapples.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.neryos.goatapples.TreeChoice;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.goat.GoatAi;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GoatAi.class)
public abstract class GoatAIMixin {
    // GoatAi.initRamActivity: the RAM activity's behaviours; the tree choice joins them, after the ram's own two.
    @Inject(method = "initRamActivity", at = @At("TAIL"))
    private static void goatapples$treeChoice(Brain<Goat> brain, CallbackInfo ci) {
        brain.addActivityWithConditions(Activity.RAM, ImmutableList.of(Pair.of(2, new TreeChoice())), ImmutableSet.of(
            Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
            Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }
}
