package community.coins.plugin.command;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * @author Eli
 * @since May 07, 2026
 */
@NullMarked
public final class DefinedCommand {
    private final String name;
    private final String logicIdentifier;
    private final String label;
    private final List<String> aliases;
    private final @Nullable String permission;

    /// @param name command identifier to use in the config
    /// @param labels all labels for the command, will be split into main label and aliases
    /// @param permission required permission, or null if no permission required
    public DefinedCommand(String name, String logicIdentifier, List<String> labels, @Nullable String permission) {
        if (labels.isEmpty()) {
            throw new IllegalArgumentException("Command cannot be defined if it has no labels");
        }

        this.name = name;
        this.logicIdentifier = logicIdentifier;
        this.label = labels.removeFirst();
        this.aliases = labels;
        this.permission = permission;
    }

    /// @return named identifier of this configured command, used in the config
    public String getName() {
        return name;
    }

    /// @return logic identifier, which tells what logic to give to the command
    public String getLogicIdentifier() {
        return logicIdentifier;
    }

    /// @return main command label
    public String getLabel() {
        return label;
    }

    /// @return other command labels, excluding the main command label
    public List<String> getAliases() {
        return aliases;
    }

    /// @return required permission, or null if no permission required
    public @Nullable String getPermission() {
        return permission;
    }
}
