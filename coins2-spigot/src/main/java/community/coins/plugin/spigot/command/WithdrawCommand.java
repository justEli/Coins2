package community.coins.plugin.spigot.command;

import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.WithdrawCommandLogic;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.Filler;
import community.coins.plugin.util.Util;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiFunction;

/**
 * @author Eli
 * @since November 15, 2026
 */
public final class WithdrawCommand extends WithdrawCommandLogic {
    private final CoinsSpigot coins;
    public WithdrawCommand(CoinsSpigot coins, DefinedCommand defined) {
        super(coins, defined);
        this.coins = coins;
    }

    @Override
    public void registerCommand() {
        BiFunction<CommandSender, String[], Boolean> command = (sender, args) -> {
            if (args.length == 0) {
                super.sendHelpMessage(sender);
                return true;
            }

            OptionalDouble value = Util.parseDouble(args[0]);
            OptionalInt amount = args.length > 1? Util.parseInt(args[1]) : OptionalInt.of(1);

            if (value.isEmpty()) {
                coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(Filler.TYPE.filled(Language.WORD_VALUE)));
                return true;
            }

            if (amount.isEmpty()) {
                coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(Filler.TYPE.filled(Language.WORD_AMOUNT)));
                return true;
            }

            super.withdrawCoins(sender, value.getAsDouble(), amount.getAsInt());
            return true;
        };

        BiFunction<CommandSender, String[], List<String>> completions = (_, args) -> {
            List<String> suggestions = new ArrayList<>();
            if (args.length <= 1) {
                suggestions.add("<%s>".formatted(Language.WORD_VALUE));
            }
            else if (args.length == 2) {
                suggestions.add("<%s>".formatted(Language.WORD_AMOUNT));
            }
            return suggestions;
        };

        coins.getCommandRegistry().registerCommand(defined, command, completions, getDescription());
    }
}
