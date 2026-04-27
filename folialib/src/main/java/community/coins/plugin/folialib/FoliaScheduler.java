package community.coins.plugin.folialib;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class FoliaScheduler {
    private final Plugin plugin;
    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runEntityTaskLater(Entity entity, long delayTicks, Runnable runnable) {
        if (PlatformUtil.isFolia()) {
            entity.getScheduler().runDelayed(plugin, task -> runnable.run(), runnable, delayTicks);
        }
        else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public void runLocationTaskLater(Location location, long delayTicks, Runnable runnable) {
        if (PlatformUtil.isFolia()) {
            plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delayTicks);
        }
        else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }
}
