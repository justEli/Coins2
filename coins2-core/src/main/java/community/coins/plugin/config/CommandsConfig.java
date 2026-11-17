package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.command.RegisterResult;
import community.coins.plugin.util.Util;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CommandsConfig extends FileConfig<DefinedCommand> {
    public CommandsConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "commands.yml");
    }

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();
        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            warn("Cannot register commands because section for defining commands is missing.");
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
            String permission = section.getString("permission"); // null always allows permission

            if (labels.isEmpty()) {
                warn("Cannot register command '%s' because there are no labels.".formatted(id));
                continue;
            }

            boolean exists = coins.getCommandService().hasRegisteredLogic(id);
            if (!exists) {
                warn("Cannot register command '%s' because command logic does not exist.".formatted(id));
                continue;
            }

            DefinedCommand definedCommand = new DefinedCommand(id, labels, permission);

            // registering also parses other settings from the config for the specific command
            RegisterResult result = coins.getCommandService().updateCommand(configWarns, definedCommand, section, service);
            if (result == RegisterResult.ALREADY_REGISTERED) {
                info("Command labels and permissions for '%s' cannot be updated; reboot to apply command changes.".formatted(id));
            }

            configured.put(id, definedCommand);
        }

        putDefinedItems(configured, "command", "commands");
    }
}
