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

    // event: 'entity_breed'
    // filters:
    //   initiator:
    //     type: ['player']
    //     enabled: Boolean
    //     permission: String
    //   target:
    //     enabled: true
    //     type: List<String>     (entity types)
    //     category: List<String>   (passive, hostile, tameable, from_spawner, from_split, etc.)
    //   location:
    //     disabled-worlds: List<String>
    //     cooldown:
    //       cap-amount: Boolean
    //       duration: TimeString
    // coins: ...

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
