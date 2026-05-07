package community.coins.plugin;

import community.coins.plugin.api.BasicPlugin;
import community.coins.plugin.command.CommandService;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.data.PersistentData;
import community.coins.plugin.economy.EconomyService;
import community.coins.plugin.economy.PickupHandler;
import community.coins.plugin.folialib.FoliaScheduler;
import community.coins.plugin.handler.CancellationHandler;
import community.coins.plugin.handler.CoinBehaviorHandler;
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
        loadImplementations();

        // scheduler setup with folia and Bukkit support
        this.foliaScheduler = new FoliaScheduler(this);

        // register all possible event types
        this.commandService = new CommandService();
        this.eventTypeService = new EventTypeService(this);
        this.economyService = new EconomyService(this);

        loadBasicFunctionality();

        // parse all configs
        this.configWarns = new ConfigWarns(this);
        this.coinService = new CoinService(this);
        this.configService = new ConfigService(this);

        // basic utilities
        this.persistentData = new PersistentData(this);

        // registering registrars of events
        new PlayerPickupCoinRegistrar(this);
        new PickupHandler(this);

        // get latest version
        this.versionCheck = new VersionCheck(this);
        VIRTUAL_EXECUTOR.submit(() -> versionCheck.findLatestVersion(ConfigYml.NOTIFY_ON_UPDATE));

        new Stats(this);

        // some event handling
        new CancellationHandler(this);
        new CoinBehaviorHandler(this);
        new EntityDataHandler(this);

        // things to load after core enabled
        loadAfterCore();
    }

    @Override
    public void onDisable() {
        for (Runnable task : shutdownTasks.reversed()) {
            try { task.run(); }
            catch (Exception _) {}
        }
    }

    public void debug(String message) {
        if (ConfigYml.DEBUG_LOGGING) {
            getLogger().warning("(Debug @ %d) %s".formatted(System.currentTimeMillis(), message));
        }
    }

    private ConfigWarns configWarns;
    public ConfigWarns getConfigWarns() {
        return configWarns;
    }

    private CommandService commandService;
    public CommandService getCommandService() {
        return commandService;
    }

    private EconomyService economyService;
    public EconomyService getEconomyService() {
        return economyService;
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

    public abstract void loadImplementations();
    public abstract void loadBasicFunctionality();
    public abstract void loadAfterCore();
}
