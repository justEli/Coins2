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
    private final String identifier;
    private final String label;
    private final List<String> aliases;
    private final @Nullable String permission;

    /// @param identifier command identifier to use in the config
    /// @param labels all labels for the command, will be split into main label and aliases
    /// @param permission required permission, or null if no permission required
    public DefinedCommand(String identifier, List<String> labels, @Nullable String permission) {
        if (labels.isEmpty()) {
            throw new IllegalArgumentException("Command cannot be defined if it has no labels");
        }

        this.identifier = identifier.toLowerCase();
        this.label = labels.removeFirst();
        this.aliases = labels;
        this.permission = permission;
    }

    /// @return identifier of this command, used in the config
    public String getIdentifier() {
        return identifier;
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
