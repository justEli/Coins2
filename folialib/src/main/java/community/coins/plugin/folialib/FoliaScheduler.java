package community.coins.plugin.folialib;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class FoliaScheduler {
    private final Plugin plugin;
    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runEntityTask(Entity entity, Runnable runnable) {
        if (PlatformUtil.isFolia()) {
            if (plugin.getServer().isOwnedByCurrentRegion(entity)) {
                runnable.run();
            }
            else {
                entity.getScheduler().run(plugin, _ -> runnable.run(), runnable);
            }
        }
        else {
            if (plugin.getServer().isPrimaryThread()) {
                runnable.run();
            }
            else {
                plugin.getServer().getScheduler().runTask(plugin, runnable);
            }
        }
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

    public void runLocationTaskRepeated(Location location, long amount, long period, Runnable runnable) {
        if (PlatformUtil.isFolia()) {
            AtomicInteger ticks = new AtomicInteger();
            plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, (task) -> {
                runnable.run();

                if (ticks.addAndGet(1) >= amount) {
                    task.cancel();
                }
            }, 1, period);
        }
        else {
            new BukkitRunnable() {
                private final AtomicInteger ticks = new AtomicInteger();

                @Override
                public void run() {
                    runnable.run();

                    if (ticks.addAndGet(1) >= amount) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, period);
        }
    }
}
