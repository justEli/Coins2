package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.util.Util;
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
            coins.log(Level.INFO, "Commands cannot be reloaded, so please reboot to apply command changes.");
            return;
        }

        var config = getOrCreateConfig();

        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            addWarn("Cannot register commands because section for defining commands is missing.");
            return;
        }

        Map<String, DefinedCommand> configured = new HashMap<>();
        for (String name : commandsSection.getKeys(false)) {
            ConfigurationSection section = commandsSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            if (!section.getBoolean("enabled", true)) {
                continue;
            }

            String id = Util.toIdentifier(name);
            List<String> labels = section.getStringList("labels");
            String permission = section.getString("permission", "coins.command"); // todo no permission req if null

            if (labels.isEmpty()) {
                configWarns.warn("Cannot register command '%s' because there are no labels.".formatted(id));
                continue;
            }

            boolean registered = coins.getCommandService().implementCommand(id, labels, permission);
            if (!registered) {
                configWarns.warn("Cannot register command '%s' because command logic does not exist.".formatted(id));
                continue;
            }

            configured.put(id, new DefinedCommand(id, labels));
        }

        putDefinedItems(configured, "command", "commands");
    }
}
