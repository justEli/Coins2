package community.coins.plugin.util;

import java.util.UUID;

/**
 * @author Eli
 * @since November 16, 2026
 */
public class BasicPlayer {
    private final UUID uuid;
    private final String username;

    public BasicPlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}
