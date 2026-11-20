package community.coins.plugin.spigot.command;

import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.MintageCommandLogic;
import community.coins.plugin.language.Language;
import community.coins.plugin.spigot.CoinsSpigot;
import community.coins.plugin.util.Filler;
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
import java.util.function.BiFunction;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class MintageCommand extends MintageCommandLogic {
    private final CoinsSpigot coins;
    public MintageCommand(CoinsSpigot coins, DefinedCommand defined) {
        super(coins, defined);
        this.coins = coins;
    }

    @Override
    public void registerCommand() {
        BiFunction<CommandSender, String[], Boolean> command = (sender, args) -> {
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
                        coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(Filler.TYPE.filled(Language.WORD_AMOUNT)));
                        return true;
                    }

                    OptionalInt radius = Util.parseInt(args[4]);
                    if (radius.isEmpty()) {
                        coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(Filler.TYPE.filled(Language.WORD_RADIUS)));
                        return true;
                    }

                    OptionalInt value = Util.parseInt(args[5]);
                    if (value.isEmpty()) {
                        coins.sendMessage(sender, Language.COMMAND_INVALID_NUMBER.with(Filler.TYPE.filled(Language.WORD_VALUE)));
                        return true;
                    }

                    super.dropCoins(sender, location, coinIdentifier, amount.getAsInt(), radius.getAsInt(), value.getAsInt());
                }
                default -> {
                    return false;
                }
            }
            return true;
        };

        BiFunction<CommandSender, String[], List<String>> completions = (_, args) -> {
            List<String> suggestions = new ArrayList<>();
            if (args.length <= 1) {
                String remaining = args.length == 1? args[0].toLowerCase() : "";
                suggestStartsWith(ARG0_SUGGESTIONS, remaining, suggestions::add);
            }
            else if (args.length == 2) {
                String remaining = args[1].toLowerCase();
                if (args[0].equalsIgnoreCase("create")) {
                    suggestStartsWith(
                        coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                        remaining, suggestions::add
                    );
                }
                else if (args[0].equalsIgnoreCase("value")) {
                    if ("set".startsWith(remaining)) {
                        suggestions.add("set");
                    }
                }
                else if (args[0].equalsIgnoreCase("drop")) {
                    for (Player onlinePlayer : coins.getServer().getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(remaining)) {
                            suggestions.add(onlinePlayer.getName());
                        }
                    }
                    if (remaining.isEmpty() || remaining.contains(",") || Util.parseInt(remaining).isPresent()) {
                        suggestions.add("<x,y,z>");
                        suggestions.add("<x,y,z,%s>".formatted(Language.WORD_WORLD));
                    }
                }
            }
            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                    suggestStartsWith(
                        coins.getConfigService().getCurrenciesConfig().getDefinedKeys(),
                        args[2].toLowerCase(), suggestions::add
                    );
                }
                else if (args[0].equalsIgnoreCase("create")) {
                    suggestions.add("<%s>".formatted(Language.WORD_AMOUNT));
                }
                else if (args[0].equalsIgnoreCase("drop")) {
                    String remaining = args[2].toLowerCase();
                    suggestStartsWith(
                        coins.getConfigService().getCoinsConfig().getDefinedKeys(),
                        remaining, suggestions::add
                    );
                }
            }
            else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("value") && args[1].equalsIgnoreCase("set")) {
                    suggestions.add("<%s>".formatted(Language.WORD_VALUE));
                }
                else if (args[0].equalsIgnoreCase("drop")) {
                    suggestions.add("<%s>".formatted(Language.WORD_AMOUNT));
                }
            }
            else if (args.length == 5) {
                if (args[0].equalsIgnoreCase("drop")) {
                    suggestions.add("<%s>".formatted(Language.WORD_RADIUS));
                }
            }
            else if (args.length == 6) {
                if (args[0].equalsIgnoreCase("drop")) {
                    suggestions.add("<%s>".formatted(Language.WORD_VALUE));
                }
            }
            return suggestions;
        };

        coins.getCommandRegistry().registerCommand(defined, command, completions, getDescription());
    }

    private static final List<String> ARG0_SUGGESTIONS = List.of("reload", "create", "value", "drop");

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
