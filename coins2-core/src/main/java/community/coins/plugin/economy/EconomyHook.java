package community.coins.plugin.economy;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.config.RegisterResult;
import community.coins.plugin.economy.storage.CurrencyBalanceStorage;
import community.coins.plugin.economy.storage.CurrencyStorage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is created once on initialization for each supported plugin.
 * @see EconomyService#getSupportedEconomy(String)
 * @author Eli
 * @since May 05, 2026
 */
@NullMarked
public abstract class EconomyHook {
    protected final CoinsCore coins;
    protected final EconomyService service;
    protected final String pluginName;

    // class that holds balance cache per currency; should not be changed, because other plugins may indirectly depend on it.
    // however, the storage implementation inside this (sql or file) can be changed/re-set
    private final Map<String, CurrencyBalanceStorage> currencies = new HashMap<>(); // don't remove from this map on reload

    /// @param pluginName not an identifier, but the case-sensitive (plugin) name of the hook
    public EconomyHook(CoinsCore coins, EconomyService service, String pluginName) {
        this.coins = coins;
        this.service = service;
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    protected boolean isPluginEnabled() {
        return coins.getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    /// @return null if there was already a different currency registered and no more than one is allowed
    public @Nullable CurrencyBalanceStorage getCurrencyStorage(DefinedCurrency currency) {
        if (!isAllowedToRegister(currency)) {
            return null;
        }
        return currencies.computeIfAbsent(currency.getIdentifier(), _ -> new CurrencyBalanceStorage(coins));
    }

    /// checks {@code isMultiCurrencySupported()} and whether there is no other currency already installed
    public boolean isAllowedToRegister(DefinedCurrency currency) {
        if (!isMultiCurrencySupported() && !currencies.isEmpty()) {
            return currencies.containsKey(currency.getIdentifier());
        }
        return true;
    }

    /// if it has support for more than one currency
    public abstract boolean isMultiCurrencySupported();

    /// if it has support for being integrated as economy provider
    public abstract boolean isIntegrationSupported();

    /// update or register that an economy integration should be handled by this plugin and its currency.
    /// should be run <b>before</b> {@code registerEconomy}
    /// @see #registerEconomy(ConfigWarns.Named, DefinedCurrency)
    public RegisterResult updateIntegration(ConfigWarns.Named warns, DefinedCurrency currency, CurrencyStorage storage) {
        if (!isPluginEnabled()) {
            warns.warn("Cannot register integration for currency '%s' because plugin '%s' is not enabled."
                .formatted(currency.getIdentifier(), pluginName)
            );
            return RegisterResult.NOT_FOUND;
        }

        // while the CurrencyBalanceStorage (with its cache) is never recreated, its storage can be recreated
        CurrencyBalanceStorage currencyStorage = getCurrencyStorage(currency);
        if (currencyStorage == null) {
            // this should be impossible, because isAllowedToRegister should always be checked first
            return RegisterResult.FAIL;
        }

        currencyStorage.updateStorageImplementation(storage);
        return registerProvider(warns, currency);
    }

    /// register that an economy integration should be handled by this plugin and its currency
    protected abstract RegisterResult registerProvider(ConfigWarns.Named warns, DefinedCurrency currency);

    /// register the economy, either handled by this plugin or another plugin.
    /// run <b>after</b> {@code updateIntegration} or {@code registerProvider}, otherwise it won't detect our own integration
    /// @see #updateIntegration(ConfigWarns.Named warns, DefinedCurrency currency, CurrencyStorage storage)
    public abstract RegisterResult registerEconomy(ConfigWarns.Named warns, DefinedCurrency currency);

    public abstract double getBalance(UUID uuid, String currency);

    public abstract boolean canAfford(UUID uuid, String currency, double amount);

    public abstract boolean deposit(UUID uuid, String currency, double amount);

    public abstract boolean withdraw(UUID uuid, String currency, double amount);
}
