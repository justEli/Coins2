package community.coins.plugin.handler;

import community.coins.plugin.CoinsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eli
 * @since April 30, 2026
 */
public final class CoinBehaviourHandler implements Listener {
    private final CoinsCore coins;

    public CoinBehaviourHandler(CoinsCore coins) {
        this.coins = coins;
        coins.parseEventHandlers(this);
    }

    // apply characteristics of coins that are not present as ItemStack, but only as Item
    @EventHandler(ignoreCancelled = true)
    void onItemSpawnEvent(ItemSpawnEvent event) {
        var item = event.getEntity();
        coins.getCoinService().getCoinMeta().applyGlowIfPresent(item);
        coins.getCoinService().getCoinMeta().applyHologramIfPresent(item);
        coins.getCoinService().getCoinMeta().applyUniqueIfPresent(item);
    }

    // remove the uniqueness of the coin, so it can stack in the inventory again
    @EventHandler(ignoreCancelled = true)
    void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        coins.getCoinService().getCoinMeta().removeUniqueIfPresent(event.getItem());
    }

    // prevent coins from being picked up by hoppers if configured that way
    @EventHandler(ignoreCancelled = true)
    void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        if (event.getInventory().getType() != InventoryType.HOPPER) {
            return;
        }

        if (coins.getCoinService().getCoinMeta().isNoHopperPickup(event.getItem())) {
            event.setCancelled(true);
        }
    }

    // prevent coins with immutable name from being changed

    private boolean isImmutable(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }
        return coins.getCoinService().getCoinMeta().isImmutableName(item);
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
