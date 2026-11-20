package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.util.ColorResolver;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
        var config = getOrCreateConfig();

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
                        warn("Config is missing `%s`, now using its default value '%s'.".formatted(configKey, value));
                    }
                    continue;
                }

                // get current/default value, for the type
                var value = field.get(TYPE);

                // handling different types
                var updatedValue = switch (value) {
                    case String _ -> config.getString(configKey);
                    case Long _ -> config.getLong(configKey);
                    case Integer _ -> config.getInt(configKey);
                    case Double _ -> config.getDouble(configKey);
                    default -> config.get(configKey);
                };

                if (updatedValue == null) {
                    warn("Config has invalid value for `%s`, now using its default value.".formatted(configKey));
                    return;
                }

                // update the field of the config class to the config's value
                field.set(TYPE, updatedValue);
            }
            catch (Throwable throwable) {
                warn("Config has invalid value for `%s`, now using its default value.".formatted(configKey));
            }
        }

        ConfigurationSection colors = config.getConfigurationSection("colors");
        if (colors == null) {
            return; // no colors section, that's ok
        }

        for (String tag : colors.getKeys(false)) {
            String hexString = colors.getString(tag);
            if (hexString == null) {
                continue;
            }

            TextColor color = TextColor.fromCSSHexString(hexString);
            if (color == null) {
                warn("Failed to parse color '%s' for color '%s', this is an invalid HEX format.".formatted(hexString, tag));
                continue;
            }

            ColorResolver.updateTag(tag, color);
        }
    }
}
