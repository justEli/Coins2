package community.coins.plugin.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Eli
 * @since April 14, 2026
 */
public final class BalanceChangeEvent extends Event implements Cancellable {
    private final UUID uuid;
    private final BigDecimal transactionAmount;
    private final BigDecimal previousBalance;

    public BalanceChangeEvent(boolean async, UUID uuid, BigDecimal transactionAmount, BigDecimal previousBalance) {
        super(async);

        this.uuid = uuid;
        this.transactionAmount = transactionAmount;
        this.previousBalance = previousBalance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
