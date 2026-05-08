package community.coins.plugin.spigot.implement;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.platform.PluginAttributes;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public final class PluginAttributesSpigot implements PluginAttributes {
    public final CoinsCore coins;
    public PluginAttributesSpigot(CoinsCore coins) {
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
}
