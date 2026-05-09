package community.coins.plugin;

import community.coins.plugin.command.CommandService;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.util.MessagePosition;
import community.coins.plugin.misc.PersistentData;
import community.coins.plugin.coin.CoinDepositHandler;
import community.coins.plugin.economy.EconomyService;
import community.coins.plugin.folialib.FoliaScheduler;
import community.coins.plugin.coin.CancellationHandler;
import community.coins.plugin.coin.CoinBehaviorHandler;
import community.coins.plugin.misc.EntityDataHandler;
import community.coins.plugin.coin.CoinMeta;
import community.coins.plugin.language.FormatEntry;
import community.coins.plugin.misc.MetricsHandler;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.coin.PlayerPickupCoinRegistrar;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.misc.VersionHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 27, 2026
 */
public abstract class CoinsCore extends JavaPlugin {
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void onEnable() {
        loadImplementations();

        // scheduler setup with folia and Bukkit support
        this.foliaScheduler = new FoliaScheduler(this);

        // basic utilities
        this.persistentData = new PersistentData(this);

        // register basic services
        this.commandService = new CommandService();
        this.eventTypeService = new EventTypeService(this);
        this.economyService = new EconomyService(this);

        loadBasicFunctionality();

        // parse all configs
        this.configWarns = new ConfigWarns(this);
        this.coinMeta = new CoinMeta(this);
        this.configService = new ConfigService(this);

        // register some events
        new CancellationHandler(this);
        new CoinBehaviorHandler(this);
        new CoinDepositHandler(this);
        new PlayerPickupCoinRegistrar(this);
        new EntityDataHandler(this);

        // version checking and register metrics
        this.versionHandler = new VersionHandler(this);
        VIRTUAL_EXECUTOR.submit(() -> versionHandler.findLatestVersion(ConfigYml.NOTIFY_ON_UPDATE));
        this.metricsHandler = new MetricsHandler(this);
    }

    @Override
    public void onDisable() {
        for (Runnable task : shutdownTasks.reversed()) {
            try { task.run(); }
            catch (Exception _) {}
        }
    }

    // basic plugin functionality

    public void parseEventHandlers(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    protected final List<Runnable> shutdownTasks = new LinkedList<>();

    public void addShutdownTask(Runnable task) {
        shutdownTasks.add(task);
    }

    // logging and messaging

    public static final String LINE = "--------------------------------------------------------------------";

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void debug(String message) {
        if (ConfigYml.DEBUG_LOGGING) {
            getLogger().warning("(Debug @ %d) %s".formatted(System.currentTimeMillis(), message));
        }
    }

    private static final Title.Times TITLE_DURATION = Title.Times.times(
        Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)
    );

    public void sendMessage(CommandSender sender, MessagePosition position, Component component) {
        Audience audience = getComponentApi().getAudience(sender);
        switch (position) {
            case CHAT -> audience.sendMessage(component);
            case ACTIONBAR -> audience.sendActionBar(component);
            case TITLE -> audience.showTitle(Title.title(component, Component.empty(), TITLE_DURATION));
            case SUBTITLE -> audience.showTitle(Title.title(Component.empty(), component, TITLE_DURATION));
        }
    }

    public void sendMessage(CommandSender sender, Component component) {
        sendMessage(sender, MessagePosition.CHAT, component);
    }

    public void sendMessage(CommandSender sender, FormatEntry entry) {
        sendMessage(sender, entry.getComponent());
    }

    // services

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

    private ConfigService configService;
    public ConfigService getConfigService() {
        return configService;
    }

    // other handlers

    private ConfigWarns configWarns;
    public ConfigWarns getConfigWarns() {
        return configWarns;
    }

    private PersistentData persistentData;
    public PersistentData getPersistentData() {
        return persistentData;
    }

    private FoliaScheduler foliaScheduler;
    public FoliaScheduler getScheduler() {
        return foliaScheduler;
    }

    private CoinMeta coinMeta;
    public CoinMeta getCoinMeta() {
        return coinMeta;
    }

    private VersionHandler versionHandler;
    public VersionHandler getVersion() {
        return versionHandler;
    }

    private MetricsHandler metricsHandler;
    public MetricsHandler getMetrics() {
        return metricsHandler;
    }

    // implementation for different platforms

    public abstract ComponentApi getComponentApi();

    public abstract ItemParseApi getItemParseApi();

    public abstract PluginAttributes getAttributes();

    public abstract void loadImplementations();

    public abstract void loadBasicFunctionality();
}
