package community.coins.plugin.handler;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.data.TransformType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

import java.util.Optional;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class MobTransformHandler implements Listener {
    private final CoinsCore coins;
    public MobTransformHandler(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    @EventHandler
    void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER || event.getEntityType() == EntityType.CAVE_SPIDER) {
            coins.getPersistentData().setTransformType(event.getEntity(), TransformType.FROM_SPAWNER);
        }
        else if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            coins.getPersistentData().setTransformType(event.getEntity(), TransformType.FROM_SPLIT);
        }
        else if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
            coins.getPersistentData().setTransformType(event.getEntity(), TransformType.FROM_BREEDING);
        }
        else if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.LIGHTNING) {
            coins.getPersistentData().setTransformType(event.getEntity(), TransformType.FROM_LIGHTNING);
        }
    }

    @EventHandler
    void onEntityTransformEvent(EntityTransformEvent event) {
        Optional<TransformType> type = coins.getPersistentData().getTransformType(event.getEntity());
        boolean fromSpawner = type.isPresent() && type.get() == TransformType.FROM_SPAWNER;

        for (Entity entity : event.getTransformedEntities()) {
            coins.getPersistentData().setTransformType(entity, TransformType.TO_DIFFERENT_TYPE);
            if (fromSpawner) {
                // when a Zombie converts to Drowned, the Drowned is technically still from a spawner
                coins.getPersistentData().setTransformType(entity, TransformType.FROM_SPAWNER);
            }
        }
    }
}
