package community.coins.plugin.paper.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import community.coins.plugin.command.BalanceCommandLogic;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.language.Language;
import community.coins.plugin.paper.CoinsPaper;
import community.coins.plugin.util.BasicPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * @author Eli
 * @since May 21, 2026
 */
public final class BalanceCommand extends BalanceCommandLogic {
    private final CoinsPaper coins;
    public BalanceCommand(CoinsPaper coins, DefinedCommand defined) {
        this.coins = coins;
        super(coins, defined);
    }

    @Override
    public void registerCommand() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(defined.getLabel());

        if (defined.getPermission() != null) {
            command.requires(source -> source.getSender().hasPermission(defined.getPermission()));
        }

        // /balance
        command.executes(context -> {
            super.showBalance(context.getSource().getSender(), null, null);
            return Command.SINGLE_SUCCESS;
        });

        var currencyArgument =
            Commands.argument("currency", StringArgumentType.word())
            .executes(context -> {
                super.showBalance(context.getSource().getSender(), context.getArgument("currency", String.class), null);
                return Command.SINGLE_SUCCESS;
            })
            .suggests((_, builder) -> {
                suggestStartsWith(
                    coins.getEconomyService().getCurrencyIdentifiers(),
                    builder.getRemainingLowerCase(), builder::suggest
                );

                return builder.buildFuture();
            });

        var targetArgument =
            Commands.argument("target", ArgumentTypes.playerProfiles())
            .requires(source -> source.getSender().hasPermission(targetPermission.get()))
            .executes(context -> {
                var sender = context.getSource().getSender();
                PlayerProfileListResolver resolver = context.getArgument("target", PlayerProfileListResolver.class);
                Collection<PlayerProfile> profiles = resolver.resolve(context.getSource());
                PlayerProfile profile = getFirstValidUuid(profiles);

                if (profile == null) {
                    coins.sendMessage(sender, Language.INVALID_PLAYER);
                    return Command.SINGLE_SUCCESS;
                }

                // todo this is not very elegant, but there's no way to know if the argument exists or not
                String currency = null;
                try { currency = context.getArgument("currency", String.class); }
                catch (IllegalArgumentException _) {}

                super.showBalance(sender, currency, new BasicPlayer(profile.getId(), profile.getName()));
                return Command.SINGLE_SUCCESS;
            });

        if (definedCurrency.get() == null) {
            // add currency as argument if there is no specific currency set for this command
            if (targetPermission.get() != null) {
                // add a target argument if there is a permission set for it
                currencyArgument.then(targetArgument); // /balance [currency] [target]
            }
            command.then(currencyArgument);
        }
        else if (targetPermission.get() != null) {
            // otherwise, there is a currency, so we only add a target argument (if a permission is set)
            command.then(targetArgument);
        }

        coins.registerCommand(command.build(), getDescription(), defined.getAliases());
    }

    private @Nullable PlayerProfile getFirstValidUuid(Collection<PlayerProfile> profiles) {
        for (PlayerProfile profile : profiles) {
            if (profile.getId() == null) {
                continue;
            }
            return profile;
        }
        return null;
    }
}
