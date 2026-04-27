package community.coins.plugin;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Eli
 * @since April 27, 2026
 */
public abstract class CoinsCore extends JavaPlugin {
    @Override
    public void onEnable() {
        addMetrics();
    }

    private void addMetrics() {
        Metrics metrics = new Metrics(this, 30976);
        metrics.addCustomChart(new SimplePie("chart_id", () -> "My value"));
        getLogger().info("Loading CoinsCore");

        onPluginLoad();
    }

    public abstract void onPluginLoad();
}
