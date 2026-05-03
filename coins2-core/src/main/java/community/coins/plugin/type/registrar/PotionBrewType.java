package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.BrewEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class PotionBrewType extends EventType {
    public PotionBrewType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasLocationWorld()
            .hasLocationCooldown();

        super(coins, service, "potion_brew", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#potion_brew

    // todo never tested before
    @EventHandler(ignoreCancelled = true)
    void onBrewEvent(BrewEvent event) {
        // todo get player from who brewed it

        var block = event.getBlock();
        var filter = createForm()
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(event.getBlock());
    }
}
