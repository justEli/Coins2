package community.coins.plugin.spigot.command;

import community.coins.plugin.command.CommandService;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.spigot.CoinsSpigot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Eli
 * @since November 16, 2026
 */
public final class CommandRegistry {
    private final CoinsSpigot coins;
    public CommandRegistry(CoinsSpigot coins) {
        this.coins = coins;
    }

    public void registerCommand(DefinedCommand defined, BiFunction<CommandSender, String[], Boolean> command0, BiFunction<CommandSender, String[], List<String>> completions, String description) {
        try {
            Field commandMapField = coins.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(coins.getServer());
            Command command = new BukkitCommand(defined.getLabel()) {
                @NullMarked
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    boolean success = command0.apply(sender, args);
                    if (!success) {
                        coins.sendMessage(sender, CommandService.getSimpleUsage(defined.getLabel()));
                    }
                    return true;
                }

                @NullMarked
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return completions.apply(sender, args);
                }
            };

            if (defined.getPermission() != null) {
                command.setPermission(defined.getPermission());
            }

            if (!defined.getAliases().isEmpty()) {
                command.setAliases(defined.getAliases());
            }

            command.setDescription(description);
            commandMap.register(coins.getDescription().getName(), command);
        }
        catch (Exception _) {}
    }
}
