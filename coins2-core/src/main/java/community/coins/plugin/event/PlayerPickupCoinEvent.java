package community.coins.plugin.event;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

/**
 * called when a detected coin is successfully picked up.
 * if not canceled, this will push the coin upwards.
 * later on, depositing money, etc. will be handled
 * @author Eli
 * @since May 05, 2026
 */
@NullMarked
public final class PlayerPickupCoinEvent extends Event implements Cancellable {
    private final Player player;
    private final Item item;
    private boolean disallowed = true;

    public PlayerPickupCoinEvent(Player player, Item item) {
        this.player = player;
        this.item = item;
    }

    public Player getPlayer() {
        return player;
    }

    public Item getItem() {
        return item;
    }

    public void setPickupDisallowed(boolean disallowed) {
        this.disallowed = disallowed;
    }

    /// pickup is disallowed by default, returning true
    public boolean isPickupDisallowed() {
        return disallowed;
    }

    // -- Cancellable --

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    // -- HandlerList --

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
