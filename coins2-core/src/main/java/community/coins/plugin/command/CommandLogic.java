package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author Eli
 * @since May 07, 2026
 */
public abstract class CommandLogic<T extends CoinsCore> {
    protected final T coins;
    protected final String identifier;

    public CommandLogic(T coins, CommandService service, String identifier) {
        this.coins = coins;
        this.identifier = identifier.toLowerCase();
        service.registerLogic(this);
    }

    public String getIdentifier() {
        return identifier;
    }

    /// register the command implementation for the appropriate platform
    public abstract void register(DefinedCommand defined);

    public abstract String getDescription();

    /// @return true if the parsing was successful and the command can be registered
    public abstract boolean parseConfig(ConfigWarns.Named warns, ConfigurationSection section, ConfigService service);

    /// @param remaining should already be lowercase
    public static void suggestStartsWith(Collection<? extends String> items, String remaining, Consumer<String> item) {
        for (String name : items) {
            if (name.toLowerCase().startsWith(remaining)) {
                item.accept(name);
            }
        }
    }
}
