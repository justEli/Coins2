package community.coins.plugin.type.api;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.DepositType;
import community.coins.plugin.type.filter.EventFilterForm;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Eli
 * @since May 01, 2026
 */
@NullMarked
public final class FilteredEvent {
    private final CoinsCore coins;
    private final EventFilterForm filter;
    public FilteredEvent(CoinsCore coins, EventFilterForm filter) {
        this.filter = filter;
        this.coins = coins;
    }

    public void thenDrop(Block block) {
        if (!filter.isAllowed()) {
            return;
        }

        thenDrop(block.getLocation().add(.5, .5, .5));
    }

    public void thenDrop(Location location) {
        if (!filter.isAllowed()) {
            return;
        }

        DepositType depositType = filter.getDepositType();
        Entity initiator = filter.getInitiatorEntity(); // can be null if 'any'

        thenConsume(coins -> {
            for (ItemStack coin : coins) {
                if (depositType == DepositType.BALANCE && initiator instanceof Player player) {
                    // todo deposit coin's value to balance
                }
                else if (depositType == DepositType.INVENTORY && initiator instanceof Player player) {
                    player.getInventory().addItem(coin);
                }
                else if (location.getWorld() != null) {
                    location.getWorld().dropItemNaturally(location, coin);
                }
            }
        });
    }

    public void thenConsume(Consumer<List<ItemStack>> coinItems) {
        if (!filter.isAllowed()) {
            return;
        }

        coinItems.accept(filter.getCoinItems());
        coins.debug("Dropping or consuming coin for '%s'".formatted(filter.getEventIdentifier()));
    }
}
