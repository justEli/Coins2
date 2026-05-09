package community.coins.plugin.economy;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.economy.hook.VaultEconomyHook;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Eli
 * @since May 05, 2026
 */
@NullMarked
public final class EconomyService implements Listener {
    private final CoinsCore coins;
    public EconomyService(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);

        // economy: 'Vault'
        hookIfInstalled(
            VaultEconomyHook.NAME,
            () -> Optional.ofNullable(coins.getServer().getServicesManager().getRegistration(Economy.class))
                .map(registration -> new VaultEconomyHook(coins, this, registration.getProvider()))
        );

        // todo add a 'physical' economy/currency

        // add more economy hooks here
    }

    // registering economies/plugins

    // String = plugin's name (case-sensitive)
    private final Map<String, EconomyHook> economyHooks = new HashMap<>();

    // called from EconomyHook to register itself
    public void registerEconomy(EconomyHook economy) {
        economyHooks.put(economy.getName(), economy);
    }

    /// @param pluginName case-sensitive plugin name of the economy
    public Optional<EconomyHook> getEconomy(String pluginName) {
        return Optional.ofNullable(economyHooks.get(pluginName));
    }

    /// @param pluginName case-sensitive plugin name
    private void hookIfInstalled(String pluginName, Supplier<Optional<EconomyHook>> hook) {
        if (!coins.getServer().getPluginManager().isPluginEnabled(pluginName)) {
            return;
        }

        EconomyHook economy;
        try { economy = hook.get().orElse(null); }
        catch (NullPointerException | NoClassDefFoundError ignored) {
            economy = null;
        }

        if (economy == null) {
            coins.log(Level.SEVERE, "Found '%s', but it is missing an economy providing plugin.".formatted(pluginName));
            return;
        }

        coins.log(Level.INFO, "Hooked into '%s' as an economy provider.".formatted(pluginName));
    }

    // registering currencies

    public void clearRegisteredCurrencies() {
        currencyToEconomyNames.clear();
        economyHooks.values().forEach(EconomyHook::clearCurrencies);
    }

    // <currency identifier, economy plugin name>
    private final Map<String, String> currencyToEconomyNames = new HashMap<>();

    public Collection<String> getCurrencyIdentifiers() {
        return currencyToEconomyNames.keySet();
    }

    public boolean registerCurrency(DefinedCurrency currency, ConfigWarns.Named warns) {
        EconomyHook economy = currency.getEconomyHook();
        if (!economy.isMultiCurrency() && economy.getAmountOfCurrencies() > 0) {
            warns.warn("""
                Cannot register currency '%s' for plugin '%s' that only supports one currency."""
                .formatted(currency.getIdentifier(), currency.getEconomyHook().getName())
            );
            return false;
        }

        economy.addCurrency(currency);
        currencyToEconomyNames.put(currency.getIdentifier(), economy.getName());
        return true;
    }

    public Optional<DefinedCurrency> getCurrency(String currency) {
        String economyName = currencyToEconomyNames.get(currency);
        if (economyName == null) {
            return Optional.empty();
        }

        return getEconomy(economyName).flatMap(economy -> economy.getCurrency(currency));
    }

    public void submitTransaction(DefinedCurrency currency, Consumer<EconomyAction> action) {
        action.accept(currency.getEconomyHook());
    }

    /// deposit the coin into the right currency and value, including deposit message and pickup sound
    /// @return true if successful deposit of coin
    public boolean depositCoin(Player player, ItemStack coin) {
        Optional<DefinedCurrency> currency = coins.getCoinMeta().getCoinDefinedCurrency(coin);
        if (currency.isEmpty()) {
            coins.getLogger().warning("""
                Attached currency to coin with item type '%s' was not found. Consider to add it to 'currencies.yml' again."""
                .formatted(coin.getType().getKey())
            );
            return false;
        }

        OptionalDouble value = coins.getCoinMeta().getCoinValue(coin);
        if (value.isEmpty()) {
            return false;
        }

        coins.getEconomyService().submitTransaction(currency.get(), transaction -> {
            if (transaction.deposit(player.getUniqueId(), value.getAsDouble())) {
                sendDepositMessage(currency.get(), player, value.getAsDouble());
                coins.getCoinMeta().playSound(player, coin);
            }
        });
        return true;
    }

    // coin pickup messages

    private final Map<UUID, Double> pickupAmountCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pickupTimeCache = new ConcurrentHashMap<>();

    private static final long ACCUMULATE_MILLIS = 1500;

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
