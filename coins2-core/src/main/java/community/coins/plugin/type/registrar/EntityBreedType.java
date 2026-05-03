package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class EntityBreedType extends EventType {
    public EntityBreedType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasTargetEntity()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "entity_breed", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#entity_breed

    @EventHandler(ignoreCancelled = true)
    void onEntityBreedEvent(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }

        var baby = event.getEntity();
        var filter = createForm()
            .withInitiatorEntity(player)
            .withTargetEntity(baby)
            .withLocationWorld(baby.getWorld())
            .withLocationCooldown(baby.getLocation())
            .build();

        filterEvent(filter).thenDrop(baby.getLocation());
    }
}
