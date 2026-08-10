package community.coins.plugin.type;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.registrar.AdvancementDoneType;
import community.coins.plugin.type.registrar.BlockBreakType;
import community.coins.plugin.type.registrar.BlockPlaceType;
import community.coins.plugin.type.registrar.CropHarvestType;
import community.coins.plugin.type.registrar.EntityBreedType;
import community.coins.plugin.type.registrar.EntityCatchType;
import community.coins.plugin.type.registrar.EntityDeathType;
import community.coins.plugin.type.registrar.EntityTameType;
import community.coins.plugin.type.registrar.EventType;
import community.coins.plugin.type.registrar.ItemEnchantType;
import community.coins.plugin.type.registrar.ItemRepairType;
import community.coins.plugin.type.registrar.LootChestOpenType;
import community.coins.plugin.type.registrar.PotionBrewType;
import community.coins.plugin.type.registrar.RecipeUnlockType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eli
 * @since May 01, 2026
 */
public final class EventTypeService {
    private final CoinsCore coins;
    public EventTypeService(CoinsCore coins) {
        this.coins = coins;

        new AdvancementDoneType(coins, this);
        new BlockBreakType(coins, this);
        new BlockPlaceType(coins, this);
        new CropHarvestType(coins, this);
        new EntityBreedType(coins, this);
        new EntityCatchType(coins, this);
        new EntityDeathType(coins, this);
        new EntityTameType(coins, this);
        new ItemEnchantType(coins, this);
        new ItemRepairType(coins, this);
        new LootChestOpenType(coins, this);
        new PotionBrewType(coins, this);
        new RecipeUnlockType(coins, this);
    }

    // get the event type for example to register a drop to it
    private final Map<String, EventType> eventTypes = new ConcurrentHashMap<>();

    public void registerEventType(EventType type) {
        coins.parseEventHandlers(type);
        eventTypes.put(type.getIdentifier(), type); // identifier already lowercase
    }

    public void clearRegisteredDrops() {
        eventTypes.forEach((_, type) -> type.clearRegisteredDrops());
    }

    public Optional<EventType> getEventType(@NotNull String identifier) {
        return Optional.ofNullable(eventTypes.get(identifier.toLowerCase()));
    }

    public Set<String> getEventTypeNames() {
        return eventTypes.keySet();
    }
}
