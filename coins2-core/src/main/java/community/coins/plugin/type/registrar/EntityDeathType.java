package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import community.coins.plugin.util.EntityUtil;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class EntityDeathType extends EventType {
    public EntityDeathType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasInitiatorEntity()
            .hasInitiatorAny()
            .hasTargetEntity()
            .hasTargetMinXpDrop()
            .hasTargetPreventAlts()
            .hasTargetMinPlayerDamage()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "entity_death", filter);
    }

    // event: 'entity_death'
    // filters:
    //   initiator:
    //     enabled: Boolean
    //     type: ['player', 'skeleton', 'any']
    //     action: ['projectile', 'stabbing', 'any']
    //     permission: String
    //   target:
    //     type: List<String>  (entity types)
    //     category: List<String>   (passive, hostile, tameable, from_spawner, from_split, etc.)
    //     min-xp-drop: Integer
    //     prevent-alts: Boolean  (if player)
    //     min-player-damage: Double  (percentage)
    //   location:
    //     disabled-worlds: List<String>
    //     cooldown:
    //       cap-amount: Boolean
    //       duration: TimeString
    // coins: ...

    @EventHandler(ignoreCancelled = true)
    void onEntityDeathEvent(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead instanceof ArmorStand) {
            return;
        }

        Entity root = EntityUtil.getRootAttacker(dead).orElse(null);

        var filter = createForm()
            .withInitiatorEntity(root)
            .withTargetEntity(dead)
            .withTargetXpDrop(event.getDroppedExp())
            .withTargetPreventAlts(root, dead)
            .withLocationWorld(dead.getWorld())
            .withLocationCooldown(dead.getLocation())
            .build();

        filterEvent(filter).thenDrop(event.getEntity().getLocation());
    }
}
