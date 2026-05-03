package community.coins.plugin.type.filter;

import community.coins.plugin.CoinsCore;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eli
 * @since May 02, 2026
 */
public final class EventFilter {
    private final CoinsCore coins;
    private final Set<String> configPaths;
    private final String eventIdentifier;

    public EventFilter(CoinsCore coins, Set<String> configPaths, String eventIdentifier) {
        this.coins = coins;
        this.configPaths = configPaths;
        this.eventIdentifier = eventIdentifier;
    }

    public EventFilterFormBuilder createForm() {
        return new EventFilterFormBuilder(coins, this);
    }

    private final AtomicReference<FilterConfig> filterConfig = new AtomicReference<>(null);

    public Optional<FilterConfig> getFilterConfig() {
        return Optional.ofNullable(filterConfig.get());
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void applyConfig(@Nullable ConfigurationSection config, String eventType) {
        if (config == null) {
            filterConfig.set(new FilterConfig()); // no filters
            return;
        }

        filterConfig.set(null);

        var filter = new FilterConfig();
        if (contains("initiator.permission", config)) {
            filter.initiatorPermission = config.getString("initiator.permission");
        }
        if (contains("initiator.type", config)) {
            List<String> values = config.getStringList("initiator.type");
            filter.initiatorType = toNamespacedKeys(values, eventType);
        }
        if (contains("initiator.any", config)) {
            filter.initiatorAny = config.getBoolean("initiator.any");
        }
        if (contains("target.type", config)) {
            List<String> values = config.getStringList("target.type");
            filter.targetType = toNamespacedKeys(values, eventType);
        }
        if (contains("target.category", config)) {
            List<String> values = config.getStringList("target.category");
            filter.targetCategory = new HashSet<>(values);
        }
        if (contains("target.min-xp-drop", config)) {
            filter.targetMinXpDrop = config.getInt("target.min-xp-drop");
        }
        if (contains("target.allow-same-block", config)) {
            filter.targetAllowSameBlock = config.getBoolean("target.allow-same-block");
        }
        if (contains("target.prevent-alts", config)) {
            filter.targetPreventAlts = config.getBoolean("target.prevent-alts");
        }
        if (contains("target.min-player-damage", config)) {
            filter.targetMinPlayerDamage = config.getDouble("target.min-player-damage");
        }
        if (contains("location.disabled-worlds", config)) {
            List<String> values = config.getStringList("location.disabled-worlds");
            filter.locationDisabledWorlds = new HashSet<>(values);
        }
        if (contains("location.cooldown.cap-amount", config) && contains(config.getString("location.cooldown.duration"), config)) {
            filter.locationCooldownCapAmount = config.getInt("location.cooldown.cap-amount");
            filter.locationCooldownDuration = config.getString("location.cooldown.duration");
        }

        filterConfig.set(filter);
    }

    private boolean contains(String path, ConfigurationSection section) {
        boolean inConfig = section.contains(path);
        if (inConfig && !configPaths.contains(path)) { // todo improve warning adding eventIdentifier
            coins.getConfigService().addWarning("Found '%s' in config where it is not supported.");
            return false;
        }
        return inConfig;
    }

    private Set<NamespacedKey> toNamespacedKeys(List<String> values, String eventType) {
        Set<NamespacedKey> keys = new HashSet<>();
        for (String value : values) {
            var name = NamespacedKey.fromString(value);
            if (name == null) { // todo improve warning adding eventIdentifier
                coins.getConfigService().addWarning("Invalid type found for event type '%s'.".formatted(eventType));
                continue;
            }
            keys.add(name);
        }
        return keys;
    }
}
