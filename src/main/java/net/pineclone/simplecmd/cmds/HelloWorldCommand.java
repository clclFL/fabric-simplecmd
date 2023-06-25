package net.pineclone.simplecmd.cmds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class HelloWorldCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> command =
                CommandManager.literal("helloworld").requires(s -> s.hasPermissionLevel(4))
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(context -> execute(context, EntityArgumentType.getPlayers(context, "player"))));
        dispatcher.register(command);
    }

    private static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players) {
        if (players.isEmpty()) {
            context.getSource().sendFeedback(Text.translatable("cmd.helloworld.fail.by.no.player",
                    StringArgumentType.getString(context, "player")), false);
            return 0;
        }

        for (ServerPlayerEntity player : players) {
            ServerPlayerEntity sender = context.getSource().getPlayer();
            if (sender == null)
                player.sendMessage(Text.translatable("cmd.helloworld.success.by.console"));
            else
                player.sendMessage(Text.translatable("cmd.helloworld.success.by.player", sender.getDisplayName().getString()));
            context.getSource().sendFeedback(Text.translatable("cmd.helloworld.success.to.sender"), false);
        }
        return players.size();
    }
}
