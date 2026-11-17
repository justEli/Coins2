package community.coins.plugin.spigot.command;

import community.coins.plugin.command.CommandLogic;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.MintageCommandLogic;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.Util;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class MintageCommand extends MintageCommandLogic<CoinsSpigot> implements SpigotCommand {
    public MintageCommand(CoinsSpigot coins) {
        super(coins, coins.getCommandService());
    }

    @Override
    public void register(DefinedCommand defined) {
        coins.registerCommand(defined, this);
    }

    @Override
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> reload(sender);
            case "create" -> {
                if (args.length < 2) {
                    return false;
                }

                int amount = args.length > 2? Util.parseInt(args[2]).orElse(1) : 1;
                super.giveCoin(sender, args[1], amount);
            }
            case "value" -> {
                if (args.length < 4) {
                    return false;
                }

                OptionalDouble value = Util.parseDouble(args[3]);
                super.setCoinValue(sender, args[2], value.orElse(0));
            }
            case "drop" -> {
                if (args.length < 6) {
                    return false;
                }

                String locationInput = args[1];
                Location location = null;

                List<Player> possiblePlayers = coins.getServer().matchPlayer(locationInput);
                if (!possiblePlayers.isEmpty()) {
                    location = possiblePlayers.getFirst().getLocation();
                }

                if (location == null) {
                    Optional<Location> blockLocation = getLocationFromInput(sender, locationInput);
                    if (blockLocation.isEmpty()) {
                        coins.sendMessage(sender, Language.COMMAND_INVALID_LOCATION);
                        return true;
                    }

                    location = blockLocation.get();
                }

                String coinIdentifier = args[2];
                OptionalInt amount = Util.parseInt(args[3]);
                if (amount.isEmpty()) {
                    coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(FILL_TYPE.filled(Language.WORD_AMOUNT)));
                    return true;
                }

                OptionalInt radius = Util.parseInt(args[4]);
                if (radius.isEmpty()) {
                    coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(FILL_TYPE.filled(Language.WORD_RADIUS)));
                    return true;
                }

                OptionalInt value = Util.parseInt(args[5]);
                if (value.isEmpty()) {
                    coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(FILL_TYPE.filled(Language.WORD_VALUE)));
                    return true;
                }

                super.dropCoins(sender, location, coinIdentifier, amount.getAsInt(), radius.getAsInt(), value.getAsInt());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static final List<String> ARG0_SUGGESTIONS = List.of("reload", "create", "value", "drop");

    @Override
    public List<String> handleCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length <= 1) {
            String remaining = args.length == 1? args[0].toLowerCase() : "";
            CommandLogic.suggestStartsWith(ARG0_SUGGESTIONS, remaining, completions::add);
        }
        else if (args.length == 2) {
            String remaining = args[1].toLowerCase();
            if (args[0].equalsIgnoreCase("create")) {
                CommandLogic.suggestStartsWith(
                    coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                    remaining, completions::add
                );
            }
            else if (args[0].equalsIgnoreCase("value")) {
                if ("set".startsWith(remaining)) {
                    completions.add("set");
                }
            }
            else if (args[0].equalsIgnoreCase("drop")) {
                for (Player onlinePlayer : coins.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(remaining)) {
                        completions.add(onlinePlayer.getName());
                    }
                }
                if (remaining.isEmpty() || remaining.contains(",") || Util.parseInt(remaining).isPresent()) {
                    completions.add("<x,y,z>");
                    completions.add("<x,y,z,%s>".formatted(Language.WORD_WORLD));
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                CommandLogic.suggestStartsWith(
                    coins.getEconomyService().getCurrencyIdentifiers(),
                    args[2].toLowerCase(), completions::add
                );
            }
            else if (args[0].equalsIgnoreCase("create")) {
                completions.add("<%s>".formatted(Language.WORD_AMOUNT));
            }
            else if (args[0].equalsIgnoreCase("drop")) {
                String remaining = args[2].toLowerCase();
                CommandLogic.suggestStartsWith(
                    coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                    remaining, completions::add
                );
            }
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                completions.add("<%s>".formatted(Language.WORD_VALUE));
            }
            else if (args[0].equalsIgnoreCase("drop")) {
                completions.add("<%s>".formatted(Language.WORD_AMOUNT));
            }
        }
        else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("drop")) {
                completions.add("<%s>".formatted(Language.WORD_RADIUS));
            }
        }
        else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("drop")) {
                completions.add("<%s>".formatted(Language.WORD_VALUE));
            }
        }
        return completions;
    }

    private Optional<Location> getLocationFromInput(CommandSender sender, String input) {
        String[] coords = input.split(",");
        if (coords.length < 3) {
            return Optional.empty();
        }

        World world;
        if (coords.length == 4) {
            world = coins.getAttributes().getWorld(coords[3]);
        }
        else if (sender instanceof Player player) {
            world = player.getWorld();
        }
        else {
            world = coins.getServer().getWorlds().getFirst();
        }

        if (world == null) {
            return Optional.empty();
        }

        OptionalInt x = Util.parseInt(coords[0]);
        OptionalInt y = Util.parseInt(coords[1]);
        OptionalInt z = Util.parseInt(coords[2]);

        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Location(world, x.getAsInt(), y.getAsInt(), z.getAsInt()));
    }
}
