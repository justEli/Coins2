package community.coins.plugin.command;

import java.util.List;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class DefinedCommand {
    private final String identifier;
    private final String label;

    public DefinedCommand(String identifier, List<String> labels) {
        this.identifier = identifier.toLowerCase();
        this.label = labels.getFirst();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLabel() {
        return label;
    }
}
