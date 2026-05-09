package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.economy.EconomyHook;
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
    public CurrenciesConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "currencies.yml");
    }

    public static boolean USING_VAULT_CURRENCY = false;

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
            addWarn("Cannot register currencies because section for defining currencies is missing.");
            return;
        }

        // first clear all currencies before registering all the configured ones
        coins.getEconomyService().clearRegisteredCurrencies();

        Map<String, DefinedCurrency> configured = new HashMap<>();
        for (String name : currenciesSection.getKeys(false)) {
            ConfigurationSection section = currenciesSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            String id = Util.toIdentifier(name);
            String economyName = section.getString("economy");
            if (economyName == null) {
                addWarn("Cannot register currency '%s' because no economy or plugin is provided.".formatted(id));
                continue;
            }

            Optional<EconomyHook> economy = coins.getEconomyService().getEconomy(economyName);
            if (economy.isEmpty()) {
                addWarn("Cannot register currency '%s' because economy '%s' is not supported.".formatted(id, economyName));
                continue;
            }

            // small bStats metric here
            switch (economyName) {
                case "Vault" -> USING_VAULT_CURRENCY = true;
            }

            int decimals = section.getInt("decimals", defaultDecimals);
            String symbol = section.getString("symbol", defaultSymbol);
            String singularName = section.getString("name.singular", defaultSingularName);
            String pluralName = section.getString("name.plural", defaultPluralName);
            String format = section.getString("format", defaultFormat);
            String depositMessage = section.getString("deposit.message", defaultDepositMessage);
            String depositPosition = section.getString("deposit.position", defaultDepositPosition);
            MessagePosition position = Util.getEnum(MessagePosition.class, depositPosition);

            DefinedCurrency definedCurrency = new DefinedCurrency(
                id, economy.get(), decimals, symbol, singularName, pluralName, format, depositMessage, position
            );

            // register the currency for the given plugin/economy
            if (coins.getEconomyService().registerCurrency(definedCurrency, configWarns)) {
                configured.put(id, definedCurrency);
            }
        }

        putDefinedItems(configured, "currency", "currencies");
    }
}
