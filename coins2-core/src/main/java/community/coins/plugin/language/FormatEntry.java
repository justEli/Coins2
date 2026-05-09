package community.coins.plugin.language;

import community.coins.plugin.util.ColorResolver;
import community.coins.plugin.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/**
 * a language entry that gets parsed to mini-message
 * @author Eli
 * @since April 24, 2026
 */
@NullMarked
public class FormatEntry extends Entry {
    protected final Component component;
    public FormatEntry(String message) {
        super(message);
        this.component = ComponentUtil.parse(message).colorIfAbsent(ColorResolver.PRIMARY);
    }

    public Component getComponent() {
        return component;
    }
}
