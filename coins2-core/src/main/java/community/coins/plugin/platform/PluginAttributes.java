package community.coins.plugin.platform;

import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public interface PluginAttributes {
    /// the current version of this plugin
    String getVersion();

    /// the name of the plugin
    String getName();

    /// the main url of the project
    String getUrl();

    /// the description of the plugin
    String getDescription();

    /// the git location in the format '{organization}/{repository}' or '{username}/{repository}'
    default String getRepository() {
        return "justEli/Coins2";
    }
}
