package community.coins.plugin.config;

import org.bukkit.event.block.Action;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eli
 * @since November 16, 2026
 */
public final class GlobalStateConfig {
    private final Set<Action> depositActions = ConcurrentHashMap.newKeySet();
    public Set<Action> getDepositActions() {
        return depositActions;
    }
}
