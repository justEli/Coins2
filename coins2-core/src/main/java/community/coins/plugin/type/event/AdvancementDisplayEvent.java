package community.coins.plugin.type.event;

import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * spigot and paper have slightly different handling for advancement done event
 * @author Eli
 * @since May 04, 2026
 */
public final class AdvancementDisplayEvent extends Event {
    private final Player player;
    private final Advancement advancement;

    public AdvancementDisplayEvent(Player player, Advancement advancement) {
        this.player = player;
        this.advancement = advancement;
    }

    public Player getPlayer() {
        return player;
    }

    public Advancement getAdvancement() {
        return advancement;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
