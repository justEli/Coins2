package community.coins.plugin.drops;

import community.coins.plugin.type.filter.EventFilterForm;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * @author Eli
 * @since May 04, 2026
 */
@NullMarked
public final class CoinDropAction {
    private final DefinedDrop definedDrop;
    private final EventFilterForm filter;
    private final List<ItemStack> coinItems;

    public CoinDropAction(DefinedDrop definedDrop, EventFilterForm filter, List<ItemStack> coinItems) {
        this.definedDrop = definedDrop;
        this.filter = filter;
        this.coinItems = coinItems;
    }

    /// the defined drop that this action is coming from
    public DefinedDrop getDefinedDrop() {
        return definedDrop;
    }

    /// the used filter that led to this coin drop action
    public EventFilterForm getFilter() {
        return filter;
    }

    /// the coins that were determined to be dropped from the defined drop
    public List<ItemStack> getCoinItems() {
        return coinItems;
    }
}
