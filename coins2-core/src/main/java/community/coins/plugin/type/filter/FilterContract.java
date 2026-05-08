package community.coins.plugin.type.filter;

import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.type.registrar.EventType;
import community.coins.plugin.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * this contract has all paths that are allowed in the config for defining a drop
 * @author Eli
 * @since May 02, 2026
 */
public final class FilterContract {
    private final EventType eventType;
    private final Set<String> configPaths;

    public FilterContract(EventType eventType, Set<String> configPaths) {
        this.eventType = eventType;
        this.configPaths = configPaths;
    }

    public EventType getEventType() {
        return eventType;
    }

    // todo clean up messy class

    /// get a FilterConfig based on the contract (of the event)
    public @NotNull EventFilterConfig createFilterConfig(ConfigurationSection conf, ConfigurationSection def, String dropId, ConfigWarns.Named warns) {
        if (conf == null) {
            conf = def;
        }
        if (conf == null) {
            return new EventFilterConfig(); // no filters
        }

        var filter = new EventFilterConfig();
        if (contains("initiator.permission", conf, def, warns, dropId)) {
            filter.setInitiatorPermission(conf.getString("initiator.permission", def.getString("initiator.permission")));
        }
        if (contains("initiator.type", conf, def, warns, dropId)) {
            List<String> values = conf.getStringList("initiator.type");
            if (values.isEmpty()) {
                values.addAll(def.getStringList("initiator.type"));
            }
            filter.setInitiatorType(toNamespacedKeys(values, warns, dropId));
        }
        if (contains("initiator.any", conf, def, warns, dropId)) {
            filter.setInitiatorAny(conf.getBoolean("initiator.any", def.getBoolean("initiator.any")));
        }
        if (contains("target.type", conf, def, warns, dropId)) {
            List<String> values = conf.getStringList("target.type");
            if (values.isEmpty()) {
                values.addAll(def.getStringList("target.type"));
            }
            filter.setTargetType(toNamespacedKeys(values, warns, dropId));
        }
        if (contains("target.category", conf, def, warns, dropId)) {
            List<String> values = conf.getStringList("target.category");
            if (values.isEmpty()) {
                values.addAll(def.getStringList("target.category"));
            }
            filter.setTargetCategory(new HashSet<>(values));
        }
        if (contains("target.min-xp-drop", conf, def, warns, dropId)) {
            filter.setTargetMinXpDrop(conf.getInt("target.min-xp-drop", def.getInt("target.min-xp-drop")));
        }
        if (contains("target.allow-same-block", conf, def, warns, dropId)) {
            filter.setTargetAllowSameBlock(conf.getBoolean("target.allow-same-block", def.getBoolean("target.allow-same-block")));
        }
        if (contains("target.prevent-alts", conf, def, warns, dropId)) {
            filter.setTargetPreventAlts(conf.getBoolean("target.prevent-alts", def.getBoolean("target.prevent-alts")));
        }
        if (contains("target.min-player-damage", conf, def, warns, dropId)) {
            filter.setTargetMinPlayerDamage(conf.getDouble("target.min-player-damage", def.getDouble("target.min-player-damage")));
        }
        if (contains("location.disabled-worlds", conf, def, warns, dropId)) {
            List<String> values = conf.getStringList("location.disabled-worlds");
            if (values.isEmpty()) {
                values.addAll(def.getStringList("location.disabled-worlds"));
            }
            filter.setLocationDisabledWorlds(new HashSet<>(values));
        }
        if (contains("location.cooldown.cap-amount", conf, def, warns, dropId) && contains("location.cooldown.duration", conf, def, warns, dropId)) {
            OptionalInt durationMillis = Util.toDurationMillis(conf.getString("location.cooldown.duration", def.getString("location.cooldown.duration")));
            if (durationMillis.isPresent()) {
                filter.setLocationCooldownCapAmount(conf.getInt("location.cooldown.cap-amount", def.getInt("location.cooldown.cap-amount")));
                filter.setLocationCooldownDurationMillis(durationMillis.getAsInt());
            }
            else {
                warns.warn("Cannot set location cooldown for drop '%s' because cooldown duration is invalid.".formatted(dropId));
            }
        }

        return filter;
    }

    private boolean contains(String path, ConfigurationSection conf, ConfigurationSection def, ConfigWarns.Named warns, String dropId) {
        if (!configPaths.contains(path)) { // if config path is not supported
            if (conf.contains(path)) {
                warns.warn("Found drop filter '%s' for drop '%s', but event type does not support it.".formatted(path, dropId));
            }
            return false;
        }

        return conf.contains(path) || def.contains(path);
    }

    private Set<NamespacedKey> toNamespacedKeys(List<String> values, ConfigWarns.Named warns, String dropId) {
        Set<NamespacedKey> keys = new HashSet<>();
        for (String value : values) {
            var name = NamespacedKey.fromString(value);
            if (name == null) {
                warns.warn("Cannot add type '%s' to drop filter of drop '%s' because type is invalid.".formatted(value, dropId));
                continue;
            }
            keys.add(name);
        }
        return keys;
    }
}
