package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.Keyed;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class ItemEnchantType extends EventType {
    public ItemEnchantType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasTargetType()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "item_enchant", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#item_enchant

    // todo allow calculations with with event.getExpLevelCost()
    @EventHandler(ignoreCancelled = true)
    void onEnchantItemEvent(EnchantItemEvent event) {
        Collection<Keyed> enchants = new HashSet<>(event.getEnchantsToAdd().keySet());
        var block = event.getEnchantBlock();

        var filter = createForm()
            .withInitiatorEntity(event.getEnchanter())
            .withTargetType(enchants)
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(block.getRelative(BlockFace.UP));
    }
}
