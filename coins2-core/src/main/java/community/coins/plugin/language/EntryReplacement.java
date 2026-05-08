package community.coins.plugin.language;

import community.coins.plugin.component.ColorResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 24, 2026
 */
@NullMarked
public final class EntryReplacement {
    private final String identifier;
    private final TextColor color;

    public EntryReplacement(String identifier) {
        this.identifier = identifier;
        this.color = ColorResolver.VAR;
    }

    public EntryReplacement(String identifier, TextColor color) {
        this.identifier = identifier;
        this.color = color;
    }

    public Filled filled(Component component) {
        return new Filled(component);
    }

    public Filled filled(Object replacement) {
        return new Filled(replacement);
    }

    public class Filled {
        private final Component replacement;
        public Filled(Component component) {
            this.replacement = component;
        }

        public Filled(Object replacement) {
            this.replacement = Component.text(replacement.toString(), color);
        }

        public String getIdentifier() {
            return identifier;
        }

        public Component getReplacement() {
            return replacement;
        }
    }
}
