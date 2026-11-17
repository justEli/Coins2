package community.coins.plugin.paper.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import community.coins.plugin.command.BalanceCommandLogic;
import community.coins.plugin.command.CommandLogic;
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
public final class BalanceCommand extends BalanceCommandLogic<CoinsPaper> {
    public BalanceCommand(CoinsPaper coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(defined.getLabel());

        if (defined.getPermission() != null) {
            command.requires(source -> source.getSender().hasPermission(defined.getPermission()));
        }

        command.executes(context -> {
            super.showBalance(context.getSource().getSender(), null, null);
            return Command.SINGLE_SUCCESS;
        });

        var parameters = Commands.argument("currency", StringArgumentType.word()).executes(context -> {
            super.showBalance(context.getSource().getSender(), context.getArgument("currency", String.class), null);
            return Command.SINGLE_SUCCESS;
        })
        .suggests((_, builder) -> {
            CommandLogic.suggestStartsWith(
                coins.getEconomyService().getCurrencyIdentifiers(),
                builder.getRemainingLowerCase(), builder::suggest
            );

            return builder.buildFuture();
        });

        if (targetPermission.get() != null) {
            parameters.then(
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

                    super.showBalance(
                        sender,
                        context.getArgument("currency", String.class),
                        new BasicPlayer(profile.getId(), profile.getName())
                    );
                    return Command.SINGLE_SUCCESS;
                })
            );
        }

        command.then(parameters);
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
