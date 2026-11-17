package community.coins.plugin.api;

import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public interface PluginAttributes {
    /// @return current version of this plugin
    String getVersion();

    /// @return name of the plugin
    String getName();

    /// @return main url of the project
    String getUrl();

    /// @return description of the plugin
    String getDescription();

    /// @return world by its name, namespaced key, or uuid
    @Nullable World getWorld(String identifier);

    /// the git location in the format '{organization}/{repository}' or '{username}/{repository}'
    default String getRepository() {
        return "justEli/Coins2";
    }
}
