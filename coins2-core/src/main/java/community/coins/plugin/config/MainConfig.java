package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
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
public final class MainConfig extends BasicConfig {
    // todo make web interface with checkboxes that compile to config entries, especially useful for things like
    //  filters (player alts, projectile kill, stab kill, etc.)
    public MainConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "config.yml");
    }

    private static final Class<?> TYPE = ConfigYml.class;

    @Override
    public void parseAndReload() {
        YamlConfiguration config = getOrCreateConfig();

        for (Field field : TYPE.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigEntry.class) || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            String configKey = configEntry.value();
            field.setAccessible(true);

            try {
                if (configKey == null || !config.contains(configKey)) {
                    if (configEntry.required()) {
                        String value = field.get(TYPE).toString();
                        addWarn("Config is missing `%s`. Using its default value '%s' now.".formatted(configKey, value));
                    }
                    continue;
                }

                // get current/default value, for the type
                var value = field.get(TYPE);

                // handling different types
                var updatedValue = switch (value) {
                    case String _ -> config.getString(configKey);
                    case List<?> _ -> config.getStringList(configKey);
                    case Set<?> _ -> getStringSet(config, configKey);
                    case Component _ -> ComponentUtil.parse(config.getString(configKey));
                    case ItemStack _ -> coins.getItemParseApi().getFromItemType(config.getString(configKey)).orElse(null);
                    case Material _ -> Util.getType(config.getString(configKey), Registry.MATERIAL).orElse(null);
                    case TextColor _ -> getTextColor(config, configKey);
                    case Long _ -> config.getLong(configKey);
                    case Integer _ -> config.getInt(configKey);
                    case Double _ -> config.getDouble(configKey);
                    default -> config.get(configKey);
                };

                if (updatedValue == null) {
                    addWarn("Invalid value for `%s`, using default.".formatted(configKey));
                    return;
                }

                // update the field of the config class to the config's value
                field.set(TYPE, updatedValue);
            }
            catch (Throwable throwable) {
                addWarn("Invalid value for `%s`, using default.".formatted(configKey));
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
        return Util.getEnum(type, config.getString(key));
    }
}
