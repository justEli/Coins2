package community.coins.plugin.spigot.command;

import community.coins.plugin.command.CoinsCommandLogic;
import community.coins.plugin.coin.DefinedCoin;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CoinsCommand extends CoinsCommandLogic {
    private final CoinsSpigot coins;
    public CoinsCommand(CoinsSpigot coins) {
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

        try {
            Field commandMapField = coins.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(coins.getServer());
            Command command = new BukkitCommand(label) {
                @NullMarked
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    boolean success = handleCommand(sender, args);
                    if (!success) {
                        sender.sendMessage(ChatColor.RED + "Incomplete command.");
                    }
                    return true;
                }

                @NullMarked
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return handleCompletions(sender, args);
                }
            };

            command.setPermission(permission);
            command.setAliases(labels);
            commandMap.register(coins.getDescription().getName(), command);
        }
        catch (Exception _) {}
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> reload(sender);
            case "give" -> {
                if (args.length < 2) {
                    return false;
                }

                giveCoin(sender, args[1]);
            }
            case "value" -> {
                if (args.length < 4) {
                    return false;
                }

                OptionalDouble value = Util.parseDouble(args[3]);
                setCoinValue(sender, args[2], value.orElse(0));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private List<String> handleCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length <= 1) {
            String remaining = args.length == 1? args[0].toLowerCase() : "";
            if ("reload".startsWith(remaining)) {
                completions.add("reload");
            }
            if ("give".startsWith(remaining)) {
                completions.add("give");
            }
            if ("value".startsWith(remaining)) {
                completions.add("value");
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                for (DefinedCoin coin : coins.getConfigService().getCoinsConfig().getDefinedItems()) {
                    completions.add(coin.getId());
                }
            }
            else if (args[0].equalsIgnoreCase("value")) {
                String remaining = args[1].toLowerCase();
                if ("set".startsWith(remaining)) {
                    completions.add("set");
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                completions.addAll(coins.getEconomyService().getCurrencyIdentifiers());
            }
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                completions.add("<%s>".formatted(Language.WORD_VALUE));
            }
        }
        return completions;
    }
}
