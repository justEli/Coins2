package community.coins.plugin.metrics;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.config.CurrenciesConfig;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class Stats {
    public Stats(CoinsCore coins) {
        Metrics metrics = new Metrics(coins, 31200);

        // total configured items
        metrics.addCustomChart(new SimplePie("totalCoinsEnabled", () ->
            String.valueOf(coins.getConfigService().getCoinsConfig().getDefinedItems().size()))
        );
        metrics.addCustomChart(new SimplePie("totalDropsEnabled", () ->
            String.valueOf(coins.getConfigService().getDropsConfig().getDefinedItems().size()))
        );
        metrics.addCustomChart(new SimplePie("totalCurrenciesEnabled", () ->
            String.valueOf(coins.getConfigService().getCurrenciesConfig().getDefinedItems().size()))
        );

        // config.yml
        metrics.addCustomChart(new SimplePie("locale", () -> ConfigYml.LOCALE));
        metrics.addCustomChart(new SimplePie("notifyOnUpdate", () -> Boolean.toString(ConfigYml.NOTIFY_ON_UPDATE)));

        // currencies.yml
        metrics.addCustomChart(new SimplePie("usingVaultCurrency", () -> Boolean.toString(CurrenciesConfig.USING_VAULT_CURRENCY)));

        // in-game statistics
        metrics.addCustomChart(new SingleLineChart("totalCoinsCreated", () -> totalCoinsCreated.getAndSet(0)));
    }

    private final AtomicInteger totalCoinsCreated = new AtomicInteger(0);

    public void registerCoinCreate(int amount) {
        totalCoinsCreated.addAndGet(amount);
    }
}
