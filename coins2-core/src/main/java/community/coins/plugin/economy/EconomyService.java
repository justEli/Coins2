package community.coins.plugin.economy;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.economy.hook.VaultEconomyHook;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class EconomyService implements Listener {
    private final CoinsCore coins;
    public EconomyService(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    // supported economies

    private void loadSupportedEconomies() {
        addSupportedPlugin(new VaultEconomyHook(coins, this));
    }

    // String = plugin's name (case-sensitive)
    private final Map<String, EconomyHook> economyHooks = new HashMap<>();

    private void addSupportedPlugin(EconomyHook hook) {
        economyHooks.put(hook.getPluginName(), hook);
    }

    public Optional<EconomyHook> getSupportedEconomy(String pluginName) {
        return Optional.ofNullable(economyHooks.get(pluginName));
    }

    // registering currencies

    private final Map<String, DefinedCurrency> currencies = new HashMap<>();
    private final Map<String, Integer> pluginCurrenciesRegistered = new HashMap<>();

    @NullMarked
    public boolean registerCurrency(ConfigWarns.Named warns, DefinedCurrency currency) {
        EconomyHook hook = currency.getHook();
        String pluginName = currency.getHook().getPluginName();

        if (!hook.isMultiCurrencySupported() && getAmountOfCurrencies(hook) > 0) {
            warns.warn("""
                Cannot register currency '%s' for plugin '%s' as it only supports one currency at most."""
                .formatted(currency.getIdentifier(), pluginName)
            );
            return false;
        }

        // register the currency at the economy
        boolean registered = hook.registerCurrency(warns, currency);
        if (!registered) {
            warns.warn("""
                Cannot register currency '%s' because plugin '%s' is not properly installed, or is not supported."""
                .formatted(currency.getIdentifier(), pluginName)
            );
            return false;
        }

        currencies.put(currency.getIdentifier(), currency); // save currency

        // update counter
        int amount = pluginCurrenciesRegistered.computeIfAbsent(pluginName, _ -> 0);
        pluginCurrenciesRegistered.put(pluginName, amount + 1);
        return true;
    }

    public Optional<DefinedCurrency> getCurrency(@NotNull String currency) {
        return Optional.ofNullable(currencies.get(currency.toLowerCase()));
    }

    public int getAmountOfCurrencies(@NotNull EconomyHook plugin) {
        return pluginCurrenciesRegistered.computeIfAbsent(plugin.getPluginName(), _ -> 0);
    }

    // this is always called just before currencies.yml is parsed (so on server start and /mintage reload)
    public void clearEconomies() {
        // clear currencies
        currencies.clear();
        pluginCurrenciesRegistered.clear();

        // clear economies
        economyHooks.values().forEach(EconomyHook::unregister);
        economyHooks.clear();
        loadSupportedEconomies();
    }

    public Collection<String> getCurrencyIdentifiers() {
        return currencies.keySet();
    }

    // coin deposits through coin items

    /// deposit the coin into the right currency and value, including deposit message and pickup sound
    /// @return true if successful deposit of coin
    @NullMarked
    public boolean depositCoin(Player player, ItemStack coin) {
        Optional<DefinedCurrency> currency = coins.getCoinMeta().getCoinDefinedCurrency(coin);
        if (currency.isEmpty()) {
            String notFoundCurrency = coins.getCoinMeta().getCoinCurrency(coin).orElse("unknown");
            coins.getLogger().warning("""
                Attached currency '%s' to coin with item type '%s' was not found. Consider to add it to 'currencies.yml' again."""
                .formatted(notFoundCurrency, coin.getType().getKey())
            );
            return false;
        }

        OptionalDouble value = coins.getCoinMeta().getCoinValue(coin);
        if (value.isEmpty()) {
            return false;
        }

        double amount = value.getAsDouble() * coin.getAmount();
        currency.get().submitTransaction(transaction -> {
            if (transaction.deposit(player.getUniqueId(), amount)) {
                sendDepositMessage(currency.get(), player, amount);
                coins.getCoinMeta().playSound(player, coin);
            }
        });
        return true;
    }

    // coin pickup messages

    private final Map<UUID, Double> pickupAmountCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pickupTimeCache = new ConcurrentHashMap<>();

    private static final long ACCUMULATE_MILLIS = 1500;

    @NullMarked
    public void sendDepositMessage(DefinedCurrency currency, Player player, double amount) {
        UUID uuid = player.getUniqueId();

        double displayAmount;
        if (currency.getDepositPosition() == MessagePosition.CHAT) {
            displayAmount = amount; // doesn't have to accumulate in chat
        }
        else {
            if (pickupTimeCache.computeIfAbsent(uuid, _ -> 0L) > System.currentTimeMillis() - ACCUMULATE_MILLIS) {
                // recently shown actionbar/title
                double previousAmount = pickupAmountCache.computeIfAbsent(uuid, _ -> 0D);
                pickupAmountCache.put(uuid, amount + previousAmount);
            }
            else {
                pickupAmountCache.put(uuid, amount);
            }

            displayAmount = pickupAmountCache.computeIfAbsent(uuid, _ -> 0D);
            pickupTimeCache.put(uuid, System.currentTimeMillis());
        }

        Component component = currency.getDepositMessage(displayAmount);
        coins.sendMessage(player, currency.getDepositPosition(), component);
    }

    // clear cache of showing deposits
    @EventHandler
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        pickupAmountCache.remove(uuid);
        pickupTimeCache.remove(uuid);
    }
}
