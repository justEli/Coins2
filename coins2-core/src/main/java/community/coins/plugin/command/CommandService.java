package community.coins.plugin.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eli
 * @since May 07, 2026
 */
public final class CommandService {
    private final Map<String, CommandLogic> commandLogics = new HashMap<>();

    /// called from CommandLogic on the implementing platform
    public void registerLogic(CommandLogic logic) {
        commandLogics.put(logic.getIdentifier(), logic);
    }

    /// used by commands.yml
    /// @return false if the logic identifier is not registered
    public boolean implementCommand(String logicIdentifier, List<String> labels, String permission) {
        CommandLogic logic = commandLogics.get(logicIdentifier.toLowerCase());
        if (logic == null) {
            return false;
        }

        logic.register(labels, permission);
        return true;
    }
}
