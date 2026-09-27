package com.neryos.goatapples;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(GoatApplesMod.MOD_ID)
public final class GoatApplesMod {
    public static final String MOD_ID = "goatapples";

    public GoatApplesMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, GoatApplesConfig.COMMON_SPEC);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> ModCommands.register(event.getDispatcher()));
    }
}
