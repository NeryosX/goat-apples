package com.neryos.goatapples;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /goatapples: tells an operator whether baited charges shake trees and which horn mode is set. */
public final class ModCommands {
    private ModCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("goatapples")
            .requires(source -> source.hasPermission(2))
            .executes(context -> run(context.getSource())));
    }

    private static int run(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("goatapples.command.noPlayer"));
            return 0;
        }
        String shakes = GoatApplesConfig.BAITED_ENABLED.get() ? "Baited charges shake trees." : "Baited charges leave trees alone.";
        player.displayClientMessage(Component.literal(shakes), false);
        player.displayClientMessage(Component.literal(GoatApplesConfig.BAITED_HORN_MODE.get()), false);
        return 1;
    }
}
