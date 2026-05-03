package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class BlockBreakType extends EventType {
    public BlockBreakType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasInitiatorEntity()
            .hasTargetType()
            .hasTargetMinXpDrop()
            .hasTargetAllowSameBlock()
            .hasLocationWorld()
            .hasLocationCooldown();
        super(coins, service, "block_break", filter);
    }

    // event: 'block_break'
    // filters:
    //   initiator:
    //     type: ['player', 'enderman']
    //     enabled: Boolean
    //     permission: String
    //   target:
    //     type: List<String>   (block types)
    //     min-xp-drop: Integer
    //     allow-same-block: Boolean
    //   location:
    //     disabled-worlds: List<String>
    //     cooldown:
    //       cap-amount: Boolean
    //       duration: TimeString
    // coins: ...

    // todo default config set xp to drop > 0
    @EventHandler(ignoreCancelled = true)
    void onBlockBreakEvent(BlockBreakEvent event) {
        var player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        var block = event.getBlock();
        var filter = createForm()
            .withInitiatorEntity(player)
            .withTargetType(block.getType())
            .withTargetXpDrop(event.getExpToDrop())
            .withTargetSameBlock(isSameDrop(block, player))
            .withLocationWorld(block.getWorld())
            .withLocationCooldown(block.getLocation())
            .build();

        filterEvent(filter).thenDrop(block);
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Enderman enderman)) {
            return;
        }

        var block = event.getBlock();
        if (block.getType() == Material.AIR || event.getTo() != Material.AIR) {
            return;
        }

        var filter = createForm()
            .withInitiatorEntity(enderman)
            .withTargetType(block.getType())
            .withLocationWorld(block.getWorld())
            .build();

        filterEvent(filter).thenDrop(block);
    }

    /// the block material that is mined is exactly the same as the item it drops
    private static boolean isSameDrop(Block block, Player player) {
        var type = block.getType();
        var breakTool = player.getInventory().getItemInMainHand();

        for (ItemStack item : block.getDrops(breakTool)) {
            if (item.getType() == type) {
                return true;
            }
        }

        return false;
    }
}
