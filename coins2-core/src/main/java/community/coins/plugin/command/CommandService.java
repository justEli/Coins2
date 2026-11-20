package community.coins.plugin.command;

import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.config.RegisterResult;
import community.coins.plugin.language.EntryReplacement;
import community.coins.plugin.language.Language;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eli
 * @since May 07, 2026
 */
@NullMarked
public final class CommandService {
    private final Map<String, CommandIdentifier<?>> commandIdentifiers = new HashMap<>();
    private final Map<String, CommandLogic> registeredCommandNames = new HashMap<>();

    /// called from the command's CommandIdentifier on the implementing platform.
    /// this is done before the configs are registered
    public <T extends CommandLogic> void registerIdentifier(CommandIdentifier<T> type) {
        commandIdentifiers.put(type.getIdentifier(), type);
    }

    /// whether this command identifier exists and thus has registered logic
    public boolean hasRegisteredLogic(String identifier) {
        return commandIdentifiers.containsKey(identifier.toLowerCase());
    }

    /// used by commands.yml, this also parses the config values for this specific command (if any).
    /// unlike #registerLogic, this can be called multiple times for the same logic identifier, to register different commands
    /// @return if the command was not registered because the config didn't parse successfully
    public RegisterResult updateCommand(ConfigWarns.Named warns, DefinedCommand defined, ConfigurationSection config, ConfigService service, boolean firstRun) {
        CommandIdentifier<?> registered = commandIdentifiers.get(defined.getLogicIdentifier());
        if (registered == null) {
            return RegisterResult.NOT_FOUND; // logic not found
        }

        // create the command object for the defined command name
        boolean alreadyRegistered = registeredCommandNames.containsKey(defined.getName());
        CommandLogic logic = alreadyRegistered? registeredCommandNames.get(defined.getName()) : registered.getCommandCreation().apply(defined);

        // (re-)parse the config for this specific command
        if (!logic.parseConfig(warns, config, service)) {
            return RegisterResult.FAIL; // config parsing failed
        }

        if (alreadyRegistered || !firstRun) {
            return RegisterResult.ALREADY_REGISTERED; // cannot register another time
        }

        logic.registerCommand();
        registeredCommandNames.put(defined.getName(), logic);
        return RegisterResult.REGISTERED;
    }

    private static final EntryReplacement FILL_COMMAND = new EntryReplacement("command");

    public static Component getSimpleUsage(String label) {
        return Language.COMMAND_INCOMPLETE.with(
            FILL_COMMAND.filled("/%s (%s)".formatted(label, Language.WORD_ARGUMENTS))
        );
    }
}
