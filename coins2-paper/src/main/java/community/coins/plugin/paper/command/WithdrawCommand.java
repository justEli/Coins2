package community.coins.plugin.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.WithdrawCommandLogic;
import community.coins.plugin.paper.CoinsPaper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * @author Eli
 * @since November 15, 2026
 */
public final class WithdrawCommand extends WithdrawCommandLogic<CoinsPaper> {
    public WithdrawCommand(CoinsPaper coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(defined.getLabel());

        if (defined.getPermission() != null) {
            command.requires(source -> source.getSender().hasPermission(defined.getPermission()));
        }

        command.executes(context -> {
            super.sendHelpMessage(context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        })
        .then(
            Commands.argument("value", DoubleArgumentType.doubleArg(0))
            .executes(context -> {
                super.withdrawCoins(
                    context.getSource().getSender(),
                    context.getArgument("value", double.class),
                    1
                );
                return Command.SINGLE_SUCCESS;
            })
            .then(
                Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                .executes(context -> {
                    super.withdrawCoins(
                        context.getSource().getSender(),
                        context.getArgument("value", double.class),
                        context.getArgument("amount", int.class)
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
        );

        coins.registerCommand(command.build(), getDescription(), defined.getAliases());
    }
}
