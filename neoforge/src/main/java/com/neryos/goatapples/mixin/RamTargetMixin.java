package com.neryos.goatapples.mixin;

import com.neryos.goatapples.TreeChoice;
import com.neryos.goatapples.TreeShake;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RamTarget.class)
public abstract class RamTargetMixin {
    // RamTarget.tick: a charge that hits a block snapping a goat's horn calls owner.dropHorn().
    @Redirect(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/goat/Goat;dropHorn()Z"))
    private boolean goatapples$impact(Goat goat) {
        return TreeShake.impact(goat, TreeChoice.takeCharge(goat));
    }
}
