package net.pineclone.simplecmd.cmds.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pineclone.simplecmd.cmds.tpa.status.PreTeleport;
import net.pineclone.simplecmd.cmds.tpa.status.TPAManager;
import net.pineclone.simplecmd.cmds.tpa.status.TPARequest;

public class TPAAcceptCommand {

    private static final int PERMISSION_LEVEL = 0;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("tpaaccept")
                .requires(s -> s.hasPermissionLevel(PERMISSION_LEVEL))
                .executes(context -> {
                    ServerPlayerEntity receiver = context.getSource().getPlayer();
                    if (receiver == null) {
                        context.getSource().sendFeedback(Text.literal("can not execute this command with terminal"), false);
                        return 0;
                    }
                    return execute(context);
                });
        dispatcher.register(command);
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity receiver = context.getSource().getPlayer();
        assert receiver != null;
        TPAManager.RequestList requestList = TPAManager.getRequestList(receiver);
        if (requestList.peek().isEmpty()) {
            receiver.sendMessage(Text.translatable("cmd.tpaaccept.no_request_to_reply").formatted(Formatting.RED));
            return 0;
        }

        //noinspection OptionalGetWithoutIsPresent
        TPARequest currentRequest = requestList.pop().get();
        currentRequest.update(new PreTeleport(currentRequest));
        return 1;
    }

}
