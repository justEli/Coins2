package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class ItemRepairType extends EventType {
    public ItemRepairType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasTargetType()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "item_repair", filter);
    }

    // event: 'item_repair'
    // filters:
    //   initiator:
    //     enabled: Boolean
    //     permission: String
    //   target:
    //     enabled: true
    //     type: List<String>   (item types)
    //   location:
    //     disabled-worlds: List<String>
    //     cooldown:
    //       cap-amount: Boolean
    //       duration: TimeString
    // coins: ...

    @EventHandler(ignoreCancelled = true)
    void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (!(inventory instanceof AnvilInventory anvil)) {
            return;
        }

        InventoryView view = event.getView();
        int rawSlot = event.getRawSlot();

        // compare raw slot to the inventory view to make sure we are in the upper inventory
        if (rawSlot != view.convertSlot(rawSlot)) {
            return;
        }

        // 2 = result slot
        if (rawSlot != 2) {
            return;
        }

        // all three items in the anvil inventory
        ItemStack[] items = anvil.getContents();

        // item in the left slot
        ItemStack leftItem = items[0];

        // item in the right slot
        ItemStack rightItem = items[1];

        // I do not know if this is necessary
        if (leftItem == null || rightItem == null) {
            return;
        }

        Material leftType = leftItem.getType();
        Material rightType = rightItem.getType();

        // if the player is repairing something the ids will be the same
        if (leftType == Material.AIR || leftType != rightType) {
            return;
        }

        // item in the result slot
        ItemStack result = event.getCurrentItem();

        // check if there is an item in the result slot
        if (result == null) {
            return;
        }

        ItemMeta meta = result.getItemMeta();
        if (meta == null) {
            return;
        }

        // get the repairable interface to get the repair cost
        if (!(meta instanceof Repairable repairable)) {
            return;
        }

        int repairCost = repairable.getRepairCost();

        // can the player afford to repair the item
        if (player.getLevel() < repairCost) {
            return;
        }

        if (anvil.getLocation() == null) {
            return;
        }

        var block = anvil.getLocation().getBlock();
        var filter = createForm()
            .withInitiatorEntity(player)
            .withTargetType(rightType)
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(block.getRelative(BlockFace.UP));
    }
}
