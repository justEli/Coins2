package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.event.AdvancementDisplayEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * advancement implementation as it is different for Spigot and Paper
 * @author Eli
 * @since April 29, 2026
 */
public final class AdvancementDoneType extends EventType {
    public AdvancementDoneType(CoinsCore coins, EventTypeService service) {
        super(coins, service, "advancement_done", filter -> filter
            .hasInitiatorPlayer()
            .hasTargetType()
            .hasLocationWorld()
        );
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#advancement_done

    @EventHandler
    void onPlayerAdvancementDoneEvent(AdvancementDisplayEvent event) {
        Player player = event.getPlayer();
        var filter = createFilter()
            .withInitiatorEntity(player)
            .withTargetType(event.getAdvancement())
            .withLocationWorld(player.getLocation().getWorld());

        callEvent(filter, player.getLocation());
    }
}
