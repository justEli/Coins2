package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Files;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 30, 2026
 */
public abstract class BasicConfig {
    protected final CoinsCore coins;
    protected final ConfigService service;
    protected final String fileName;
    protected final ConfigWarns.Named configWarns;

    public BasicConfig(CoinsCore coins, ConfigService service, String fileName) {
        this.coins = coins;
        this.service = service;
        this.fileName = fileName;
        this.configWarns = coins.getConfigWarns().create(fileName);
    }

    public YamlConfiguration getOrCreateConfig() {
        var configFile = coins.getDataFolder().toPath().resolve(fileName);
        if (!Files.exists(configFile)) {
            coins.saveResource(fileName, false);
        }

        return YamlConfiguration.loadConfiguration(configFile.toFile());
    }

    public void warn(String message) {
        configWarns.warn(message);
    }

    public void info(String message) {
        coins.log(Level.INFO, "[%s]: %s".formatted(fileName, message));
    }

    public abstract void parseAndReload();
}
