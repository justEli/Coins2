package community.coins.plugin.util;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class Util {
    public static <T extends Keyed> Optional<T> getType(@Nullable String type, Registry<T> registry) {
        if (type == null) {
            return Optional.empty();
        }

        var key = NamespacedKey.fromString(type);
        if (key == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(registry.get(key));
    }

    public static String toCapitalized(@Nullable String message) {
        return (message == null || message.isEmpty())? "" : message.substring(0, 1).toUpperCase() + message.substring(1).toLowerCase();
    }

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([dhms])");

    public static OptionalInt toDurationMillis(@Nullable String timeString) {
        if (timeString == null) {
            return OptionalInt.empty();
        }

        Matcher matcher = DURATION_PATTERN.matcher(timeString.toLowerCase());

        int millis = 0;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "d" -> millis += value * 86400000;
                case "h" -> millis += value * 3600000;
                case "m" -> millis += value * 60000;
                case "s" -> millis += value * 1000;
                default -> millis = -1;
            }

            if (millis < 0) {
                return OptionalInt.empty();
            }
        }

        if (millis == 0) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(millis);
    }

    public static <T extends Enum<T>> @Nullable T getEnum(Class<T> type, String value) {
        if (value == null) {
            return null;
        }

        try { return Enum.valueOf(type, value.toUpperCase().replace(" ", "_")); }
        catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    public static double toRoundedMoneyDecimals(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }

    public static OptionalDouble parseDouble(String number) {
        try {
            return OptionalDouble.of(Double.parseDouble(number.replaceAll("[<>\\[\\]]", "")));
        }
        catch (NumberFormatException exception) {
            return OptionalDouble.empty();
        }
    }

    public static String toIdentifier(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim().replace(" ", "_");
    }
}
