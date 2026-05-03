package community.coins.plugin.type;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.api.EventType;
import community.coins.plugin.type.registrar.AdvancementDoneType;
import community.coins.plugin.type.registrar.BlockBreakType;
import community.coins.plugin.type.registrar.CropHarvestType;
import community.coins.plugin.type.registrar.EntityBreedType;
import community.coins.plugin.type.registrar.EntityCatchType;
import community.coins.plugin.type.registrar.EntityDeathType;
import community.coins.plugin.type.registrar.EntityTameType;
import community.coins.plugin.type.registrar.ItemEnchantType;
import community.coins.plugin.type.registrar.ItemRepairType;
import community.coins.plugin.type.registrar.LootChestOpenType;
import community.coins.plugin.type.registrar.PotionBrewType;
import community.coins.plugin.type.registrar.RecipeUnlockType;
import community.coins.plugin.type.filter.EventFilterBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private final Map<String, EventType> eventTypes = new HashMap<>();

    // only for EventType
    public void registerEventType(String identifier, EventType registrar) {
        coins.parseEventHandlers(registrar);
        eventTypes.put(identifier, registrar);
    }

    public Optional<EventType> getEventType(String key) {
        return Optional.ofNullable(eventTypes.get(key));
    }

    public EventFilterBuilder filterBuilder() {
        return new EventFilterBuilder(coins);
    }
}
