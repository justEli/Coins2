package community.coins.plugin.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import community.coins.plugin.command.CoinsCommandLogic;
import community.coins.plugin.item.DefinedCoin;
import community.coins.plugin.paper.CoinsPaper;
import io.papermc.paper.command.brigadier.Commands;

import java.util.List;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CoinsCommand extends CoinsCommandLogic {
    private final CoinsPaper coins;
    public CoinsCommand(CoinsPaper coins) {
        super(coins, coins.getCommandService());
        this.coins = coins;
    }

    @Override
    public void register(List<String> labels, String permission) {
        if (labels.isEmpty()) {
            return;
        }

        String label = labels.getFirst();
        labels.removeFirst();

        coins.registerCommand(
            Commands.literal(label)
                .requires(source -> source.getSender().hasPermission(permission))
                .then(
                    Commands.literal("reload")
                    .executes(context -> {
                        reload(context.getSource().getSender());
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(
                    Commands.literal("give")
                    .then(
                        Commands.argument("coin_id", StringArgumentType.string())
                        .executes(context -> {
                            giveCoin(
                                context.getSource().getSender(),
                                context.getArgument("coin_id", String.class)
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                        .suggests((_, builder) -> {
                            for (DefinedCoin coin : coins.getConfigService().getCoinsConfig().getDefinedItems()) {
                                builder.suggest(coin.getId());
                            }

                            return builder.buildFuture();
                        })
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
                                    setCoinValue(
                                        context.getSource().getSender(),
                                        context.getArgument("currency", String.class),
                                        context.getArgument("value", Double.class)
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                            .suggests((_, builder) -> {
                                for (String currency : coins.getEconomyService().getCurrencyIdentifiers()) {
                                    builder.suggest(currency);
                                }

                                return builder.buildFuture();
                            })
                        )
                    )
                )
                .build(),
            getDescription(),
            labels
        );
    }
}
