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
public abstract class CommandLogic {
    protected final CoinsCore coins;
    protected final DefinedCommand defined;

    public CommandLogic(CoinsCore coins, DefinedCommand defined) {
        this.coins = coins;
        this.defined = defined;
    }

    /// register the command implementation for the appropriate platform
    public abstract void registerCommand();

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
