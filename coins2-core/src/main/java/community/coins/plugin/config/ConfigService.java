package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.language.LanguageParser;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class ConfigService {
    private final CoinsCore coins;
    private final ConfigParser configParser;
    private final CoinsConfig coinsConfig;
    private final DropsConfig dropsConfig;
    private final LanguageParser languageParser;

    // coin types (and event types) are pretty stand-alone, they don't depend on other features
    // drops however, depend on coin types and event types
    // nothing depends on language, so that can always go last
    public ConfigService(CoinsCore coins) {
        this.coins = coins;
        this.configParser = new ConfigParser(coins, this);
        this.coinsConfig = new CoinsConfig(coins, this);
        this.dropsConfig = new DropsConfig(coins, this);
        this.languageParser = new LanguageParser(coins, this);

        reload();
    }

    // different configs

    public CoinsConfig getCoinsConfig() {
        return coinsConfig;
    }

    public DropsConfig getDropsConfig() {
        return dropsConfig;
    }

    public void reload() {
        warnings.set(0);

        configParser.parseAndInject(ConfigYml.class);
        coinsConfig.parseAndReload();
        dropsConfig.parseAndReload();
        languageParser.reloadLanguage();

        if (warnings.get() == 0) {
            return;
        }

        coins.log(Level.WARNING, """
            Loaded the config of Coins with %d warnings. See above here for details.""".formatted(warnings.get())
        );
    }

    // config util

    public YamlConfiguration getOrCreateConfig(String fileName) {
        var configFile = coins.getDataFolder().toPath().resolve(fileName);

        if (!Files.exists(configFile)) {
            coins.saveResource(fileName, false);
        }

        return YamlConfiguration.loadConfiguration(configFile.toFile());
    }

    // warnings

    private final AtomicInteger warnings = new AtomicInteger(0);

    public void addWarning(String message) {
        int warning = warnings.incrementAndGet();
        coins.log(Level.WARNING, "#%,d: %s".formatted(warning, message));
    }

    public void printConfigWarning(String config, String message) {
        int warning = warnings.incrementAndGet();
        coins.log(Level.WARNING, "[%s] #%,d: %s".formatted(config, warning, message));
    }
}
