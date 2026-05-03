package community.coins.plugin.type.filter;

import community.coins.plugin.config.DepositType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Eli
 * @since May 02, 2026
 */
public final class EventFilterForm {
    private final EventFilterFormBuilder builder;
    private final List<ItemStack> coinItems;

    public EventFilterForm(EventFilterFormBuilder builder, List<ItemStack> coinItems) {
        this.builder = builder;
        this.coinItems = coinItems;
    }

    public boolean isAllowed() {
        return builder.isAllowed();
    }

    public @Nullable Entity getInitiatorEntity() {
        return builder.getInitiator(); // todo implement properly
    }

    public DepositType getDepositType() {
        return DepositType.DROP; // todo implement
    }

    public List<ItemStack> getCoinItems() {
        return coinItems;
    }

    public String getEventIdentifier() {
        return builder.getEventIdentifier();
    }
}
