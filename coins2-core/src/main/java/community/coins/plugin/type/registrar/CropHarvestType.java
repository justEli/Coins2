package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.MangrovePropagule;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class CropHarvestType extends EventType {
    public CropHarvestType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasInitiatorEntity()
            .hasTargetType()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "crop_harvest", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#crop_harvest

    @EventHandler(ignoreCancelled = true)
    void onBlockBreakEvent(BlockBreakEvent event) {
        var block = event.getBlock();
        if (!isCrop(block)) {
            return;
        }

        var player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        var filter = createForm()
            .withInitiatorEntity(player)
            .withTargetType(event.getBlock().getType())
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(block);
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        var block = event.getBlock();
        if (!isCrop(block)) {
            return;
        }

        if (event.getTo() != Material.AIR) {
            return;
        }

        var filter = createForm()
            .withInitiatorEntity(villager)
            .withTargetType(event.getTo())
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(block);
    }

    private static boolean isCrop(Block block) {
        if (!(block.getState().getBlockData() instanceof Ageable ageable)) {
            return false;
        }
        if (block.getState().getBlockData() instanceof MangrovePropagule) {
            return false;
        }
        return ageable.getMaximumAge() == ageable.getAge() && ageable.getMaximumAge() >= 3;
    }
}
