package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.drops.DefinedDrop;
import community.coins.plugin.type.api.EventType;
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

        Map<String, DefinedDrop> configured = new HashMap<>();
        for (String dropName : section.getKeys(false)) {
            ConfigurationSection drop = section.getConfigurationSection(dropName);
            if (drop == null) {
                continue; // todo maybe a warning (also in CoinsConfig.java)
            }

            String definedEvent = drop.getString("event"); // predefined event in the plugin
            ConfigurationSection filters = drop.getConfigurationSection("filters");
            if (definedEvent == null || filters == null) {
                // todo it is kind of fine that it doesn't have a filter, just don't filter it
                service.printConfigWarning(getFileName(), "No event type or filter found for drop '%s'.".formatted(dropName));
                continue;
            }

            Optional<EventType> eventType = coins.getEventTypeService().getEventType(definedEvent.toLowerCase());
            if (eventType.isEmpty()) {
                service.printConfigWarning(getFileName(), "Invalid event type '%s' found for drop '%s' at `%s`.".formatted(
                    definedEvent, dropName, "event"
                ));
                continue;
            }

            // todo handle section 'filters' with DefinedDrop and EventType
            // todo implement
            eventType.get().getFilter().applyConfig(filters, definedEvent.toLowerCase());
            coins.debug("Registered drop '%s' for event type '%s'.".formatted(dropName, definedEvent));
        }

        definedDrops.clear();
        definedDrops.putAll(configured);

        coins.log(Level.INFO, "Loaded %,d defined drop(s) from '%s'.".formatted(definedDrops.size(), getFileName()));
    }
}
