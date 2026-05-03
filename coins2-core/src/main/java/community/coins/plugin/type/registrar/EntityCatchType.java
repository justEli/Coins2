package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class EntityCatchType extends EventType {
    public EntityCatchType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasTargetEntity()
            .hasTargetMinXpDrop()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "entity_catch", filter);
    }

    // event: 'entity_catch'
    // filters:
    //   initiator:
    //     enabled: Boolean
    //     permission: String
    //   target:
    //     enabled: true
    //     type: List<String>   (entity type, i.e. cod, clown_fish, etc.)
    //     category: List<String>   (passive, hostile, tameable, from_spawner, from_split, etc.)
    //     min-xp-drop: Integer
    //   location:
    //     disabled-worlds: List<String>
    //     cooldown:
    //       cap-amount: Boolean
    //       duration: TimeString
    // coins: ...

    @EventHandler(ignoreCancelled = true)
    void onPlayerFishEvent(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item item)) {
            return;
        }

        var filter = createForm()
            .withInitiatorEntity(event.getPlayer())
            .withTargetEntity(event.getCaught())
            .withTargetXpDrop(event.getExpToDrop())
            .withLocationWorld(item.getWorld())
            .withLocationCooldown(item.getLocation())
            .build();

        filterEvent(filter).thenDrop(item.getLocation()); // todo test, otherwise player.getLocation()
    }
}
