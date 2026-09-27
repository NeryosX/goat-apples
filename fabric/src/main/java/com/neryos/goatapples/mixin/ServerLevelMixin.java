package com.neryos.goatapples.mixin;

import com.neryos.goatapples.TreeShake;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    // ServerLevel.tick: once a tick, the fruit of a shaken tree falls in its turn.
    @Inject(method = "tick", at = @At("TAIL"))
    private void goatapples$fallingFruit(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        TreeShake.tick((ServerLevel) (Object) this);
    }
}
