package net.pineclone.simplecmd.provider;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerNameProvider
        implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
//        Identifier player = context.getArgument("player", Identifier.class);
        List<ServerPlayerEntity> list = context.getSource().getServer().getPlayerManager().getPlayerList();
        list.forEach(p -> builder.suggest(p.getDisplayName().getString()));
        return builder.buildFuture();
    }
}
