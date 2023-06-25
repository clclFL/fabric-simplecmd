package net.pineclone.simplecmd.cmds.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pineclone.simplecmd.cmds.tpa.status.TPAManager;
import net.pineclone.simplecmd.utils.TomlUtils;

import java.util.Collection;

public class TPACommand {

    private static final int PERMISSION_LEVEL = 0;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("tpa")
                .requires(s -> s.hasPermissionLevel(PERMISSION_LEVEL))
                .then(CommandManager.argument("receivers", EntityArgumentType.players())
                        .executes(context -> {
                            Collection<ServerPlayerEntity> receivers = EntityArgumentType.getPlayers(context, "receivers");
                            ServerPlayerEntity sender = context.getSource().getPlayer();
                            if (sender == null) {
                                context.getSource().sendFeedback(Text.literal("can not execute this command with terminal") , false);
                                return 0;
                            }

                            if (receivers.size() > 1) {
                                sender.sendMessage(Text.translatable("cmd.tpa.multiple_players_not_allow").formatted(Formatting.RED));
                                return 0;
                            }

                            ServerPlayerEntity receiver = receivers.iterator().next();
                            if (!receiver.getWorld().getRegistryKey().equals(sender.getWorld().getRegistryKey())) {
                                sender.sendMessage(Text.translatable("cmd.tpa.receiver_with_different_dimension").formatted(Formatting.RED));
                                return 0;
                            }

                            if (TPAManager.isCoolingDown(sender)) {
                                sender.sendMessage(Text.translatable("cmd.tpa.is_in_cooldown", TPAManager.getRemainingCooledTime(sender)).formatted(Formatting.RED));
                                return 0;
                            }

                            return execute(context , receiver);
                        }));
        dispatcher.register(command);
    }

    private static int execute(CommandContext<ServerCommandSource> context, ServerPlayerEntity receiver) {
        if (!TomlUtils.modToml.getBoolean("cmd.enable_tpa")) {
            context.getSource().sendFeedback(Text.translatable("cmd.tpa.not_allow").formatted(Formatting.RED), false);
            return 0;
        }

        ServerPlayerEntity sender = context.getSource().getPlayer();

        if (sender == null) {
            context.getSource().sendFeedback(Text.translatable("cmd.to.point.acquire.sender.is.null"), false);
            return 0;
        }

        if (receiver == null) {
            context.getSource().sendFeedback(Text.translatable("cmd.tpa.player.cannot.be.found")
                    .formatted(Formatting.RED), false);
            return 0;
        }
        TPAManager.createNewRequest(sender, receiver);
        return 1;
    }


}
