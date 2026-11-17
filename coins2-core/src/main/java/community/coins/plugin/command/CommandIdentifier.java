package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;

import java.util.function.Function;

/**
 * @author Eli
 * @since November 17, 2026
 */
public abstract class CommandIdentifier<T extends CommandLogic> {
    protected final CoinsCore coins;
    protected final String identifier;
    protected final Function<DefinedCommand, T> commandCreation;

    public CommandIdentifier(CoinsCore coins, Function<DefinedCommand, T> commandCreation, String identifier) {
        this.coins = coins;
        this.identifier = identifier;
        this.commandCreation = commandCreation;

        coins.getCommandService().registerIdentifier(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Function<DefinedCommand, T> getCommandCreation() {
        return commandCreation;
    }
}
