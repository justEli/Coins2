package community.coins.plugin.data;

import community.coins.plugin.CoinsCore;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * @author Eli
 * @since April 28, 2026
 */
@NullMarked
public final class PersistentData {
    private final NamespacedKey transformedKey; // stores the type of transformed entity
    private final NamespacedKey playerDamageKey; // total damage done by a player

    public PersistentData(CoinsCore coins) {
        this.transformedKey = new NamespacedKey(coins, "entity_transformed");
        this.playerDamageKey = new NamespacedKey(coins, "player_damage");
    }

    // entities
    // todo currently this can only be 1 transform type at a time, because setting a new overrides the old

    public void setTransformType(Entity entity, TransformType type) {
        entity.getPersistentDataContainer().set(transformedKey, PersistentDataType.INTEGER, type.getId());
    }

    public Optional<TransformType> getTransformType(Entity entity) {
        Integer type = entity.getPersistentDataContainer().get(transformedKey, PersistentDataType.INTEGER);
        if (type == null) {
            return Optional.empty();
        }

        return TransformType.fromId(type);
    }

    public boolean isTransformType(Entity entity, TransformType transformType) {
        Integer type = entity.getPersistentDataContainer().get(transformedKey, PersistentDataType.INTEGER);
        return type != null && transformType.getId() == type;
    }

    public double getPlayerDamage(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(playerDamageKey, PersistentDataType.DOUBLE, 0D);
    }

    public void addPlayerDamage(Entity entity, double amount) {
        entity.getPersistentDataContainer().set(playerDamageKey, PersistentDataType.DOUBLE, getPlayerDamage(entity) + amount);
    }
}
