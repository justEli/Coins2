package community.coins.plugin.config;

import community.coins.plugin.api.BasicPlugin;
import community.coins.plugin.component.ComponentUtil;
import community.coins.plugin.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class ConfigParser {
    private final BasicPlugin plugin;
    private final ConfigService service;

    // todo make web interface with checkboxes that compile to config entries, especially useful for things like
    //  filters (player alts, projectile kill, stab kill, etc.)
    public ConfigParser(BasicPlugin plugin, ConfigService service) {
        this.plugin = plugin;
        this.service = service;
    }

    private <T> String getName(Class<T> type) {
        return type.getAnnotation(ConfigFile.class).value();
    }

    public <T> void parseAndInject(Class<T> type) {
        String fileName = getName(type);
        YamlConfiguration config = service.getOrCreateConfig(fileName);

        for (Field field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigEntry.class) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            String configKey = configEntry.value();
            field.setAccessible(true);

            if (configKey == null || !config.contains(configKey) && configEntry.required()) {
                service.addWarning("Config '%s' is missing `%s`, using default.".formatted(fileName, configKey));
                continue;
            }

            try {
                // get current/default value, for the type
                var value = field.get(type);

                // handling different types
                var updatedValue = switch (value) {
                    case String _ -> config.getString(configKey);
                    case List<?> _ -> config.getStringList(configKey);
                    case Set<?> _ -> getStringSet(config, configKey);
                    case Component _ -> ComponentUtil.parse(config.getString(configKey));
                    case ItemStack _ -> plugin.getItemParseApi().getFromItemType(config.getString(configKey)).orElse(null);
                    case Material _ -> Util.getType(config.getString(configKey), Registry.MATERIAL).orElse(null);
                    case EconomyType _ -> getEnum(EconomyType.class, config, configKey);
                    case TextColor _ -> getTextColor(config, configKey);
                    case Long _ -> config.getLong(configKey);
                    case Integer _ -> config.getInt(configKey);
                    case Double _ -> config.getDouble(configKey);
                    default -> config.get(configKey);
                };

                if (updatedValue == null) {
                    service.addWarning("Config '%s' has invalid value for `%s`, using default.".formatted(fileName, configKey));
                    return;
                }

                // update the field of the config class to the config's value
                field.set(type, updatedValue);
            }
            catch (Throwable throwable) {
                service.addWarning("Config '%s' has invalid value for `%s`, using default.".formatted(fileName, configKey));
            }
        }
    }

    public static TextColor getTextColor(FileConfiguration config, String key) {
        var color = config.getString(key);
        if (color == null) {
            return null;
        }
        return TextColor.fromHexString(color);
    }

    public static Set<String> getStringSet(YamlConfiguration config, String key) {
        return new HashSet<>(config.getStringList(key));
    }

    @NullMarked
    public static <T extends Enum<T>> @Nullable T getEnum(Class<T> type, YamlConfiguration config, String key) {
        String value = config.getString(key);
        if (value == null) {
            return null;
        }

        try { return Enum.valueOf(type, value.toUpperCase().replace(" ", "_")); }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
