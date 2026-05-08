package community.coins.plugin.spigot.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.event.PlayerPickupEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.logging.Level;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class PickupItemRegistrar implements Listener {
    private final CoinsCore coins;
    public PickupItemRegistrar(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
        coins.log(Level.WARNING, "Use Paper server software to let players with a full inventory pick up coins.");
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PlayerPickupEvent registerEvent = new PlayerPickupEvent(player, event.getItem());
        coins.getServer().getPluginManager().callEvent(registerEvent);

        if (registerEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }
}
