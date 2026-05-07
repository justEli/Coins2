package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.command.DefinedCommand;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CommandsConfig extends FileConfig<DefinedCommand> {
    public CommandsConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "commands.yml");
    }

    private final AtomicBoolean registered = new AtomicBoolean(false);

    @Override
    public void parseAndReload() {
        if (registered.getAndSet(true)) {
            coins.log(Level.INFO, "Commands cannot be reloaded. Please reboot the server to apply changes.");
            return;
        }

        var config = getOrCreateConfig();

        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            addWarn("There are no defined commands in the config, `commands` section missing.");
            return;
        }

        Map<String, DefinedCommand> configured = new HashMap<>();
        for (String name : commandsSection.getKeys(false)) {
            ConfigurationSection section = commandsSection.getConfigurationSection(name);
            if (section == null) {
                coins.debug("Skipping command config entry for '%s', as nothing is configured.".formatted(name));
                continue;
            }

            if (!section.getBoolean("enabled", true)) {
                continue;
            }

            String id = name.toLowerCase();
            List<String> labels = section.getStringList("labels");
            String permission = section.getString("permission", "coins.command");

            boolean registered = coins.getCommandService().implementCommand(id, labels, permission);
            if (!registered) {
                configWarns.warn("Cannot register command with unknown command logic '%s'.".formatted(id));
                continue;
            }

            configured.put(id, new DefinedCommand(id, labels));
        }

        putDefinedItems(configured, "command", "commands");
    }
}
