package community.coins.plugin.coin;

import community.coins.plugin.CoinsCore;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eli
 * @since May 05, 2026
 */
public final class CancellationHandler implements Listener {
    private final CoinsCore coins;
    public CancellationHandler(CoinsCore coins) {
        this.coins = coins;
    }

    @EventHandler
    void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            return; // already handled at other places
        }

        Item item = event.getItem();
        if (!coins.getCoinMeta().isCoin(item.getItemStack())) {
            return;
        }

        // don't let mobs pick up coins that are already being picked up by players
        // only canceled when the pickup delay was set (to prevent double pickup)
        if (item.getPickupDelay() == 0) {
            return;
        }

        event.setCancelled(true);
    }

    // prevent coins with immutable name from being changed

    private boolean isImmutable(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }
        return coins.getCoinMeta().isImmutableName(item);
    }

    @EventHandler(ignoreCancelled = true)
    void onPrepareAnvilEvent(PrepareAnvilEvent event) {
        if (isImmutable(event.getResult())) {
            event.setResult(null);
        }
    }

    @EventHandler
    void onCraftItemEvent(CraftItemEvent event) {
        for (ItemStack stack : event.getInventory().getContents()) {
            if (isImmutable(stack)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
        for (ItemStack stack : event.getInventory().getContents()) {
            if (isImmutable(stack)) {
                event.getInventory().setResult(null);
                break;
            }
        }
    }

    @EventHandler
    void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {
        if (isImmutable(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onFurnaceBurnEvent(FurnaceBurnEvent event) {
        if (isImmutable(event.getFuel())) {
            event.setBurnTime(0);
            event.setBurning(false);
        }
    }
}
