package net.pineclone.simplecmd.cmds;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.command.ServerCommandSource;
import net.pineclone.simplecmd.cmds.tpa.TPAAcceptCommand;
import net.pineclone.simplecmd.cmds.tpa.TPACommand;
import net.pineclone.simplecmd.cmds.tpa.TPARefuseCommand;

import java.util.function.Consumer;

public class CommandRegistryHandler {

    private static final Event<CommandRegistrationCallback> HANDLER =
            CommandRegistrationCallback.EVENT;

    public static void register() {
        register(TPACommand::register);
        register(TPAAcceptCommand::register);
        register(TPARefuseCommand::register);
    }

    public static void register(Consumer<CommandDispatcher<ServerCommandSource>> command) {
        HANDLER.register((dispatcher, registryAccess, environment) -> command.accept(dispatcher));
    }

}
