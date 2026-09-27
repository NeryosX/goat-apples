package com.neryos.goatapples;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public final class GoatApplesMod implements ModInitializer {
    public static final String MOD_ID = "goatapples";

    @Override
    public void onInitialize() {
        GoatApplesConfig.load(FabricLoader.getInstance().getConfigDir());
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> ModCommands.register(dispatcher));
    }
}
