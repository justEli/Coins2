package community.coins.plugin;

import community.coins.plugin.api.BasicPlugin;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.folialib.FoliaScheduler;
import community.coins.plugin.handler.MobTransformHandler;
import community.coins.plugin.item.CoinService;
import community.coins.plugin.data.PersistentData;
import community.coins.plugin.metrics.Stats;
import community.coins.plugin.util.VersionCheck;

/**
 * @author Eli
 * @since April 27, 2026
 */
public abstract class CoinsCore extends BasicPlugin {
    @Override
    public void onEnable() {
        beforeCoreLoaded();

        // basic utilities
        this.persistentData = new PersistentData(this);

        // scheduler setup with folia and Bukkit support
        this.foliaScheduler = new FoliaScheduler(this);

        // parse all configs
        this.configService = new ConfigService(this);
        this.coinService = new CoinService(this);

        // get latest version
        this.versionCheck = new VersionCheck(this);
        VIRTUAL_EXECUTOR.submit(() -> versionCheck.findLatestVersion(ConfigYml.NOTIFY_ON_UPDATE));

        getLogger().info("Loading CoinsCore");
        new Stats(this);

        // some event handling
        new MobTransformHandler(this);

        // things to load after core enabled
        afterCoreLoaded();
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
