package community.coins.plugin.spigot.command;

import community.coins.plugin.command.BalanceCommandLogic;
import community.coins.plugin.command.CommandLogic;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.BasicPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eli
 * @since May 21, 2026
 */
public final class BalanceCommand extends BalanceCommandLogic<CoinsSpigot> implements SpigotCommand {
    public BalanceCommand(CoinsSpigot coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        coins.registerCommand(defined, this);
    }

    @Override
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            super.showBalance(sender, null, null);
            return true;
        }

        String currency = args[0].toLowerCase();
        if (args.length == 1) {
            super.showBalance(sender, currency, null);
            return true;
        }

        if (!sender.hasPermission(targetPermission.get())) {
            super.showBalance(sender, currency, null);
            return true;
        }

        Player player = coins.getServer().getPlayer(args[1]);
        if (player == null) {
            coins.sendMessage(sender, Language.INVALID_PLAYER);
            return true;
        }

        super.showBalance(sender, currency, new BasicPlayer(player.getUniqueId(), player.getName()));
        return true;
    }

    @Override
    public List<String> handleCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length <= 1) {
            String remaining = args.length == 1? args[0].toLowerCase() : "";
            CommandLogic.suggestStartsWith(coins.getEconomyService().getCurrencyIdentifiers(), remaining, completions::add);
        }
        else if (args.length == 2 && sender.hasPermission(targetPermission.get())) {
            String remaining = args[1].toLowerCase();
            for (Player onlinePlayer : coins.getServer().getOnlinePlayers()) {
                if (onlinePlayer.getName().toLowerCase().startsWith(remaining)) {
                    completions.add(onlinePlayer.getName());
                }
            }
        }
        return completions;
    }
}
