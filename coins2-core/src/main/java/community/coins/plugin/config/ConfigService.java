package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.language.LanguageParser;

import java.util.logging.Level;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class ConfigService {
    private final CoinsCore coins;
    private final MainConfig mainConfig;
    private final CurrenciesConfig currenciesConfig;
    private final CoinsConfig coinsConfig;
    private final DropsConfig dropsConfig;
    private final CommandsConfig commandsConfig;
    private final LanguageParser languageParser;

    // coin types (and event types) are pretty stand-alone, they don't depend on other features
    // drops however, depend on coin types and event types
    // nothing depends on language, so that can always go last
    public ConfigService(CoinsCore coins) {
        this.coins = coins;

        this.mainConfig = new MainConfig(coins, this);
        this.currenciesConfig = new CurrenciesConfig(coins, this);
        this.coinsConfig = new CoinsConfig(coins, this);
        this.dropsConfig = new DropsConfig(coins, this);
        this.commandsConfig = new CommandsConfig(coins, this);
        this.languageParser = new LanguageParser(coins);

        reload();
    }

    // different configs

    public CoinsConfig getCoinsConfig() {
        return coinsConfig;
    }

    public void reload() {
        coins.getConfigWarns().clearWarnings();

        mainConfig.parseAndReload();
        currenciesConfig.parseAndReload();
        coinsConfig.parseAndReload();
        dropsConfig.parseAndReload();
        commandsConfig.parseAndReload();
        languageParser.reloadLanguage();

        int size = coins.getConfigWarns().getWarnings();
        if (size == 0) {
            return;
        }

        coins.log(Level.WARNING, """
            Loaded the configs of Coins with %,d warnings. See above here for details."""
            .formatted(size)
        );
    }
}
