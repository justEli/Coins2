package community.coins.plugin.command;

import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.language.EntryReplacement;
import community.coins.plugin.language.Language;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eli
 * @since May 07, 2026
 */
@NullMarked
public final class CommandService {
    private final Map<String, CommandLogic<?>> commandLogics = new HashMap<>();
    private final Set<String> registeredIds = ConcurrentHashMap.newKeySet();

    /// called from the command's CommandLogic on the implementing platform.
    /// this is done before the configs are registered
    public void registerLogic(CommandLogic<?> logic) {
        commandLogics.put(logic.getIdentifier(), logic);
    }

    /// whether this command identifier exists and thus has registered logic
    public boolean hasRegisteredLogic(String identifier) {
        return commandLogics.containsKey(identifier.toLowerCase());
    }

    /// used by commands.yml, this also parses the config values for this specific command (if any)
    /// @return if the command was not registered because the config didn't parse successfully
    public RegisterResult updateCommand(ConfigWarns.Named warns, DefinedCommand defined, ConfigurationSection config, ConfigService service) {
        CommandLogic<?> logic = commandLogics.get(defined.getIdentifier());
        if (logic == null) {
            return RegisterResult.NO_LOGIC;
        }

        // (re-)parse the config for this specific command
        if (!logic.parseConfig(warns, config, service)) {
            return RegisterResult.CONFIG_FAIL;
        }

        if (registeredIds.contains(defined.getIdentifier())) {
            return RegisterResult.ALREADY_REGISTERED; // cannot register another time
        }

        logic.register(defined);
        registeredIds.add(defined.getIdentifier());
        return RegisterResult.REGISTERED;
    }

    private static final EntryReplacement FILL_COMMAND = new EntryReplacement("command");

    public static Component getSimpleUsage(String label) {
        return Language.COMMAND_INCOMPLETE.with(
            FILL_COMMAND.filled("/%s (arguments)".formatted(label)) // todo translate "arguments"
        );
    }
}
