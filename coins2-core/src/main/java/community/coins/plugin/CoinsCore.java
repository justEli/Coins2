package community.coins.plugin;

import community.coins.plugin.command.CommandService;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.config.ConfigYml;
import community.coins.plugin.config.MessagePosition;
import community.coins.plugin.data.PersistentData;
import community.coins.plugin.economy.CoinDepositHandler;
import community.coins.plugin.economy.EconomyService;
import community.coins.plugin.folialib.FoliaScheduler;
import community.coins.plugin.handler.CancellationHandler;
import community.coins.plugin.handler.CoinBehaviorHandler;
import community.coins.plugin.handler.EntityDataHandler;
import community.coins.plugin.item.CoinMeta;
import community.coins.plugin.language.FormatEntry;
import community.coins.plugin.metrics.Stats;
import community.coins.plugin.platform.ComponentApi;
import community.coins.plugin.platform.ItemParseApi;
import community.coins.plugin.platform.PluginAttributes;
import community.coins.plugin.registrar.PlayerPickupCoinRegistrar;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.util.VersionCheck;
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

        // register all possible event types
        this.commandService = new CommandService();
        this.eventTypeService = new EventTypeService(this);
        this.economyService = new EconomyService(this);

        loadBasicFunctionality();

        // parse all configs
        this.configWarns = new ConfigWarns(this);
        this.coinMeta = new CoinMeta(this);
        this.configService = new ConfigService(this);

        // basic utilities
        this.persistentData = new PersistentData(this);

        // registering registrars of events
        new PlayerPickupCoinRegistrar(this);
        new CoinDepositHandler(this);

        // get latest version
        this.versionCheck = new VersionCheck(this);
        VIRTUAL_EXECUTOR.submit(() -> versionCheck.findLatestVersion(ConfigYml.NOTIFY_ON_UPDATE));

        this.stats = new Stats(this);

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

    public void parseEventHandlers(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    protected final List<Runnable> shutdownTasks = new LinkedList<>();

    public void addShutdownTask(Runnable task) {
        shutdownTasks.add(task);
    }

    public abstract ComponentApi getComponentApi();

    public abstract ItemParseApi getItemParseApi();

    public abstract PluginAttributes getAttributes();

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

    public static final String LINE = "--------------------------------------------------------------------";

    public void log(Level level, String message) {
        getLogger().log(level, message);
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

    private CoinMeta coinMeta;
    public CoinMeta getCoinMeta() {
        return coinMeta;
    }

    private ConfigService configService;
    public ConfigService getConfigService() {
        return configService;
    }

    private VersionCheck versionCheck;
    public VersionCheck getVersionCheck() {
        return versionCheck;
    }

    private Stats stats;
    public Stats getMetrics() {
        return stats;
    }

    public abstract void loadImplementations();
    public abstract void loadBasicFunctionality();
    public abstract void loadAfterCore();
}
