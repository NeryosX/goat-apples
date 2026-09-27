package com.neryos.goatapples.mixin;

import com.neryos.goatapples.TreeChoice;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.PrepareRamNearestTarget;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareRamNearestTarget.class)
public abstract class PrepareRamMixin {
    // PrepareRamNearestTarget.stop: with nothing to ram it sets the ram cooldown, which would end the tree choice's walk.
    @Inject(method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)V", at = @At("HEAD"), cancellable = true)
    private void goatapples$keepTreeChoice(ServerLevel level, PathfinderMob entity, long gameTime, CallbackInfo ci) {
        if (entity instanceof Goat goat && TreeChoice.planning(goat)) ci.cancel();
    }
}
