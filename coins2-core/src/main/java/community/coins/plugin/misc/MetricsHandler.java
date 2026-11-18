package community.coins.plugin.misc;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigYml;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class MetricsHandler {
    public static boolean USING_ECONOMY_VAULT = false;
    public static String STORAGE_INTEGRATION = "none";

    public MetricsHandler(CoinsCore coins) {
        Metrics metrics = new Metrics(coins, 31200);

        // total configured items
        metrics.addCustomChart(new SimplePie("totalCoinsEnabled", () ->
            String.valueOf(coins.getConfigService().getCoinsConfig().getDefinedKeys().size()))
        );
        metrics.addCustomChart(new SimplePie("totalDropsEnabled", () ->
            String.valueOf(coins.getConfigService().getDropsConfig().getDefinedKeys().size()))
        );
        metrics.addCustomChart(new SimplePie("totalCurrenciesEnabled", () ->
            String.valueOf(coins.getConfigService().getCurrenciesConfig().getDefinedKeys().size()))
        );

        // config.yml
        metrics.addCustomChart(new SimplePie("locale", () -> ConfigYml.LOCALE));
        metrics.addCustomChart(new SimplePie("notifyOnUpdate", () -> Boolean.toString(ConfigYml.NOTIFY_ON_UPDATE)));

        // currencies.yml
        metrics.addCustomChart(new SimplePie("usingEconomyVault", () -> Boolean.toString(USING_ECONOMY_VAULT)));
        metrics.addCustomChart(new SimplePie("storageIntegration", () -> STORAGE_INTEGRATION));

        // in-game statistics
        metrics.addCustomChart(new SingleLineChart("totalCoinsCreated", () -> totalCoinsCreated.getAndSet(0)));
    }

    private final AtomicInteger totalCoinsCreated = new AtomicInteger(0);

    public void registerCoinCreate(int amount) {
        totalCoinsCreated.addAndGet(amount);
    }
}
