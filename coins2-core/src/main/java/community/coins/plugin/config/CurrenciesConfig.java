package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.economy.EconomyHook;
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
    public CurrenciesConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "currencies.yml");
    }

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();

        int defaultDecimals = config.getInt("default.decimals", 2);
        String defaultSymbol = config.getString("default.symbol", "¢");
        String defaultSingularName = config.getString("default.name.singular", "Coin");
        String defaultPluralName = config.getString("default.name.plural", "Coins");
        String defaultFormat = config.getString("default.format", "<#6DD47E>{amount}{symbol}");
        String defaultDepositMessage = config.getString("default.deposit.message", "<#148C30>↑ {format}");
        String defaultDepositPosition = config.getString("default.deposit.position", "actionbar");

        ConfigurationSection currenciesSection = config.getConfigurationSection("currencies");
        if (currenciesSection == null) {
            addWarn("There are no defined currencies in the config, `currencies` section missing.");
            return;
        }

        // first clear all currencies before registering all the configured ones
        coins.getEconomyService().clearRegisteredCurrencies();

        Map<String, DefinedCurrency> configured = new HashMap<>();
        for (String name : currenciesSection.getKeys(false)) {
            ConfigurationSection section = currenciesSection.getConfigurationSection(name);
            if (section == null) {
                coins.debug("Skipping currency config entry for '%s', as nothing is configured.".formatted(name));
                continue;
            }

            String economyName = section.getString("economy");
            if (economyName == null) {
                addWarn("No plugin provided for currency '%s'.".formatted(name));
                continue;
            }

            Optional<EconomyHook> economy = coins.getEconomyService().getEconomy(economyName);
            if (economy.isEmpty()) {
                addWarn("No supported plugin '%s' found for currency '%s' at `economy`.".formatted(economyName, name));
                continue;
            }

            int decimals = section.getInt("decimals", defaultDecimals);
            String symbol = section.getString("symbol", defaultSymbol);
            String singularName = section.getString("name.singular", defaultSingularName);
            String pluralName = section.getString("name.plural", defaultPluralName);
            String format = section.getString("format", defaultFormat);
            String depositMessage = section.getString("deposit.message", defaultDepositMessage);
            String depositPosition = section.getString("deposit.position", defaultDepositPosition);
            MessagePosition position = Util.getEnum(MessagePosition.class, depositPosition);

            String id = name.toLowerCase();
            DefinedCurrency definedCurrency = new DefinedCurrency(
                id, economy.get(), decimals, symbol, singularName, pluralName, format, depositMessage, position
            );

            // register the currency for the given plugin/economy
            coins.getEconomyService().registerCurrency(definedCurrency, configWarns);

            configured.put(id, definedCurrency);
        }

        putDefinedItems(configured, "currency", "currencies");
    }
}
