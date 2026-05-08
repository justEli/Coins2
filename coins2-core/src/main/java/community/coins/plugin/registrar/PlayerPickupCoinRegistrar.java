package community.coins.plugin.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.event.PlayerPickupCoinEvent;
import community.coins.plugin.event.PlayerPickupEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class PlayerPickupCoinRegistrar implements Listener {
    private final CoinsCore coins;
    public PlayerPickupCoinRegistrar(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    private static final Vector THROW_VECTOR = new Vector(0, .45, 0);
    private static final UUID NO_OWNER_UUID = UUID.fromString("00000001-0001-0001-0001-0000000000AD");

    @EventHandler(ignoreCancelled = true)
    void onPlayerPickupEvent(PlayerPickupEvent event) {
        var item = event.getItem();
        if (!coins.getCoinMeta().isCoin(item.getItemStack())) {
            return; // do nothing because it's not a coin
        }

        // the event is always canceled for coins
        event.setCancelled(true);

        // don't let players pick up coins that are already being handled or picked up
        if (item.getPickupDelay() > 0) {
            return;
        }

        // prevent pickup while PlayerPickupCoinEvent is handled
        UUID previousOwner = item.getOwner();
        item.setPickupDelay(1200);
        item.setOwner(NO_OWNER_UUID);

        PlayerPickupCoinEvent registerEvent = new PlayerPickupCoinEvent(event.getPlayer(), event.getItem());
        coins.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled()) {
            // allow to pick up again as PlayerPickupCoinEvent was canceled
            item.setPickupDelay(0);
            item.setOwner(previousOwner);
            return;
        }

        // throw the coin upwards
        item.setVelocity(THROW_VECTOR);
        coins.getScheduler().runEntityTaskLater(item, 6, item::remove);
    }
}
