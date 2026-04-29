package community.coins.plugin.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public abstract class BasicPlugin extends JavaPlugin {
    protected static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public abstract PluginAttributes getAttributes();

    public abstract ItemParseApi getItemParseApi();

    public void parseEventHandlers(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public static final String LINE = "--------------------------------------------------------------------";

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }
}
