package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.drops.DefinedCoinDrop;
import community.coins.plugin.drops.DefinedDrop;
import community.coins.plugin.type.filter.EventFilterConfig;
import community.coins.plugin.type.registrar.EventType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 30, 2026
 */
public final class DropsConfig implements FileConfig<DefinedDrop> {
    private final CoinsCore coins;
    private final ConfigService service;

    public DropsConfig(CoinsCore coins, ConfigService service) {
        this.coins = coins;
        this.service = service;
    }

    @Override
    public String getFileName() {
        return "drops.yml";
    }

    private final Map<String, DefinedDrop> definedDrops = new HashMap<>();

    @Override
    public Optional<DefinedDrop> getDefinedItem(@NotNull String key) {
        return Optional.ofNullable(definedDrops.get(key.toLowerCase()));
    }

    @Override
    public Collection<DefinedDrop> getDefinedItems() {
        return definedDrops.values();
    }

    @Override
    public void parseAndReload() {
        var config = service.getOrCreateConfig(getFileName());

        var section = config.getConfigurationSection("drops");
        if (section == null) {
            service.printConfigWarning(getFileName(), "There are no defined drops in the config, `drops` section missing.");
            return;
        }

        // first we clear all registered drops, because we re-register them down here
        coins.getEventTypeService().clearRegisteredDrops();

        Map<String, DefinedDrop> configured = new HashMap<>();
        for (String dropName : section.getKeys(false)) {
            ConfigurationSection drop = section.getConfigurationSection(dropName);
            if (drop == null) {
                coins.debug("Skipping drops config entry for '%s', as nothing is configured.".formatted(dropName));
                continue; // almost impossible i believe
            }

            String definedEvent = drop.getString("event"); // predefined event in the plugin
            if (definedEvent == null) {
                service.printConfigWarning(getFileName(), "No event type found for drop '%s'.".formatted(dropName));
                continue;
            }

            boolean disabled = drop.contains("enabled") && !drop.getBoolean("enabled");
            if (disabled) {
                coins.debug("Skipping drops config entry for '%s', as it is disabled.".formatted(dropName));
                continue; // drop is not enabled
            }

            Optional<EventType> eventType = coins.getEventTypeService().getEventType(definedEvent.toLowerCase());
            if (eventType.isEmpty()) {
                service.printConfigWarning(getFileName(), """
                    Invalid event type '%s' found for drop '%s' at `event`. Supported types are: %s"""
                    .formatted(definedEvent, dropName, coins.getEventTypeService().getEventTypeNames())
                );
                continue;
            }

            EventType event = eventType.get();

            // get a filter config from the event type's filter contract
            ConfigurationSection filtersSection = drop.getConfigurationSection("filters"); // can be null!
            EventFilterConfig filterConfig = event.getFilterContract().getFilterConfig(
                filtersSection, definedEvent.toLowerCase()
            );

            // create a DefinedCoinDrop from the "coins" section
            ConfigurationSection coinsSection = drop.getConfigurationSection("coins");
            if (coinsSection == null) {
                continue; // todo warning
            }
            DefinedCoinDrop definedCoinDrop = new DefinedCoinDrop(coins, service, coinsSection);

            // now we have a DefinedDrop with EventType, EventFilterConfig and DefinedCoinDrop
            DefinedDrop definedDrop = new DefinedDrop(dropName, filterConfig, definedCoinDrop);

            // register the DefinedDrop to EventType
            event.registerDrop(definedDrop);

            configured.put(dropName, definedDrop);
            coins.debug("Registered drop '%s' for event type '%s'.".formatted(dropName, definedEvent));
        }

        definedDrops.clear();
        definedDrops.putAll(configured);

        coins.log(Level.INFO, "Loaded %,d defined drop(s) from '%s'.".formatted(definedDrops.size(), getFileName()));
    }
}
