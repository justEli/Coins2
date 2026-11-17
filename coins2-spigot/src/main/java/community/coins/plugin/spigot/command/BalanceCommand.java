package community.coins.plugin.spigot.command;

import community.coins.plugin.command.BalanceCommandLogic;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.BasicPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Eli
 * @since May 21, 2026
 */
public final class BalanceCommand extends BalanceCommandLogic {
    private final CoinsSpigot coins;
    public BalanceCommand(CoinsSpigot coins, DefinedCommand defined) {
        super(coins, defined);
        this.coins = coins;
    }

    @Override
    public void registerCommand() {
        boolean hasCurrencyArgument = definedCurrency.get() == null;

        BiFunction<CommandSender, String[], Boolean> command = (sender, args) -> {
            if (args.length == 0) {
                super.showBalance(sender, null, null);
                return true;
            }

            String currency = null;
            if (hasCurrencyArgument) {
                 currency = args[0].toLowerCase();
                if (args.length == 1) {
                    super.showBalance(sender, currency, null);
                    return true;
                }
            }

            if (!sender.hasPermission(targetPermission.get())) {
                super.showBalance(sender, currency, null);
                return true;
            }

            String inputPlayer = hasCurrencyArgument? args[1] : args[0];
            Player player = coins.getServer().getPlayer(inputPlayer);
            if (player == null) {
                coins.sendMessage(sender, Language.INVALID_PLAYER);
                return true;
            }

            super.showBalance(sender, currency, new BasicPlayer(player.getUniqueId(), player.getName()));
            return true;
        };

        BiFunction<CommandSender, String[], List<String>> completions = (sender, args) -> {
            List<String> suggestions = new ArrayList<>();
            if (args.length <= 1 && hasCurrencyArgument) {
                String remaining = args.length == 1? args[0].toLowerCase() : "";
                suggestStartsWith(coins.getEconomyService().getCurrencyIdentifiers(), remaining, suggestions::add);
            }
            else if (sender.hasPermission(targetPermission.get()) && ((args.length == 2 && hasCurrencyArgument) || args.length <= 1)) {
                String remaining = (hasCurrencyArgument? args[1] : args[0]).toLowerCase();
                for (Player onlinePlayer : coins.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(remaining)) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }
            }
            return suggestions;
        };

        coins.getCommandRegistry().registerCommand(defined, command, completions, getDescription());
    }
}
