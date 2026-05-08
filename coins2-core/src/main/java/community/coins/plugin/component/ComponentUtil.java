package community.coins.plugin.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 20, 2026
 */
@NullMarked
public final class ComponentUtil {
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
        .strict(false).editTags(builder -> builder.resolver(new ColorResolver())).build();

    /// parse a String of MiniMessage format to Component
    public static Component parse(@Nullable String message) {
        return (message == null || message.isEmpty())? Component.empty() : MINI_MESSAGE.deserialize(message);
    }

    public static String toStripped(Component component) {
        return PLAIN_TEXT_SERIALIZER.serialize(component);
    }

    /// replaces {amount} to appropriate value
    public static Component replaceAmount(Component component, String formattedAmount) {
        return component.replaceText(builder ->
            builder.matchLiteral("{amount}").replacement(formattedAmount)
        );
    }
}
