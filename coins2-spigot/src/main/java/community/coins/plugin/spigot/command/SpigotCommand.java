package community.coins.plugin.spigot.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author Eli
 * @since November 16, 2026
 */
public interface SpigotCommand {
    boolean handleCommand(CommandSender sender, String[] args);

    List<String> handleCompletions(CommandSender sender, String[] args);

    String getDescription();
}
