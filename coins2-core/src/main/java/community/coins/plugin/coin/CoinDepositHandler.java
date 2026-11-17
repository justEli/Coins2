package community.coins.plugin.coin;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.event.PlayerPickupCoinEvent;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    // deposit a coin when it is picked up (except for withdrawn coins)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerPickupCoinEvent(PlayerPickupCoinEvent event) {
        var player = event.getPlayer();

        var item = event.getItem().getItemStack();
        if (coins.getCoinMeta().getWithdrawOwner(item).isPresent()) {
            // don't deposit coin when it's withdrawn
            event.setPickupDisallowed(false); // allow pickup of coin
            return;
        }

        if (coins.getEconomyService().depositCoin(player, item)) {
            return; // successfully deposited
        }

        // cannot pick up a coin that has no value
        event.setCancelled(true);
    }

    // immediately deposit when a dropped coin is clicked in an inventory (except for withdrawn coins)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!coins.getCoinMeta().isCoin(event.getCurrentItem())) {
            return; // do nothing because it's not a coin
        }

        if (coins.getCoinMeta().getWithdrawOwner(event.getCurrentItem()).isPresent()) {
            return; // don't deposit coin when it's withdrawn
        }

        if (coins.getEconomyService().depositCoin(player, event.getCurrentItem())) {
            // successfully deposited
            event.setCancelled(true);
            event.getCurrentItem().setAmount(0);
        }
    }

    // deposit (withdrawn) coins when a player clicks it
    @EventHandler
    void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!coins.getGlobalState().getDepositActions().contains(event.getAction())) {
            return; // performed action is not registered for depositing
        }

        if (coins.getCoinMeta().getWithdrawOwner(event.getItem()).isEmpty()) {
            return; // coin is not withdrawn
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Container) {
            return; // make an exception for containers
        }

        event.setCancelled(true);

        var item = event.getItem();
        if (item == null) {
            return;
        }

        if (coins.getEconomyService().depositCoin(event.getPlayer(), item)) {
            event.getItem().setAmount(0);
        }
    }
}
