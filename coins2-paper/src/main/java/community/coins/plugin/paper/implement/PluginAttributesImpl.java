package community.coins.plugin.paper.implement;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.PluginAttributes;
import org.jspecify.annotations.NullMarked;

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
}
