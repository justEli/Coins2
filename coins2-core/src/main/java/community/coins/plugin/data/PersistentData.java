package community.coins.plugin.data;

import community.coins.plugin.CoinsCore;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class PersistentData {
    private final NamespacedKey valueKey; // stores a coin's value
    private final NamespacedKey withdrawnKey; // stores the withdrawer's uuid of the coin
    private final NamespacedKey transformedKey; // stores the type of transformed entity

    public PersistentData(CoinsCore coins) {
        this.valueKey = NamespacedKey.fromString("coin_value", coins);
        this.withdrawnKey = NamespacedKey.fromString("coin_withdrawn", coins);
        this.transformedKey = NamespacedKey.fromString("entity_transformed", coins);
    }

    // coins

    public boolean isCoin(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        // a coin always has a value (todo check the entire config on if this is implemented everywhere)
        return item.getItemMeta().getPersistentDataContainer().has(valueKey, PersistentDataType.DOUBLE);
    }

    public void setCoinValue(ItemStack item, double amount) {
        if (item == null || item.getItemMeta() == null || amount <= 0) {
            return;
        }

        item.getItemMeta().getPersistentDataContainer().set(valueKey, PersistentDataType.DOUBLE, amount);
    }

    public OptionalDouble getCoinValue(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return OptionalDouble.empty();
        }

        Double value = item.getItemMeta().getPersistentDataContainer().get(valueKey, PersistentDataType.DOUBLE);
        return value == null? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    public Optional<UUID> getWithdrawOwner(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return Optional.empty();
        }

        String rawUuid = item.getItemMeta().getPersistentDataContainer().get(withdrawnKey, PersistentDataType.STRING);
        if (rawUuid == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(rawUuid));
        }
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public void setWithdrawOwner(ItemStack item, UUID uuid) {
        if (item == null || item.getItemMeta() == null) {
            return;
        }

        item.getItemMeta().getPersistentDataContainer().set(withdrawnKey, PersistentDataType.STRING, uuid.toString());
    }

    // entities

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
}
