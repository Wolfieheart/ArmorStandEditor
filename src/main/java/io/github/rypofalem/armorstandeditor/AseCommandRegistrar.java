package io.github.rypofalem.armorstandeditor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Registers /ase, /armorstandeditor and /asedit through the Paper Lifecycle API
 * (getCommand() is unsupported for Paper plugins). Wraps the existing CommandEx
 * and AseTabCompleter so neither needs to change.
 */
public class AseCommandRegistrar {
    private static final List<String> ALIASES = List.of("ase", "armorstandeditor", "asedit");
    private static final String[] NO_ARGS = new String[0];

    private final ArmorStandEditorPlugin plugin;
    private final CommandEx executor;
    private final AseTabCompleter tabCompleter;

    AseCommandRegistrar(ArmorStandEditorPlugin plugin, CommandEx executor, AseTabCompleter tabCompleter) {
        this.plugin = plugin;
        this.executor = executor;
        this.tabCompleter = tabCompleter;
    }

    void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            ALIASES.forEach(alias ->
                    commands.register(buildNode(alias, new StubCommand(alias)), "ArmorStandEditor command"));
        });
    }

    private LiteralCommandNode<CommandSourceStack> buildNode(String alias, org.bukkit.command.Command stub) {
        return Commands.literal(alias)
                .executes(ctx -> run(ctx, stub, alias, NO_ARGS))
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> suggest(ctx, builder, stub, alias))
                        .executes(ctx -> run(ctx, stub, alias,
                                ctx.getArgument("args", String.class).split(" ", -1))))
                .build();
    }

    private int run(CommandContext<CommandSourceStack> ctx, org.bukkit.command.Command stub,
                    String alias, String[] args) {
        executor.onCommand(ctx.getSource().getSender(), stub, alias, args);
        return Command.SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> ctx,
                                                   SuggestionsBuilder builder,
                                                   org.bukkit.command.Command stub, String alias) {
        String remaining = builder.getRemaining();
        String[] args = remaining.split(" ", -1);
        List<String> completions =
                tabCompleter.onTabComplete(ctx.getSource().getSender(), stub, alias, args);

        // Only replace the token being typed, not the whole greedy argument.
        SuggestionsBuilder lastToken =
                builder.createOffset(builder.getStart() + remaining.lastIndexOf(' ') + 1);
        completions.forEach(lastToken::suggest);
        return lastToken.buildFuture();
    }

    /** Stub so AseTabCompleter/CommandEx can call command.getName(); Brigadier has no equivalent object. */
    private static final class StubCommand extends org.bukkit.command.Command {
        StubCommand(String name) {
            super(name);
        }

        @Override
        public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
            return false;
        }
    }
}
