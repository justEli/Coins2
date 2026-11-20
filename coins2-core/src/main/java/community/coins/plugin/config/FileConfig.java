package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 30, 2026
 */
public abstract class FileConfig<T> extends BasicConfig {
    public FileConfig(CoinsCore coins, ConfigService service, String fileName) {
        super(coins, service, fileName);
    }

    protected final Map<String, T> defined = new HashMap<>();

    public Optional<T> getDefined(@NotNull String identifier) {
        return Optional.ofNullable(defined.get(identifier.toLowerCase()));
    }

    public Collection<T> getDefinedValues() {
        return defined.values();
    }

    public Set<String> getDefinedKeys() {
        return defined.keySet();
    }

    protected void putDefined(Map<String, T> values, String typeSingular, String typePlural) {
        defined.clear();
        defined.putAll(values);

        String type = defined.size() == 1? typeSingular : typePlural;
        coins.log(Level.INFO, "Loaded %,d defined %s from '%s'.".formatted(defined.size(), type, fileName));
    }
}
