package community.coins.plugin.misc;

import com.google.gson.JsonParser;
import community.coins.plugin.CoinsCore;
import community.coins.plugin.util.ReleaseVersion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Eli
 * @since February 04, 2022 (creation)
 */
public final class VersionHandler {
    private final CoinsCore coins;
    public VersionHandler(CoinsCore coins) {
        this.coins = coins;
    }

    public void findLatestVersion(boolean printToConsole) {
        Optional<ReleaseVersion> version = findLatestVersion(coins.getAttributes().getRepository());
        if (version.isEmpty()) {
            return;
        }

        this.latestVersion = version.get();
        if (!printToConsole) {
            return;
        }

        var pluginVersion = coins.getAttributes().getVersion();
        if (!pluginVersion.equals(latestVersion.tag()) && !latestVersion.preRelease()) {
            coins.log(Level.WARNING, CoinsCore.LINE);
            coins.log(Level.WARNING, """
                Detected an outdated version of %s (%s is installed).
                The latest version is %s, released on %s.
                Download: %s"""
                .formatted(
                    coins.getAttributes().getName(),
                    pluginVersion,
                    latestVersion.tag(),
                    latestVersion.date(),
                    coins.getAttributes().getUrl()
                )
            );
            coins.log(Level.WARNING, CoinsCore.LINE);
        }
    }

    private ReleaseVersion latestVersion;
    public Optional<ReleaseVersion> getLatestVersion() {
        return Optional.ofNullable(latestVersion);
    }

    public static Optional<ReleaseVersion> findLatestVersion(String repository) {
        try {
            URL url = URI.create("https://api.github.com/repos/" + repository + "/releases/latest").toURL();
            URLConnection request = url.openConnection();

            request.setReadTimeout(1000);
            request.setConnectTimeout(1000);
            request.connect();

            try (var reader = new InputStreamReader((InputStream) request.getContent())) {
                var root = JsonParser.parseReader(reader);
                var jsonObject = root.getAsJsonObject();
                return Optional.of(new ReleaseVersion(
                    jsonObject.get("tag_name").getAsString(),
                    jsonObject.get("prerelease").getAsBoolean(),
                    jsonObject.get("name").getAsString(),
                    jsonObject.get("published_at").getAsString()
                ));
            }
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
