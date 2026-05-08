package community.coins.plugin.language;

import community.coins.plugin.util.Util;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 24, 2026
 */
@NullMarked
public final class WordEntry extends Entry {
    public WordEntry(String word) {
        super(word);
    }

    public Component getComponent() {
        return Component.text(raw);
    }

    public Component getCapitalizedComponent() {
        return Component.text(Util.toCapitalized(raw));
    }

    public String getCapitalized() {
        return Util.toCapitalized(raw);
    }
}
