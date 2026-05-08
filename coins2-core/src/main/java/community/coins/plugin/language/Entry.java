package community.coins.plugin.language;

import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 24, 2026
 */
@NullMarked
public abstract class Entry {
    protected final String raw;
    public Entry(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
