package community.coins.plugin.spigot.type;

import community.coins.plugin.api.BasicPlugin;
import community.coins.plugin.type.event.AdvancementDisplayEvent;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * todo test on spigot
 * @author Eli
 * @since May 04, 2026
 */
public final class AdvancementDisplayRegistrar implements Listener {
    private final BasicPlugin plugin;
    public AdvancementDisplayRegistrar(BasicPlugin plugin) {
        this.plugin = plugin;
        plugin.parseEventHandlers(this);
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Advancement advancement = event.getAdvancement();
        if (advancement.getDisplay() == null) {
            return;
        }

        plugin.getServer().getPluginManager().callEvent(new AdvancementDisplayEvent(
            event.getPlayer(), event.getAdvancement()
        ));
    }
}
