package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;

import java.util.List;

/**
 * @author Eli
 * @since May 07, 2026
 */
public abstract class CommandLogic {
    protected final CoinsCore coins;
    protected final String identifier;

    public CommandLogic(CoinsCore coins, CommandService service, String identifier) {
        this.coins = coins;
        this.identifier = identifier.toLowerCase();
        service.registerLogic(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract void register(List<String> labels, String permission);

    public abstract String getDescription();
}
