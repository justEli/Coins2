package community.coins.plugin.spigot.command;

import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.WithdrawCommandLogic;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.Util;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * @author Eli
 * @since November 15, 2026
 */
public final class WithdrawCommand extends WithdrawCommandLogic<CoinsSpigot> implements SpigotCommand {
    public WithdrawCommand(CoinsSpigot coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        coins.registerCommand(defined, this);
    }

    @Override
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            super.sendHelpMessage(sender);
            return true;
        }

        OptionalDouble value = Util.parseDouble(args[0]);
        OptionalInt amount = args.length > 1? Util.parseInt(args[1]) : OptionalInt.of(1);

        if (value.isEmpty()) {
            coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(FILL_TYPE.filled(Language.WORD_VALUE)));
            return true;
        }

        if (amount.isEmpty()) {
            coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(FILL_TYPE.filled(Language.WORD_AMOUNT)));
            return true;
        }

        super.withdrawCoins(sender, value.getAsDouble(), amount.getAsInt());
        return true;
    }

    @Override
    public List<String> handleCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length <= 1) {
            completions.add("<%s>".formatted(Language.WORD_VALUE));
        }
        else if (args.length == 2) {
            completions.add("<%s>".formatted(Language.WORD_AMOUNT));
        }

        return completions;
    }
}
