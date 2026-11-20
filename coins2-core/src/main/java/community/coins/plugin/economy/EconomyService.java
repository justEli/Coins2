package community.coins.plugin.economy;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.economy.hook.VaultEconomyHook;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

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

        // add economy hooks that this plugin supports
        addSupportedEconomy(new VaultEconomyHook(coins, this));
    }

    // String = plugin's name (case-sensitive)
    private final Map<String, EconomyHook> supportedEconomies = new HashMap<>();

    private void addSupportedEconomy(EconomyHook hook) {
        supportedEconomies.put(hook.getPluginName(), hook);
    }

    public Optional<EconomyHook> getSupportedEconomy(String pluginName) {
        return Optional.ofNullable(supportedEconomies.get(pluginName));
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
                .formatted(notFoundCurrency, coins.getComponentApi().getKey(coin.getType()))
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
