package community.coins.plugin.config;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Eli
 * @since April 30, 2026
 */
public interface FileConfig<T> {
    String getFileName();

    Optional<T> getDefinedItem(@NotNull String key);

    Collection<T> getDefinedItems();

    void parseAndReload();
}
