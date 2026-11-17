package community.coins.plugin.paper.implement;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.PluginAttributes;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public final class PluginAttributesImpl implements PluginAttributes {
    public final CoinsCore coins;
    public PluginAttributesImpl(CoinsCore coins) {
        this.coins = coins;
    }

    @Override
    public String getVersion() {
        return coins.getPluginMeta().getVersion();
    }

    @Override
    public String getName() {
        return coins.getPluginMeta().getName();
    }

    @Override
    public String getUrl() {
        var website = coins.getPluginMeta().getWebsite();
        return website == null? "" : website;
    }

    @Override
    public String getDescription() {
        var description = coins.getPluginMeta().getDescription();
        return description == null? "" : description;
    }

    @Override
    public @Nullable World getWorld(String identifier) {
        if (Key.parseable(identifier)) {
            // noinspection PatternValidation
            var world0 = coins.getServer().getWorld(Key.key(identifier));
            if (world0 != null) {
                return world0;
            }
        }

        var world1 = coins.getServer().getWorld(identifier);
        if (world1 != null) {
            return world1;
        }

        var key = NamespacedKey.fromString(identifier);
        if (key != null) {
            var world2 = coins.getServer().getWorld(key);
            if (world2 != null) {
                return world2;
            }
        }

        try {
            return coins.getServer().getWorld(UUID.fromString(identifier));
        }
        catch (IllegalArgumentException _) {
            return null;
        }
    }
}
