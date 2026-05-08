package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.drops.DefinedCoinDrop;
import community.coins.plugin.drops.DefinedDrop;
import community.coins.plugin.type.filter.EventFilterConfig;
import community.coins.plugin.type.registrar.EventType;
import community.coins.plugin.util.Util;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eli
 * @since April 30, 2026
 */
public final class DropsConfig extends FileConfig<DefinedDrop> {
    private static final ScheduledExecutorService SCHEDULED_THREAD =
        Executors.newSingleThreadScheduledExecutor();

    public DropsConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "drops.yml");

        // clean up unused cache
        SCHEDULED_THREAD.scheduleAtFixedRate(() -> {
            getDefinedItems().forEach(DefinedDrop::cleanUpLocationCache);
            coins.debug("Cleared all location cooldown caches");
        }, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();

        ConfigurationSection dropsSection = config.getConfigurationSection("drops");
        if (dropsSection == null) {
            addWarn("Cannot register drops because section for defining drops is missing.");
            return;
        }

        // first we clear all registered drops on the events, because we re-register them down here
        coins.getEventTypeService().clearRegisteredDrops();

        Map<String, DefinedDrop> configured = new HashMap<>();
        for (String name : dropsSection.getKeys(false)) {
            ConfigurationSection section = dropsSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            boolean disabled = section.contains("enabled") && !section.getBoolean("enabled");
            if (disabled) {
                coins.debug("Skipping drops config entry for '%s', as it is disabled.".formatted(name));
                continue; // drop is not enabled
            }

            String id = Util.toIdentifier(name);
            String definedEvent = section.getString("event"); // predefined event in the plugin
            if (definedEvent == null) {
                addWarn("Cannot register drop '%s' because event type was not provided.".formatted(id));
                continue;
            }

            Optional<EventType> eventType = coins.getEventTypeService().getEventType(definedEvent.toLowerCase());
            if (eventType.isEmpty()) {
                addWarn("""
                    Cannot register drop '%s' because '%s' is an unknown event type. Supported types are: %s"""
                    .formatted(definedEvent, name, coins.getEventTypeService().getEventTypeNames())
                );
                continue;
            }

            EventType event = eventType.get();

            // get a filter config from the event type's filter contract
            ConfigurationSection filtersSection = section.getConfigurationSection("filters"); // can be null!
            ConfigurationSection defaultFilters = config.getConfigurationSection("default.filters"); // can be null!
            EventFilterConfig filterConfig = event.getFilterContract().createFilterConfig(filtersSection, defaultFilters, id, configWarns);

            // create a DefinedCoinDrop from the "coins" section
            ConfigurationSection coinsSection = section.getConfigurationSection("coins");
            if (coinsSection == null) {
                continue;
            }

            DefinedCoinDrop definedCoinDrop = new DefinedCoinDrop(service, configWarns, coinsSection, id);

            // now we have a DefinedDrop with EventType, EventFilterConfig and DefinedCoinDrop
            DefinedDrop definedDrop = new DefinedDrop(id, filterConfig, definedCoinDrop);

            // register the DefinedDrop to EventType
            event.registerDrop(definedDrop);

            configured.put(id, definedDrop);
            coins.debug("Registered drop '%s' for event type '%s'.".formatted(id, definedEvent));
        }

        putDefinedItems(configured, "drop", "drops");
    }
}
