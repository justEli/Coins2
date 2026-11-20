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

    public DropsConfig getDropsConfig() {
        return dropsConfig;
    }

    public CurrenciesConfig getCurrenciesConfig() {
        return currenciesConfig;
    }

    public void reload() {
        coins.getConfigWarns().clearWarnings();

        mainConfig.parseAndReload();
        currenciesConfig.parseAndReload();

        if (currenciesConfig.getDefinedKeys().isEmpty()) {
            coins.getLogger().severe("""
                No currencies registered: Coin variants and drops cannot be registered, because there is no \
                currency to depend on. Please install an appropriate economy for this plugin to register a currency."""
            );
        }
        else {
            // can only define coins and drops if there's at least 1 currency
            coinsConfig.parseAndReload();
            dropsConfig.parseAndReload();
        }

        commandsConfig.parseAndReload();
        languageParser.reloadLanguage();

        int size = coins.getConfigWarns().getWarnings();
        if (size == 0) {
            return;
        }

        coins.log(Level.WARNING, "Loaded the configs of Coins with %,d warnings. See above here for details.".formatted(size));
    }
}
