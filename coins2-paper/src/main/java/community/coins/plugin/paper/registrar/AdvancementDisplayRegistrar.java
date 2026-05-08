package community.coins.plugin.paper.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.event.AdvancementDisplayEvent;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * @author Eli
 * @since May 04, 2026
 */
public final class AdvancementDisplayRegistrar implements Listener {
    private final CoinsCore coins;
    public AdvancementDisplayRegistrar(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Advancement advancement = event.getAdvancement();
        if (advancement.getDisplay() == null) {
            return;
        }

        coins.getServer().getPluginManager().callEvent(new AdvancementDisplayEvent(
            event.getPlayer(), event.getAdvancement()
        ));
    }
}
