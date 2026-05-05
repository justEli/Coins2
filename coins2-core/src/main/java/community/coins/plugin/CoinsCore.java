package community.coins.plugin;

import community.coins.plugin.api.BasicPlugin;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.data.PersistentData;
import community.coins.plugin.folialib.FoliaScheduler;
import community.coins.plugin.handler.CoinBehaviourHandler;
import community.coins.plugin.handler.EntityDataHandler;
import community.coins.plugin.item.CoinService;
import community.coins.plugin.metrics.Stats;
import community.coins.plugin.registrar.PlayerPickupCoinRegistrar;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.util.VersionCheck;

/**
 * @author Eli
 * @since April 27, 2026
 */
public abstract class CoinsCore extends BasicPlugin {
    @Override
    public void onEnable() {
        beforeCoreLoaded();

        // registering registrars of events
        new PlayerPickupCoinRegistrar(this);

        // register all possible event types
        this.eventTypeService = new EventTypeService(this);

        // parse all configs
        this.coinService = new CoinService(this);
        this.configService = new ConfigService(this);

        // basic utilities
        this.persistentData = new PersistentData(this);

        // scheduler setup with folia and Bukkit support
        this.foliaScheduler = new FoliaScheduler(this);

        // get latest version
        this.versionCheck = new VersionCheck(this);
        VIRTUAL_EXECUTOR.submit(() -> versionCheck.findLatestVersion(ConfigYml.NOTIFY_ON_UPDATE));

        getLogger().info("Loading CoinsCore");
        new Stats(this);

        // some event handling
        new CoinBehaviourHandler(this);
        new EntityDataHandler(this);

        // things to load after core enabled
        afterCoreLoaded();
    }

    @Override
    public void onDisable() {
        for (Runnable task : shutdownTasks.reversed()) {
            try { task.run(); }
            catch (Exception _) {}
        }
    }

    public void debug(String message) {
        getLogger().warning("(Debug @ %d) %s".formatted(System.currentTimeMillis(), message));
    }

    private EventTypeService eventTypeService;
    public EventTypeService getEventTypeService() {
        return eventTypeService;
    }

    private PersistentData persistentData;
    public PersistentData getPersistentData() {
        return persistentData;
    }

    private FoliaScheduler foliaScheduler;
    public FoliaScheduler getScheduler() {
        return foliaScheduler;
    }

    private CoinService coinService;
    public CoinService getCoinService() {
        return coinService;
    }

    private ConfigService configService;
    public ConfigService getConfigService() {
        return configService;
    }

    private VersionCheck versionCheck;
    public VersionCheck getVersionCheck() {
        return versionCheck;
    }

    public abstract void beforeCoreLoaded();
    public abstract void afterCoreLoaded();
}
