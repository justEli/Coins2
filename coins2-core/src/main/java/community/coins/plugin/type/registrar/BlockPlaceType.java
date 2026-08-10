package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * @author Eli
 * @since August 10, 2026
 */
public final class BlockPlaceType extends EventType {
    public BlockPlaceType(CoinsCore coins, EventTypeService service) {
        super(coins, service, "block_place", filter -> filter
            .hasInitiatorPlayer()
            .hasInitiatorEntity()
            .hasTargetType()
            .hasLocationWorld()
        );
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#block_place

    @EventHandler(ignoreCancelled = true)
    void onBlockBreakEvent(BlockPlaceEvent event) {
        var player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        var block = event.getBlock();
        var filter = createFilter()
            .withInitiatorEntity(player)
            .withTargetType(block.getType())
            .withLocationWorld(block.getWorld());

        callEvent(filter, block.getRelative(BlockFace.UP));
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman enderman)) {
            return;
        }

        var block = event.getBlock();
        if (event.getTo() == Material.AIR) {
            return; // when the block becomes air, it was broken (not placed)
        }

        var filter = createFilter()
            .withInitiatorEntity(enderman)
            .withTargetType(event.getTo())
            .withLocationWorld(block.getWorld());

        callEvent(filter, block.getRelative(BlockFace.UP));
    }
}
