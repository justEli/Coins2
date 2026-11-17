package community.coins.plugin.spigot.implement;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.PluginAttributes;
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
        return coins.getDescription().getVersion();
    }

    @Override
    public String getName() {
        return coins.getDescription().getName();
    }

    @Override
    public String getUrl() {
        var website = coins.getDescription().getWebsite();
        return website == null? "" : website;
    }

    @Override
    public String getDescription() {
        var description = coins.getDescription().getDescription();
        return description == null? "" : description;
    }

    @Override
    public @Nullable World getWorld(String identifier) {
        var world = coins.getServer().getWorld(identifier);
        if (world != null) {
            return world;
        }

        try {
            return coins.getServer().getWorld(UUID.fromString(identifier));
        }
        catch (IllegalArgumentException _) {
            return null;
        }
    }
}
