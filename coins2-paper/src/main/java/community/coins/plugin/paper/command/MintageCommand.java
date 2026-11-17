package community.coins.plugin.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import community.coins.plugin.command.CommandLogic;
import community.coins.plugin.command.CommandService;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.MintageCommandLogic;
import community.coins.plugin.paper.CoinsPaper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class MintageCommand extends MintageCommandLogic<CoinsPaper> {
    public MintageCommand(CoinsPaper coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(defined.getLabel());

        if (defined.getPermission() != null) {
            command.requires(source -> source.getSender().hasPermission(defined.getPermission()));
        }

        command.executes(context -> {
            coins.sendMessage(context.getSource().getSender(), CommandService.getSimpleUsage(defined.getLabel()));
            return Command.SINGLE_SUCCESS;
        })
        .then(
            Commands.literal("reload")
            .executes(context -> {
                super.reload(context.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(
            Commands.literal("create")
            .then(
                Commands.argument("coin_id", StringArgumentType.string())
                .executes(context -> {
                    super.giveCoin(
                        context.getSource().getSender(),
                        context.getArgument("coin_id", String.class),
                        1
                    );
                    return Command.SINGLE_SUCCESS;
                })
                .suggests((_, builder) -> {
                    CommandLogic.suggestStartsWith(
                        coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                        builder.getRemainingLowerCase(), builder::suggest
                    );

                    return builder.buildFuture();
                })
                .then(
                    Commands.argument("amount", IntegerArgumentType.integer())
                    .executes(context -> {
                        super.giveCoin(
                            context.getSource().getSender(),
                            context.getArgument("coin_id", String.class),
                            context.getArgument("amount", int.class)
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        )
        .then(
            Commands.literal("value")
            .then(
                Commands.literal("set")
                .then(
                    Commands.argument("currency", StringArgumentType.word())
                    .then(
                        Commands.argument("value", DoubleArgumentType.doubleArg(0))
                        .executes(context -> {
                            super.setCoinValue(
                                context.getSource().getSender(),
                                context.getArgument("currency", String.class),
                                context.getArgument("value", Double.class)
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .suggests((_, builder) -> {
                        CommandLogic.suggestStartsWith(
                            coins.getEconomyService().getCurrencyIdentifiers(),
                            builder.getRemainingLowerCase(), builder::suggest
                        );

                        return builder.buildFuture();
                    })
                )
            )
        )
        .then(
            Commands.literal("drop")
            .then(
                Commands.argument("location", ArgumentTypes.blockPosition())
                .then(
                    Commands.argument("coin_id", StringArgumentType.word())
                    .suggests((_, builder) -> {
                        CommandLogic.suggestStartsWith(
                            coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                            builder.getRemainingLowerCase(), builder::suggest
                        );

                        return builder.buildFuture();
                    })
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer())
                        .then(
                            Commands.argument("radius", IntegerArgumentType.integer())
                            .then(
                                Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                .executes(context -> {
                                    World world = context.getSource().getSender() instanceof Player player
                                        ? player.getWorld()
                                        : coins.getServer().getRespawnWorld();

                                    BlockPositionResolver resolver =
                                        context.getArgument("location", BlockPositionResolver.class);

                                    //noinspection UnstableApiUsage
                                    FinePosition pos = resolver.resolve(context.getSource()).offset(.5, .5, .5);
                                    //noinspection UnstableApiUsage
                                    Location location = pos.toLocation(world);

                                    super.dropCoins(
                                        context.getSource().getSender(),
                                        location,
                                        context.getArgument("coin_id", String.class),
                                        context.getArgument("amount", int.class),
                                        context.getArgument("radius", int.class),
                                        context.getArgument("value", double.class)
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                )
            )
        );

        coins.registerCommand(command.build(), getDescription(), defined.getAliases());
    }
}
