package community.coins.plugin.economy;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.event.PlayerPickupCoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class CoinDepositHandler implements Listener {
    private final CoinsCore coins;
    public CoinDepositHandler(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerPickupCoinEvent(PlayerPickupCoinEvent event) {
        var player = event.getPlayer();
        if (coins.getEconomyService().depositCoin(player, event.getItem().getItemStack())) {
            return; // successfully deposited
        }

        // cannot pick up a coin that has no value
        event.setCancelled(true);
    }

    // todo keep in mind to change this when physical coins become a thing
    // immediately deposit money when a dropped coin is clicked in an inventory
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!coins.getCoinService().getCoinMeta().isCoin(event.getCurrentItem())) {
            return; // do nothing because it's not a coin
        }

        if (coins.getEconomyService().depositCoin(player, event.getCurrentItem())) {
            // successfully deposited
            event.setCancelled(true);
            event.getCurrentItem().setAmount(0);
        }
    }
}
