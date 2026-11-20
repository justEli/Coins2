package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.command.DefinedCommand;
import community.coins.plugin.util.Util;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CommandsConfig extends FileConfig<DefinedCommand> {
    public CommandsConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "commands.yml");
    }

    private final AtomicBoolean firstRun = new AtomicBoolean(true);

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();
        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            warn("Cannot register commands because section for defining commands is missing.");
            return;
        }

        Map<String, DefinedCommand> configured = new HashMap<>();
        for (String rawName : commandsSection.getKeys(false)) {
            ConfigurationSection section = commandsSection.getConfigurationSection(rawName);
            if (section == null) {
                continue;
            }

            if (!section.getBoolean("enabled", true)) {
                continue;
            }

            String name = Util.toIdentifier(rawName);
            List<String> labels = section.getStringList("labels");
            String permission = section.getString("permission"); // null always allows permission

            if (labels.isEmpty()) {
                warn("Cannot register command '%s' because there are no labels.".formatted(name));
                continue;
            }

            // get the logic implementation, or just the id of the command if not set
            String logicIdentifier = Util.toIdentifier(section.getString("logic", rawName));

            boolean exists = coins.getCommandService().hasRegisteredLogic(logicIdentifier);
            if (!exists) {
                warn("Cannot register command '%s' because command logic '%s' does not exist.".formatted(name, logicIdentifier));
                continue;
            }

            DefinedCommand definedCommand = new DefinedCommand(name, logicIdentifier, labels, permission);

            // registering also parses other settings from the config for the specific command
            RegisterResult result = coins.getCommandService().updateCommand(configWarns, definedCommand, section, service, firstRun.get());
            if (result == RegisterResult.ALREADY_REGISTERED) {
                info("Command labels and permissions for '%s' cannot be updated; reboot to apply these changes.".formatted(name));
            }

            configured.put(name, definedCommand);
        }

        putDefined(configured, "command", "commands");
        firstRun.set(false);
    }
}
