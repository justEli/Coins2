package community.coins.plugin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.economy.EconomyHook;
import community.coins.plugin.economy.storage.BalanceData;
import community.coins.plugin.economy.storage.CurrencyBalanceStorage;
import community.coins.plugin.economy.storage.CurrencyStorage;
import community.coins.plugin.economy.storage.FileStorage;
import community.coins.plugin.economy.storage.SqlStorage;
import community.coins.plugin.misc.MetricsHandler;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class CurrenciesConfig extends FileConfig<DefinedCurrency> {
    public CurrenciesConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "currencies.yml");

        coins.addShutdownTask(() -> {
            closeConnection(); // submit to sql thread that connection must be closed
            try {
                // start closing the thread and wait 10 seconds at most for tasks to finish and connection to close
                SQL_EXECUTOR.shutdown();
                if (!SQL_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                    SQL_EXECUTOR.shutdownNow();
                }
            }
            catch (InterruptedException _) {
                SQL_EXECUTOR.shutdownNow();
            }
        });
    }

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();

        // always close the previous sql connection first
        closeConnection();

        // database config first
        boolean sqlEnabled = config.getBoolean("storage.sql.enabled", false);
        if (sqlEnabled) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getString("storage.sql.url"));
            hikariConfig.setUsername(config.getString("storage.sql.username"));
            hikariConfig.setPassword(config.getString("storage.sql.password"));
            SQL_EXECUTOR.submit(() -> {
                try {
                    this.hikari = new HikariDataSource(hikariConfig);
                }
                catch (Exception exception) {
                    Throwable cause = exception.getCause();
                    coins.log(Level.SEVERE,
                        "Failed to establish SQL connection: " + exception.getMessage()
                        + (cause == null? "" : " (%s)".formatted(cause.getMessage()))
                    );
                }
            });
        }

        // default values for currencies
        int defaultDecimals = config.getInt("default.decimals", 2);
        boolean defaultIntegrationEnabled = config.getBoolean("default.integration.enabled", false);
        String defaultIntegrationStorage = config.getString("default.integration.storage");
        String defaultSymbol = config.getString("default.symbol", "¢");
        String defaultSingularName = config.getString("default.name.singular", "Coin");
        String defaultPluralName = config.getString("default.name.plural", "Coins");
        String defaultFormat = config.getString("default.format", "<#6DD47E>{amount}{symbol}");
        String defaultDepositMessage = config.getString("default.deposit.message", "<#148C30>↑ {format}");
        String defaultDepositPosition = config.getString("default.deposit.position", "actionbar");

        ConfigurationSection currenciesSection = config.getConfigurationSection("currencies");
        if (currenciesSection == null) {
            warn("Cannot register currencies because section for defining currencies is missing.");
            return;
        }

        // clear all currencies and reset economies before parsing all updates
        coins.getEconomyService().clearEconomies();

        // clear metrics
        MetricsHandler.USING_ECONOMY_VAULT = false;
        MetricsHandler.STORAGE_INTEGRATION = "none";

        Map<String, DefinedCurrency> configured = new HashMap<>();
        for (String name : currenciesSection.getKeys(false)) {
            ConfigurationSection section = currenciesSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            String id = Util.toIdentifier(name);
            String economyName = section.getString("economy");
            if (economyName == null) {
                warn("Cannot register currency '%s' because no economy or plugin is provided.".formatted(id));
                continue;
            }

            Optional<EconomyHook> plugin = coins.getEconomyService().getSupportedEconomy(economyName);
            if (plugin.isEmpty()) {
                warn("Cannot register currency '%s', because '%s' is not a supported plugin.".formatted(id, economyName));
                continue;
            }

            boolean integrationEnabled = section.getBoolean("integration.enabled", defaultIntegrationEnabled);
            String integrationStorage = section.getString("integration.storage", defaultIntegrationStorage);
            int decimals = section.getInt("decimals", defaultDecimals);
            String symbol = section.getString("symbol", defaultSymbol);
            String singularName = section.getString("name.singular", defaultSingularName);
            String pluralName = section.getString("name.plural", defaultPluralName);
            String format = section.getString("format", defaultFormat);
            String depositMessage = section.getString("deposit.message", defaultDepositMessage);
            String depositPosition = section.getString("deposit.position", defaultDepositPosition);

            MessagePosition position = Util.getEnum(MessagePosition.class, depositPosition);
            if (position == null) {
                position = MessagePosition.ACTIONBAR;
                warn("""
                    Cannot set deposit message position for currency '%s' because '%s' is invalid."""
                    .formatted(id, depositPosition)
                );
            }

            if (decimals > BalanceData.MAX_DECIMALS) {
                decimals = BalanceData.MAX_DECIMALS;
                warn("Using %d decimals for currency '%s' as this is the highest allowed amount of decimals."
                    .formatted(BalanceData.MAX_DECIMALS, id)
                );
            }

            DefinedCurrency definedCurrency = new DefinedCurrency(
                id, plugin.get(), decimals, symbol, singularName, pluralName, format, depositMessage, position
            );

            if (integrationEnabled) {
                if (!plugin.get().isIntegrationSupported()) {
                    warn("Cannot add economy integration for currency '%s' because it is not supported for plugin '%s'."
                        .formatted(id, economyName)
                    );
                }
                else {
                    // register that this currency should be handled by this plugin
                    CurrencyStorage storage = null;
                    if ("sql".equalsIgnoreCase(integrationStorage)) {
                        if (sqlEnabled) {
                            storage = new SqlStorage(coins, service, definedCurrency.getIdentifier());
                            MetricsHandler.STORAGE_INTEGRATION = "sql";
                        }
                        else {
                            warn("Cannot register integration for currency '%s' because SQL storage is not configured.".formatted(id));
                        }
                    }
                    else if ("file".equalsIgnoreCase(integrationStorage)) {
                        storage = new FileStorage(coins, definedCurrency.getIdentifier());
                        MetricsHandler.STORAGE_INTEGRATION = "file";
                    }

                    if (storage == null) {
                        warn("Cannot register integration for currency '%s' because storage '%s' is invalid."
                            .formatted(id, integrationStorage)
                        );
                    }
                    else {
                        definedCurrency.getHook().registerIntegration(
                            configWarns, definedCurrency, new CurrencyBalanceStorage(coins, storage)
                        );
                    }
                }
            }

            // register the currency for the given plugin/economy
            if (coins.getEconomyService().registerCurrency(configWarns, definedCurrency)) {
                configured.put(id, definedCurrency);
            }
        }

        putDefinedItems(configured, "currency", "currencies");

        if (configured.isEmpty()) {
            warn("No currencies have been registered. This plugin needs at least one currency to function.");
        }
    }

    // queue for submitting file changes
    public static final ExecutorService SQL_EXECUTOR = Executors.newSingleThreadExecutor();
    static {
        SQL_EXECUTOR.submit(() -> Thread.currentThread().setName("async-sql-io-thread"));
    }

    private HikariDataSource hikari;

    /// gets an SQL connection from the pool
    public @Nullable Connection getConnection() throws SQLException {
        if (hikari == null) {
            return null;
        }

        return hikari.getConnection();
    }

    /// submits to the single threaded sql executor that the connection must be closed
    private void closeConnection() {
        SQL_EXECUTOR.submit(() -> {
            if (hikari != null && !hikari.isClosed()) {
                hikari.close();
            }
        });
    }
}
