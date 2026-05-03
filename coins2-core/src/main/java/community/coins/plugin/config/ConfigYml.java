package community.coins.plugin.config;

/**
 * @author Eli
 * @since April 27, 2026
 */
@ConfigFile("config.yml")
public final class ConfigYml {
    @ConfigEntry("locale")
    public static String LOCALE = "en-US";

    @ConfigEntry("notify-on-update")
    public static boolean NOTIFY_ON_UPDATE = true;
}
