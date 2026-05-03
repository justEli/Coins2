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

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#entity_death

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
