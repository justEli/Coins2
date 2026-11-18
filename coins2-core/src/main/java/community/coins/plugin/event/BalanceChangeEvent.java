package community.coins.plugin.event;

import community.coins.plugin.economy.DefinedCurrency;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Event that is called when this plugin handles the economy.
 * @author Eli
 * @since April 14, 2026
 */
@NullMarked
public final class BalanceChangeEvent extends Event implements Cancellable {
    private final UUID uuid;
    private final double transactionAmount;
    private final double previousBalance;
    private final DefinedCurrency currency;

    public BalanceChangeEvent(boolean async, UUID uuid, double transactionAmount, double previousBalance, DefinedCurrency currency) {
        super(async);

        this.uuid = uuid;
        this.transactionAmount = transactionAmount;
        this.previousBalance = previousBalance;
        this.currency = currency;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    public DefinedCurrency getCurrency() {
        return currency;
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
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
