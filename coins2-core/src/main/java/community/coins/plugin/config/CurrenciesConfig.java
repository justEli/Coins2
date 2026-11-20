package community.coins.plugin.config;

import com.zaxxer.hikari.HikariConfig;
import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.economy.EconomyHook;
import community.coins.plugin.economy.storage.BalanceData;
import community.coins.plugin.economy.storage.CurrencyStorage;
import community.coins.plugin.economy.storage.FileStorage;
import community.coins.plugin.economy.storage.SqlConnection;
import community.coins.plugin.economy.storage.SqlStorage;
import community.coins.plugin.misc.MetricsHandler;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.util.Util;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class CurrenciesConfig extends FileConfig<DefinedCurrency> {
    private final SqlConnection sql;
    public CurrenciesConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "currencies.yml");
        this.sql = new SqlConnection(coins);
    }

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();

        // always close the previous sql connection first
        sql.closeConnection();

        // database config first
        boolean sqlEnabled = config.getBoolean("storage.sql.enabled", false);
        if (sqlEnabled) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getString("storage.sql.url"));
            hikariConfig.setUsername(config.getString("storage.sql.username"));
            hikariConfig.setPassword(config.getString("storage.sql.password"));
            sql.openConnection(hikariConfig);
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

        // clear metrics
        MetricsHandler.USING_ECONOMY_VAULT = false;
        MetricsHandler.STORAGE_INTEGRATION = "none";

        Map<String, DefinedCurrency> previousCurrencies = new HashMap<>(defined); // track which ones were removed
        Map<String, DefinedCurrency> configured = new HashMap<>();

        for (String name : currenciesSection.getKeys(false)) {
            ConfigurationSection section = currenciesSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            String id = Util.toIdentifier(name);

            // todo if a new currency is added on reload (doesn't contain previousCurrencies) show them
            //  a warning that it may not be as expected because the integration got registered in runtime.
            //  but this would only be for another economy integration than Vault that supports multi currency

            String pluginName = section.getString("economy");
            boolean integrationEnabled = section.getBoolean("integration.enabled", defaultIntegrationEnabled);

            // don't allow to change the economy plugin or integration toggle during a reload
            DefinedCurrency previousCurrency = previousCurrencies.get(id); // from before a reload
            boolean hasPreviousCurrency = previousCurrency != null;

            if (hasPreviousCurrency) {
                if (!previousCurrency.getHook().getPluginName().equals(pluginName)) {
                    // if plugin name at `economy` changed compared to the previous settings of the currency
                    warn("""
                        The economy plugin to use (at `economy`) for currency '%s' cannot be changed during a reload. \
                        Rebooting the server allows the change.""".formatted(id)
                    );
                    pluginName = previousCurrency.getHook().getPluginName();
                }

                // previousStorage should never be null, unless there are different currencies for a plugin that
                //  doesn't support it, but that shouldn't happen
                var previousStorage = previousCurrency.getHook().getCurrencyStorage(previousCurrency);
                if (previousStorage != null && (previousStorage.getStorage() == null) == integrationEnabled) {
                    // if `integration.enabled` changed compared to the previous settings of the currency
                    warn("""
                        Plugin integration (at `integration.enabled`) for currency '%s' cannot be changed during a \
                        reload. Rebooting the server allows the change.""".formatted(id)
                    );
                    integrationEnabled = previousStorage.getStorage() != null;
                }
            }

            previousCurrencies.remove(id); // this currency still exists, so it was not removed

            if (pluginName == null) {
                warn("Cannot register currency '%s' because no economy or plugin is provided.".formatted(id));
                continue;
            }

            Optional<EconomyHook> plugin = coins.getEconomyService().getSupportedEconomy(pluginName);
            if (plugin.isEmpty()) {
                warn("Cannot register currency '%s', because '%s' is not a supported plugin.".formatted(id, pluginName));
                continue;
            }

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
                warn("Cannot set deposit message position for currency '%s' because '%s' is invalid."
                    .formatted(id, depositPosition)
                );
            }

            if (decimals > BalanceData.MAX_DECIMALS) {
                decimals = BalanceData.MAX_DECIMALS;
                warn("Using %d decimals for currency '%s' as this is the highest allowed amount of decimals."
                    .formatted(BalanceData.MAX_DECIMALS, id)
                );
            }

            EconomyHook hook = plugin.get();
            DefinedCurrency definedCurrency =
                new DefinedCurrency(id, hook, decimals, symbol, singularName, pluralName, format, depositMessage, position);

            if (!hook.isAllowedToRegister(definedCurrency)) {
                warn("""
                    Cannot register currency '%s' for plugin '%s' as it only supports one currency at most. Another currency \
                    is already using this plugin integration. If this is a reload, you could reboot the server to try and \
                    register the currency."""
                    .formatted(id, pluginName)
                );
                continue;
            }

            boolean integrationSupported = hook.isIntegrationSupported();
            if (integrationEnabled && !integrationSupported) {
                warn("Cannot register economy integration for currency '%s' because it is not supported for plugin '%s'."
                    .formatted(id, pluginName)
                );
                integrationEnabled = false;
            }

            // first we attempt to register a currency integration with the current plugin
            if (integrationEnabled) {
                // register that this currency should be handled by this plugin
                CurrencyStorage storage = null;
                if ("sql".equalsIgnoreCase(integrationStorage)) {
                    if (sqlEnabled) {
                        storage = new SqlStorage(sql, definedCurrency.getIdentifier());
                        MetricsHandler.STORAGE_INTEGRATION = "sql";
                    }
                    else {
                        warn("Cannot register economy integration '%s' for currency '%s' because SQL storage is not configured."
                            .formatted(pluginName, id)
                        );
                    }
                }
                else if ("file".equalsIgnoreCase(integrationStorage)) {
                    storage = new FileStorage(coins, definedCurrency.getIdentifier());
                    MetricsHandler.STORAGE_INTEGRATION = "file";
                }

                if (storage == null) {
                    warn("Cannot register economy integration '%s' for currency '%s' because storage '%s' is invalid."
                        .formatted(pluginName, id, integrationStorage)
                    );
                }
                else {
                    // either register the currency to be integrated by this plugin, or update the currency's storage
                    definedCurrency.getHook().updateIntegration(configWarns, definedCurrency, storage);
                }
            }

            // register or update the currency for the given plugin hook (either handled by this plugin or another)
            RegisterResult registeredEconomy = hook.registerEconomy(configWarns, definedCurrency);
            if (registeredEconomy == RegisterResult.NOT_FOUND || registeredEconomy == RegisterResult.FAIL) {
                warn("Cannot register currency '%s' because plugin '%s' is not properly installed, or is not supported."
                    .formatted(id, pluginName)
                );
                continue;
            }

            configured.put(id, definedCurrency);
        }

        // currencies that were removed from the config after reload
        for (var entry : previousCurrencies.entrySet()) {
            warn("""
                Currency '%s' was removed from the config, which can cause problems with the current setup. Please reboot the \
                server to confirm the removal of this currency. For now, it will continue to function using the old settings. \
                You can simply re-add the currency and reload, to get the currency to work properly again."""
                .formatted(entry.getKey())
            );

            // add them again with their old settings
            configured.put(entry.getKey(), entry.getValue());
        }

        putDefined(configured, "currency", "currencies");

        if (configured.isEmpty()) {
            warn("No currencies have been registered. This plugin needs at least one currency to function.");
        }
    }
}
